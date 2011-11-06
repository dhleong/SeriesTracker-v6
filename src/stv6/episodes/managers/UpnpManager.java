package stv6.episodes.managers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
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
    
    private final String broadIp;
    private final int upnpPort;
    private final Pattern resultPattern;
    
    public UpnpManager(String broadcastingIp, int upnpPort) {
        this.broadIp = broadcastingIp;
        this.upnpPort = upnpPort;
        
        resultPattern = Pattern.compile("<Result>(.*)</Result>");
    }

    @Override
    public void reload() {
        // Do we need to do anything?
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
                String title = findNamedChild(n, "dc:title").getTextContent();
                String link = findNamedChild(n, "res").getTextContent();
                
                if (title != null && link != null) {
//                System.out.println("id=" + id + " > " + title);
                    episodes.add(new BasicEpisode(title, link));
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
                    subpaths.add(new Path(id, path.localDirectory 
                            + File.separator + title));
                }
            }
        }
        
        return subpaths;
    }

    private Document browsePage(String path) {
        try {
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
            body.append("<BrowseFlag>BrowseDirectChildren</BrowseFlag>");
            body.append("</u:Browse>");
            body.append("</s:Body>");
            body.append("</s:Envelope>");
            req.setBody(body);
            
            Response resp = r.request();
            if (!"200 OK".equals(resp.getStatus())) {
                // TODO
                return null;
            }
            
            Matcher m = resultPattern.matcher(resp.getBody());
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
    
    /*
    public static final void main(String[] args) {
        UpnpManager man = new UpnpManager("192.168.11.2", 49000);
        System.out.println("Fetching series...");
        SeriesList list = man.getAvailableSeries();
        System.out.println("done " + list.size());
        for (Series s : list) {
            System.out.println("Found: " + s.getName());
            if (s.isManaged()) {
                for (Episode e : new TrackedSeries((BasicSeries)s, 0, 0).getEpisodes()) {
                    System.out.println(" - " + e.getTitle() + ": " + e.getLink());
                }
            }
        }
    }
    */
}
