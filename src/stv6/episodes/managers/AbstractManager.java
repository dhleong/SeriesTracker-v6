package stv6.episodes.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stv6.episodes.BasicEpisode;
import stv6.episodes.EpisodeManager;
import stv6.series.BasicSeries;
import stv6.series.SeriesList;

public abstract class AbstractManager implements EpisodeManager {
	
	public static String capitalizeWords(String str) {
		String[] words = str.split("\\s");
		String firstLetter = null;
		StringBuffer newname = new StringBuffer();
		for (int i=0; i<words.length; i++) {						
			try {				
				firstLetter = words[i].substring(0, 1);
				newname.append( words[i].replaceFirst(firstLetter, firstLetter.toUpperCase()) );
			} catch (Exception e) {
				// the first letter was... not a letter. Go ahead and just add
				newname.append(words[i]);
			}
			
			// add the space we split on back in
			if (i < words.length-1) newname.append( ' ' );				
		}	
		
		return newname.toString();
	}
	
	@Override
	public SeriesList getAvailableSeries() {
		SeriesList list = new SeriesList();
		for (Path p : getBasePaths())
			spider(list, p, true);
		
		return list;
	}
	
	@Override
	public void getAvailableSeries(SeriesList list) {
		for (Path p : getBasePaths())
			spider(list, p, false);
	}
	
	/**
	 * @return the base Path from which to start the spider
	 */
	protected abstract List<Path> getBasePaths();
	
	/**
	 * @param path
	 * @return An ArrayList of episodes available at the given Path. Don't
	 * 	bother sorting explicitly; we'll do it here
	 */
	protected abstract ArrayList<BasicEpisode> getEpisodesFor(Path path);
	
	protected abstract Iterable<Path> getSubpathsFor(Path path);

	public static boolean isVideoFile(String filename) {
		return filename.endsWith(".avi")
				|| filename.endsWith(".ogm")
				|| filename.endsWith(".mkv")
				|| filename.endsWith(".mpg")
				|| filename.endsWith(".mpeg")
				|| filename.endsWith(".mp4");
	}

	public static boolean isSubtitleFile(String filename) {
		return filename.endsWith(".srt")
				|| filename.endsWith(".ass")
				|| filename.endsWith(".sub")
				|| filename.endsWith(".txt");
	}
	
	/**
	 * @param list
	 * @param path
	 * @param createNew If True, then we'll add series that aren't
	 * 	already in the list instead of just manage-ifying them
	 */
	private void spider(SeriesList list, Path path, boolean createNew) {
		// check for episodes in this folder
		ArrayList<BasicEpisode> eps = getEpisodesFor(path);		
		if (!eps.isEmpty()) {
			Collections.sort(eps);
			// ugly? yes... oh well
			for (int i=0; i<eps.size(); i++)
				eps.get(i).setId(i);
			
			// we don't want the path/name to end with a separator; take care of it
			int end = path.localDirectory.length();			
			if (path.localDirectory.charAt(end-1) == File.separatorChar)
				end--;
			
			int namePos = path.localDirectory.lastIndexOf( File.separatorChar, end-1 ) + 1;			
			String name = capitalizeWords( path.localDirectory.substring(namePos,end) );
			if (list.contains(name)) 
				list.getByName(name).manageify(path.localDirectory, eps);	
			else if (createNew) {
				BasicSeries s = new BasicSeries(BasicSeries.NO_ID, name);
				s.manageify(path.localDirectory, eps);
				list.add(s);
			}
		}
		
		// spider through sub folders
		for (Path p : getSubpathsFor(path)) {
			spider(list, p, createNew);
		}
	}
}
