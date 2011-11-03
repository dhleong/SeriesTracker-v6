package stv6.series;

import java.io.File;
import java.util.ArrayList;

import stv6.episodes.BasicEpisode;
import stv6.handlers.util.CoverHandler;
import stv6.mysql.DatabaseConstructor;
import stv6.templating.TemplateObject;

public class BasicSeries implements TemplateObject, Series {
	
	/**
	 * If the id == NO_ID, it means it has none in the DB yet
	 */
	public static final int NO_ID = -1;
	
	protected int id;
	protected final String name;
	
	private boolean managed = true;

	protected String localPath;
	protected ArrayList<BasicEpisode> episodes; 
	
	@DatabaseConstructor
	public BasicSeries(int id, String name) {
		this(id, name, false);
	}
	
	public BasicSeries(int id, String name, boolean managed) {
		this.id = id;
		this.name = name;
		this.managed = managed;
	}


	/* (non-Javadoc)
	 * @see stv5.ISeries#compareTo(stv5.Series)
	 */
	@Override
	public int compareTo(Series o) {
		return name.compareToIgnoreCase(o.getName());
	}

	/* (non-Javadoc)
	 * @see stv5.ISeries#getClassName()
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}
	
	/* (non-Javadoc)
	 * @see stv5.ISeries#getId()
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see stv5.ISeries#getLink()
	 */
	public String getLink() {
		return "browse?id=" + id;
	}
	
	public String getLocalPath() {		
		return localPath;
	}
	
	/* (non-Javadoc)
	 * @see stv5.ISeries#getName()
	 */
	public String getName() {
		return name;
	}
	
	public boolean hasCover() {
		if (localPath == null)
			return false;
		
		File localFile = new File(localPath);
		
		File[] files = localFile.listFiles(CoverHandler.FILENAME_FILTER);
		if (files == null || files.length == 0) {
			return false;
		}
		
		File theFile = files[0]; // pick first candidate
		return theFile.exists();
	}
	
	/* (non-Javadoc)
	 * @see stv5.ISeries#isManaged()
	 */
	public boolean isManaged() {
		return managed;
	}

	/* (non-Javadoc)
	 * @see stv5.ISeries#manageify(java.lang.String, java.util.ArrayList)
	 */
	public void manageify(String localPath, ArrayList<BasicEpisode> eps) {
		this.localPath = localPath;
		this.episodes = eps;
		managed = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see stv5.series.Series#setId(int)
	 */	
	public void setId(int newId) {
		this.id = newId;
	}

}
