package stv6.episodes.managers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import stv6.Profile;
import stv6.STHandlerManager;
import stv6.episodes.BasicEpisode;
import stv6.episodes.EpisodeManager;
import stv6.handlers.util.StaticMessageHandler;

public class MediaTombManager extends AbstractManager implements EpisodeManager {

	private static final int UPDATE_WAIT = 2500;
	
	private static final List<Path> basePaths = new LinkedList<Path>();
	static {
		basePaths.add(new Path("1", ""));
	}
	
	private final String broadIp, trProfile;
	private final int mtPort;
	private final boolean trCheck;
	private final List<String> trExtensions;
	
	private String sid;
	
	public MediaTombManager(String broadcastingIp, int mtport, String trProf, 
			boolean trCheck, List<String> trExtensions) {
		this.broadIp = broadcastingIp;
		this.mtPort = mtport;
		this.trProfile = trProf;
		this.trCheck = trCheck;
		this.trExtensions = trExtensions;
	}

	private boolean doSleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
		
		return true;
	}
	
	@Override
	protected List<Path> getBasePaths() {
		return basePaths;
	}

	@Override
	protected ArrayList<BasicEpisode> getEpisodesFor(Path path) {
		ArrayList<BasicEpisode> episodes = new ArrayList<BasicEpisode>();
		try {			
			Document cont = loadPage("items&parent_id="+path.pathId);
			NodeList items = cont.getElementsByTagName("item");
			if (items == null || items.getLength() == 0)
				return episodes;
			
			boolean transcode = true;
			NodeList children;
			String link, title, ext;	
			File currentDir = new File(path.localDirectory);
			File[] matchedFiles;
			SubtitleMatchingFilter filter;
			for (int i=0; i<items.getLength(); i++) {
				children = items.item(i).getChildNodes();
				// "title" (filename) is first child
				title = children.item(0).getTextContent();
				if (isVideoFile(title)) {
					// "res" (the link) is second child
					link = children.item(1).getTextContent();
					
					// (possibly) check for subtitles before transcoding
					if (trCheck && currentDir != null) {
						ext = title.substring( title.lastIndexOf('.')+1 );
						if (!trExtensions.contains(ext)) {
							// only bother with this if we don't already
							//	know to force transcoding by the filename
							filter = new SubtitleMatchingFilter(title);
							matchedFiles = currentDir.listFiles(filter);
							if (matchedFiles != null && matchedFiles.length == 0) 							
								transcode = false;
							matchedFiles = null;
						}
					}
					
					if (transcode) {
						// only make the link transcode if necessary
						link = link.replace("res_id=0", "res_id=none") + 
							"&pr_name="+trProfile+"&tr=1";
					}
					
					transcode = true; // reset for next time
					episodes.add(new BasicEpisode(title, link));					
				}
			}
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		
		Collections.sort(episodes);
		return episodes;
	}
	
	
	@Override
    protected Iterable<Path> getSubpathsFor(Path path) {
		
		// recurse into subdirectories
		LinkedList<Path> subpaths = new LinkedList<Path>();
		Document cont;
		try {
			cont = loadPage("containers&parent_id="+path.pathId);
			NodeList nodes = cont.getElementsByTagName("container");
			Node curr;
	        for (int i=0; i<nodes.getLength(); i++) {
	        	curr = nodes.item(i);

	    		// only extend the series name if this one has episodes in it
	    		String newName = path.localDirectory + File.separator + curr.getTextContent();        			
	    		String newId = curr.getAttributes().getNamedItem("id").getNodeValue();
	    		subpaths.add( new Path(newId, newName) );
	        }
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		
		return subpaths;
	}
	
	/**
	 * @return True if the MediaTomb is updating its database 
	 */
	public boolean isUpdating() {
		try {
			Document doc = loadPage("update&force_update=0");
			NodeList tasklist = doc.getElementsByTagName("task");
			return (tasklist.getLength() > 0);			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return false;
	}
	
	/**
	 * @param url, starting with the req_type=
	 * 	eg: "containers&parent_id=0"
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document loadPage(String url) throws SAXException, IOException {		
		DOMParser parser = new DOMParser();
		parser.parse("http://"+broadIp+":"+mtPort+"/content/interface?sid="+sid+"&req_type="+url);
		Document doc = parser.getDocument();
		return doc;
	}
	
	/**
	 * @return True on success, else false
	 */
	private boolean refreshSID() {
		// get an sid for mediatomb
		try {
			Document siddoc = loadPage("auth&checkSID=1");
			NodeList rootlist = siddoc.getElementsByTagName("root");
			Node rootNode = rootlist.item(0);			
			sid = rootNode.getAttributes().getNamedItem("sid").getNodeValue();			
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Could not connect to mediatomb server. Failed.");
//			System.exit(1);		
			Profile.getInstance().unselect();
			return false;
		} catch (NullPointerException e) {
			System.out.println(" * Could not retrieve a new SID.");
			return false;
		}
		
		return true;
	}

	@Override
	public void reload() {
		refreshSID();
		
		// wait until it's done updating
		if (isUpdating()) {
			System.out.print("EM: Waiting for MediaTomb to finish updating");
			STHandlerManager.getInstance().setStaticMessage(true);
			StaticMessageHandler.getInstance().setBody("Waiting for MediaTomb to finish updating...");
			do {
				doSleep(UPDATE_WAIT);
				System.out.print(".");
			} while (isUpdating());
			System.out.println(" Done.");
			StaticMessageHandler.getInstance().setBody();
			STHandlerManager.getInstance().setStaticMessage(false);
		}
	}
	
	private class SubtitleMatchingFilter implements FilenameFilter {
		
		private final String original;
		
		public SubtitleMatchingFilter(String original) {
			this.original = original.substring(0, original.lastIndexOf('.'));
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith(original) && AbstractManager.isSubtitleFile(name);
		}
		
	}

}
