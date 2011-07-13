package stv6.episodes;

public interface Episode {

	/**
	 * The episode "id"; in other words, the index
	 * 	into the array of episodes for the series
	 * 	where this is stored
	 * @param id
	 */
	public abstract int getId();

	/**
	 * @return The link to actually view
	 */
	public abstract String getLink();

	public abstract String getTitle();
	
	/**
	 * @see Episode#getId()
	 * @param id
	 */
	public abstract void setId(int id);

}