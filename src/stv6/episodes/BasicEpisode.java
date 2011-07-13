package stv6.episodes;

import com.eekboom.utils.Strings;

public class BasicEpisode implements Comparable<Episode>, Episode {
	private int id;
	private final String title, link;

	public BasicEpisode(String title, String link) {
		this.title = title;
		this.link = link;
	}

	@Override
	public int compareTo(Episode o) {
		// TODO: more "natural" comparison
		//return title.compareTo(o.getTitle());

		return Strings.compareNaturalIgnoreCaseAscii(getTitle(), o.getTitle());
	}
	
	/* (non-Javadoc)
	 * @see stv5.episodes.Episode#getId()
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see stv5.episodes.Episode#getLink()
	 */
	public String getLink() {
		return link;
	}	
	
	/* (non-Javadoc)
	 * @see stv5.episodes.Episode#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

}
