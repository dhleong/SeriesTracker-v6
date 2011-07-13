package stv6.episodes;

import stv6.templating.TemplateObject;

public class SeriesEpisode implements TemplateObject, Episode{
	public static final String CLASS_NAME = "episode";
	
	private final BasicEpisode base;
	private final int seriesId;
	
	public SeriesEpisode(BasicEpisode base, int seriesId) {
		this.base = base;
		this.seriesId = seriesId;
	}
	
	public SeriesEpisode(String title, String link, int seriesId) {
		base = new BasicEpisode(title, link);
		base.setId(-1);
		this.seriesId = seriesId;
	}

	public int compareTo(Episode o) {
		return base.compareTo(o);
	}
	
	public boolean equals(Object obj) {
		return base.equals(obj);
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}
	
	public int getId() {
		return base.getId();
	}
	
	public String getLink() {		
		return base.getLink();
	}

	/**
	 * @return The link for the "view" page
	 * 	on ST without saving
	 */
	public String getNoSaveLink() {
		return "view?id="+seriesId+"&amp;ep="+getId();
	}
	
	public String getTitle() {
		return base.getTitle();
	}

	/**
	 * @return The link for the "view" page
	 * 	on ST with saving
	 */
	public String getSaveLink() {
		return "view?id="+seriesId+"&amp;ep="+getId()+"&amp;save=1";
	}
	
	public void setId(int id) {
		base.setId(id);
	}
}
