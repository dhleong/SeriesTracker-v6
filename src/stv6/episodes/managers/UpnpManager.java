package stv6.episodes.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import stv6.episodes.BasicEpisode;
import stv6.http.HttpRequestor;
import stv6.http.request.Request;
import stv6.http.request.Response;

/**
 * Theoretically generic UPNP manager, but specifically
 *  designed to work with the PS3MediaServer UPNP server
 *  
 * @author dhleong
 *
 */
public class UpnpManager extends AbstractManager {
    
    private static final List<Path> basePaths = new LinkedList<Path>();
    static {
        basePaths.add(new Path("0", ""));
    }

    private static final String CONTENT_PATH = "/upnp/control/content_directory";
    private static final String BROWSE_ACTION = "urn:schemas-upnp-org:service:ContentDirectory:1#Search";
    
    private static final String CLASS_FOLDER = "object.container.storageFolder";
    private static final String CLASS_VIDEO  = "object.item.videoItem";
    
    private static final String DEFAULT_PROFILE_FILENAME = "PMS.conf";
    private static final String ENV_PROFILE_PATH = "PMS_PROFILE";
    private static final String PROPERTY_PROFILE_PATH = "pms.profile.path";
    /** TODO This is NOT necessarily constant, and we should add a setting */
    private static final String PROFILE_DIRECTORY_NAME = "PMS";
    
    private static final Pattern RESULT_PATTERN = Pattern.compile("<Result>(.*)</Result>");
    
    private String broadIp;
    private int upnpPort = -1;
    private boolean isPms = false;
    
    private String localFoldersInfo;
    private final List<Path> localFolders = new LinkedList<Path>();
    
    public UpnpManager(String broadcastingIp, int upnpPort) {
        this.broadIp = broadcastingIp;
        this.upnpPort = upnpPort;
    }
    
    public UpnpManager(String broadcastIp, int upnpPort, String localFoldersInfo) {
        this(broadcastIp, upnpPort);
        
        this.localFoldersInfo = localFoldersInfo;
    }
    
    public UpnpManager(String broadcastIp, boolean isPms) {
        this.broadIp = broadcastIp;
        
        if (!isPms) 
            return;
        
        this.isPms = true;
        
        reload(); // make sure we can load everything
    }

    @Override
    public void reload() {
        if (isPms) {
            
            // we can use some hax to get the information from the PMS.conf file
            File pmsConf = getPmsProfile();
            if (pmsConf == null || !pmsConf.exists()) {
                throw new RuntimeException("Couldn't find PMS configuration.");
            }
            
            // reset these to defaults
            localFoldersInfo = null; 
            upnpPort = -1;
            
            // load port and local folders
            try {
                BufferedReader r = new BufferedReader(new FileReader(pmsConf));
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.startsWith("folders")) {
                        String[] parts = line.split(" = ");
                        // PMS uses a comma instead of File.pathSeparator. Retarded...
                        localFoldersInfo = parts[1].replace(",", File.pathSeparator);
                    } else if (line.startsWith("port")) {
                        String[] parts = line.split(" = ");
                        upnpPort = Integer.parseInt(parts[1]);
                    } else if (line.startsWith("hostname")) {
                    	String[] parts = line.split(" = ");
                    	broadIp = parts[1];
                    }
                }
            } catch (FileNotFoundException e) {
                // won't happen... we already confirmed it exists
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Couldn't read PMS configuration");
            }
            
            if (upnpPort < 0)
                upnpPort = 5001; // PMS default
        }
        
        if (localFoldersInfo != null) {
            FileSystemManager.loadFolderInfo(localFoldersInfo, localFolders );
        }
    }

    @Override
    protected List<Path> getBasePaths() {
        return basePaths;
    }

    @Override
    protected ArrayList<BasicEpisode> getEpisodesFor(Path path) {
        ArrayList<BasicEpisode> episodes = new ArrayList<BasicEpisode>();
        Document doc = browsePage(path.pathId);
        if (doc == null)
            return episodes;
        
        NodeList nodes = doc.getElementsByTagName("item");
        for (int i=0; i<nodes.getLength(); i++) {
            Node n = nodes.item(i);
//            NamedNodeMap attr = n.getAttributes();
//            String id = attr.getNamedItem("id").getTextContent();
            String klass = findNamedChild(n, "upnp:class").getTextContent();
            if (CLASS_VIDEO.equals(klass)) {
//                String title = findNamedChild(n, "dc:title").getTextContent();
                String link = findNamedChild(n, "res").getTextContent();
                
                // nah, build the title from the link so we have the real filename
                String title;
                try {
                    title = URLDecoder.decode(
                            link.substring(link.lastIndexOf('/')+1), "UTF-8");

                    if (title != null && link != null) {
//                    System.out.println("id=" + id + " > " + title);
                        episodes.add(new BasicEpisode(title, link));
                    }
                } catch (UnsupportedEncodingException e) {
                    // shouldn't happen
                    e.printStackTrace();
                }
                
            }
        }
        
        return episodes;
    }

    @Override
    protected Iterable<Path> getSubpathsFor(Path path) {
        LinkedList<Path> subpaths = new LinkedList<Path>();
        Document doc = browsePage(path.pathId);
        if (doc == null)
            return subpaths;
        
        NodeList nodes = doc.getElementsByTagName("container");
        for (int i=0; i<nodes.getLength(); i++) {
            Node n = nodes.item(i);
            NamedNodeMap attr = n.getAttributes();
            String id = attr.getNamedItem("id").getTextContent();
            String klass = findNamedChild(n, "upnp:class").getTextContent();
            if (CLASS_FOLDER.equals(klass)) {
                String title = findNamedChild(n, "dc:title").getTextContent();
                
                if (title != null && !title.startsWith("#-")) {
//                System.out.println("id=" + id + " > " + title);
                    String local = path.localDirectory + File.separator + title;
                    subpaths.add(new Path(id, mapLocal(local)));
                }
            }
        }
        
        return subpaths;
    }

    private Document browsePage(String path) {
        try {
        	System.out.println("[debug:UpnpManager] " + broadIp + ":" + upnpPort + " -- " + path);
            HttpRequestor r = HttpRequestor.post("http://" + 
                    broadIp + CONTENT_PATH, upnpPort);
            Request req = r.getRequest();
            req.setHeader("SOAPAction", BROWSE_ACTION);
            req.setHeader("User-Agent", "SeriesTracker");
            req.setHeader("Content-Type", "text/xml; charset=\"utf-8\"");
            
            StringBuilder body = new StringBuilder();
            body.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope\"");
            body.append(" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding\">");
            body.append("<s:Body>");
            body.append("<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1#Browse\">");
            body.append("<ObjectID>");
            body.append(path);
            body.append("</ObjectID>");
//            body.append("<BrowseFlag>BrowseDirectChildren</BrowseFlag>");
            body.append("</u:Browse>");
            body.append("</s:Body>");
            body.append("</s:Envelope>");
            req.setBody(body);
            
            Response resp = r.request();
            if (!"200 OK".equals(resp.getStatus())) {
                // TODO
                return null;
            }
            
            Matcher m = RESULT_PATTERN.matcher(resp.getBody());
            if (!m.find()) {
                // TODO
                return null;
            }
                
            String result = m.group(1);
            DOMParser parser = new DOMParser();
            parser.parse(new InputSource(new StringReader(
                    result.replaceAll("&lt;", "<").replaceAll("&gt;", ">"))));
            return parser.getDocument();
            
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    private static Node findNamedChild(Node node, String name) {
        NodeList kids = node.getChildNodes();
        for (int i=0; i<kids.getLength(); i++) {
            Node n = kids.item(i);
            if (name.equals(n.getNodeName()))
                return n;
        }
        
        return null;
    }
    
    /**
     * Make sure the local path exists. 
     * @param localPath
     * @return
     */
    private String mapLocal(String localPath) {
        if (localFoldersInfo == null || localFolders.size() == 0)
            return localPath; // nothing to do
        
        File f = new File(localPath);
        if (f.exists())
            return localPath; // it's fine
        
        String testedPath;
        for (Path p : localFolders) {
            testedPath = p.localDirectory + File.separator + localPath;
            f = new File(testedPath);
            if (f.exists())
                return testedPath;
            
            // the name of the localDir could also be the first 
            //  item of localPath
            int lastSepPos = p.localDirectory.lastIndexOf(File.separatorChar);
            if (localPath.startsWith(
                    p.localDirectory.substring(lastSepPos))) {
                testedPath = p.localDirectory.substring(0, lastSepPos) + localPath;
                f = new File(testedPath);
                if (f.exists())
                    return testedPath;
            }
        }
        
        // oh well
        return localPath;
    }
    
    /**
     * Find the PMS.conf file with Playstation Media Server's config data
     * @return
     */
    private static File getPmsProfile() {
        // first try the system property, typically set via the profile chooser
        String profile = System.getProperty(PROPERTY_PROFILE_PATH);

        // failing that, try the environment variable
        if (profile == null) {
            profile = System.getenv(ENV_PROFILE_PATH);
        }
        
        File profileDirectory = null;

        if (profile != null) {
            File f = new File(profile);

            // if it exists, we know whether it's a file or directory
            // otherwise, it must be a file since we don't autovivify directories

            if (f.exists() && f.isDirectory()) {
                profileDirectory = f.getAbsoluteFile();
                return new File(f, DEFAULT_PROFILE_FILENAME).getAbsoluteFile();
            } else { // doesn't exist or is a file (i.e. not a directory)
                return f.getAbsoluteFile();
            }
        } else {
            
            String profileDir = null;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.indexOf( "win" ) >= 0) {
                String programData = System.getenv("ALLUSERSPROFILE");
                if (programData != null) {
                    profileDir = String.format("%s\\%s", programData, PROFILE_DIRECTORY_NAME);
                } else {
                    profileDir = ""; // i.e. current (working) directory
                }
            } else if (os.indexOf( "mac" ) >= 0) {
                profileDir = String.format(
                        "%s/%s/%s",
                        System.getProperty("user.home"),
                        "/Library/Application Support",
                        PROFILE_DIRECTORY_NAME
                );
            } else {
                String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");

                if (xdgConfigHome == null) {
                    profileDir = String.format("%s/.config/%s", System.getProperty("user.home"), PROFILE_DIRECTORY_NAME);
                } else {
                    profileDir = String.format("%s/%s", xdgConfigHome, PROFILE_DIRECTORY_NAME);
                }
            }

            File f = new File(profileDir);

            if ((f.exists() || f.mkdir()) && f.isDirectory()) {
                profileDirectory = f.getAbsoluteFile();
            } else {
//                PROFILE_DIRECTORY = FilenameUtils.normalize(new File("").getAbsolutePath());
                return null; // no ideas
            }

            return new File(profileDirectory, DEFAULT_PROFILE_FILENAME).getAbsoluteFile();
        }
    }
    
    /*
    public static final void main(String[] args) {
        UpnpManager man = new UpnpManager("192.168.11.3", true);
        man.reload();
        System.out.println("Got port: " + man.upnpPort);
        System.out.println("Got locals: " + man.localFoldersInfo);
        System.out.println("Fetching series...");
        SeriesList list = man.getAvailableSeries();
        System.out.println("done " + list.size());
        for (Series s : list) {
            System.out.println("Found: " + s.getName());
            if (s.isManaged()) {
                TrackedSeries t = new TrackedSeries((BasicSeries)s, 0, 0);
                for (Episode e : t.getEpisodes()) {
                    System.out.println(" - " + e.getTitle() + ": " + t.getLocalPathFor(e));
                }
            }
        }
    }
    */
}
