package stv6.episodes.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import stv6.episodes.BasicEpisode;

/**
 * Note: This is totally untested!
 * @author dhleong
 *
 */
public class TversityManager extends AbstractManager {
	
	private String broadcastingIp;
	private int tvPort;
	
	public TversityManager(String broadcastingIp, int tvPort) {
		this.broadcastingIp = broadcastingIp;
		this.tvPort = tvPort;
	}

	@Override
	protected List<Path> getBasePaths() {
		List<Path> paths = new LinkedList<Path>();
        
		try {
            Document doc = getTversityPage("/mediasource/fetchlist?type=folder");

            NodeList nodes = doc.getElementsByTagName("result");
            
            for (int i=0; i<nodes.getLength(); i++) {
            	Node item = nodes.item(i);
            	paths.add(new Path(
            		item.getAttributes().getNamedItem("url").getNodeValue(),
            		item.getAttributes().getNamedItem("id").getNodeValue()
            	));
            }
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(1);
		}		
		
		return paths;
	}

	@Override
	protected ArrayList<BasicEpisode> getEpisodesFor(Path path) {
		ArrayList<BasicEpisode> episodes = new ArrayList<BasicEpisode>();
        try {        	
			Document doc = getTversityPage("/medialib/browse?id="+path.pathId+"&format=rss&count=0");
            NodeList nodes = doc.getElementsByTagName("item");
            for (int i=0; i<nodes.getLength(); i++) {
            	Node item = nodes.item(i);
            	// right now, tversity publishes the "title" in the first child,
            	//  and the "link" in the last child. Hopefully this will stay
            	//  the same!
            	String name = item.getFirstChild().getTextContent();
            	//String guid = item.getLastChild().getPreviousSibling().getTextContent();
            	String link = item.getLastChild().getTextContent();

            	if (link.indexOf("medialib/browse") == -1)
            			episodes.add(new BasicEpisode(name, link));
            }					
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return episodes;
	}

	@Override
	protected Iterable<Path> getSubpathsFor(Path path) {
		LinkedList<Path> subpaths = new LinkedList<Path>();
        try {        	
        	Document doc = getTversityPage("/medialib/browse?id="+path.pathId+"&format=rss&count=0");
            NodeList nodes = doc.getElementsByTagName("item");
            for (int i=0; i<nodes.getLength(); i++) {
            	Node item = nodes.item(i);
            	// right now, tversity publishes the "title" in the first child,
            	//  and the "link" in the last child. Hopefully this will stay
            	//  the same!
            	String name = item.getFirstChild().getTextContent();
            	String guid = item.getLastChild().getPreviousSibling().getTextContent();
            	String link = item.getLastChild().getTextContent();

            	// act on folders
            	if (link.indexOf("medialib/browse") > -1) 	            	            	
	            	subpaths.add(new Path(path.localDirectory + File.separator + name, guid));
	            	         	
            }					
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param url, ex: "/medialib/browse"
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document getTversityPage(String url) throws SAXException, IOException {
		if (url.charAt(0) != '/')
			url = "/" + url;
		
		DOMParser parser = new DOMParser();
		parser.parse("http://"+broadcastingIp+":"+tvPort+url);
		Document doc = parser.getDocument();
		return doc;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

}
