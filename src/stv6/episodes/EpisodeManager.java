package stv6.episodes;

import stv6.Reloadable;
import stv6.series.SeriesList;

public interface EpisodeManager extends Reloadable {
	
	/**
	 * Not to be confused with {@link #getAvailableSeries(SeriesList)},
	 * 	which only UPDATES a given list
	 * 
	 * @return A list of all Series that we can find 
	 */
	public SeriesList getAvailableSeries();

	/**
	 * Update the Series in the given SeriesList with
	 * 	local path and available episodes. (Calls Series#manageify()
	 * 	on each series found)
	 * @param list
	 */
	public void getAvailableSeries(SeriesList list);

}
