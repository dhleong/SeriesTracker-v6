package stv6.episodes.managers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import stv6.episodes.BasicEpisode;
import stv6.episodes.EpisodeManager;

public class FileSystemManager extends AbstractManager implements EpisodeManager {

	private final VideoFileFilter myFileFilter = new VideoFileFilter();
	private final FolderFilter myFolderFilter = new FolderFilter();
	
	private final String folderInfo;
	private final List<Path> localFolders = new LinkedList<Path>();

	/**
	 * The only constructor. It is designed to have its
	 * 	argument passed directly from the commandline 
	 * 
	 * @param localfolders A string representing either
	 * 	a ";" separated list of root folders wherein
	 * 	the series' folders are located, or a filename
	 * 	which gives this list 
	 */
	public FileSystemManager(String folderInfo) {
		this.folderInfo = folderInfo;
	}	

	@Override
	protected List<Path> getBasePaths() {
		return localFolders;
	}

	@Override
	protected ArrayList<BasicEpisode> getEpisodesFor(Path path) {
		ArrayList<BasicEpisode> eps = new ArrayList<BasicEpisode>();
		File[] videos = new File(path.localDirectory).listFiles(myFileFilter);
		if (videos != null && videos.length > 0) {
			for (File f : videos) 
				eps.add(new BasicEpisode(f.getName(), f.getAbsolutePath()));
		}
		return eps;
	}

	@Override
	protected Iterable<Path> getSubpathsFor(Path path) {
		LinkedList<Path> paths = new LinkedList<Path>();
		
		File[] subfolders = new File(path.localDirectory).listFiles(myFolderFilter);
		if (subfolders != null && subfolders.length > 0) {
			for (File subdir : subfolders) 			
				paths.add( new Path(subdir.getAbsolutePath()) );
		}
		
		return paths;
	}


	@Override
	public void reload() {
		loadFolderInfo(folderInfo, localFolders);
	}
	
   
    
    private static void addLocalFolders(List<Path> localFolders, String...paths) {
        localFolders.clear();
        
        for (String path : paths) 
            addLocalFolder( localFolders, path );     
    }
    
    private static void addLocalFolders(List<Path> localFolders, List<String> paths) {
        localFolders.clear();
        
        for (String path : paths) 
            addLocalFolder( localFolders, path );     
    }
    
    /**
     * Attempts to add "path" to the list of local folders;
     *  Ignores the path if invalid or could not be found
     * 
     * If path matches "@NAME/..", we assume it's a Windows
     *  system and attempt to find an external harddrive
     *  whose label matches NAME
     * @param path
     */
    private static void addLocalFolder(List<Path> localFolders, String path) {
        if (path.charAt(0) == '@') {
            int end = path.indexOf( File.separatorChar );
            if (end < 0)
                return;
            
            String needle = path.substring(1, end);
            String actualPath = path.substring(end+1);
            
            File[] roots = File.listRoots();
            FileSystemView v = FileSystemView.getFileSystemView();
            String label;
            for (File f : roots) {
                label = v.getSystemDisplayName(f);
                if (label.startsWith(needle)) {
                    localFolders.add( new Path(f.getAbsolutePath() + actualPath) );
                    break;
                }
            }
        } else {
            // just add it
            localFolders.add( new Path(path) );
        }
    }
	   
    protected static void loadFolderInfo(String folderInfo, List<Path> localFolders) {
        // load the local folders list
        if (folderInfo.contains(File.pathSeparator)) {
            // it's definitely a list
            String[] paths = folderInfo.split(File.pathSeparator);
            addLocalFolders( localFolders, paths );
        } else {
            File cfgFile = new File(folderInfo);
            if (!cfgFile.exists() && folderInfo.charAt(0) != '@') {
                System.out.println(" - No folders provided; Failed.");
                System.err.println("---------- DEBUG ----------");
                System.err.println(" FolderInfo: " + folderInfo);
                System.err.println(" file = " + cfgFile);
                if (cfgFile != null) {
                    System.err.println(" file.path = " + cfgFile.getAbsolutePath());
                    System.err.println(" file.exists = " + cfgFile.exists());
                    System.err.println(" file.canRead = " + cfgFile.canRead());
                    System.err.println(" file.isDirectory = " + cfgFile.isDirectory());
                }
                System.err.println("---------- DEBUG ----------");
                System.exit(1);
            } 
                
            if (cfgFile.isFile()) {
                // it's file; read line by line
                ArrayList<String> folders = new ArrayList<String>();
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                            new DataInputStream(
                                    new FileInputStream(cfgFile.getCanonicalFile())
                                    ))
                            );
                    String line;
                    while ((line=reader.readLine()) != null)
                        folders.add(line);
                    
                    addLocalFolders( localFolders, folders );
                } catch (IOException e) {
                    System.out.println(" - Could not read folders from file " 
                            + folderInfo);
                    System.exit(1);
                }
            } else {
                // it's a single folder
                addLocalFolders( localFolders, folderInfo );
            }
        } 
    }
	
	private class VideoFileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String filename) {
			return (AbstractManager.isVideoFile(filename));			
		}
	}

	private class FolderFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
		
	}
	
}
