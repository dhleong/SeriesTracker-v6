package stv6.database;

import java.io.File;
import java.util.List;

import stv6.Reloadable;
import stv6.User;
import stv6.series.Series;
import stv6.series.SeriesList;
import stv6.series.TrackedSeries;
import stv6.sync.IdUpdateData;
import stv6.sync.SyncSettings;
import stv6.sync.SyncSettings.SyncPage;
import stv6.sync.TrackData;

public interface Database extends Reloadable {
	
	public void addExistingSeries(int profileId, List<Series> toInsert);
	
	/**
	 * Create the given series and add them to the profile with id profileId
	 * 
	 * @param profileId
	 * @param toCreate
	 */
	public void addNewSeries(int profileId, List<Series> toCreate);

	/**
	 * Simply create the given series
	 * 
	 * @param toCreate
	 */
	public void createNewSeries(List<Series> toCreate);

	public int createNewSeriesId();
	
	/**
	 * Fill the SeriesList with all series belonging to the
	 * 	specified profile
	 * 
	 * @param series
	 * @param profileId
	 */
	public void fillSeries(SeriesList series, int profileId);

	/**
	 * Get the DB id for a profile with the name
	 * @param string
	 * @return
	 */
	public int getProfileId(String profileName);

	/**
	 * Get the DB id for a profile stored in the file
	 * 
	 * @param file
	 * @return
	 */
	public int getProfileId(File file);

	/**
	 * Fills the SeriesList of every series available in the DB
	 */
	public void getAllSeries(SeriesList list);

	/**
	 * @param list
	 * @param overwrite @see SeriesList#add(stv5.series.Series, boolean) 
	 */
	public void getAllSeries(SeriesList list, boolean overwrite);

	/**
	 * Take the given series list and return a copy, where
	 * 	each has been wrapped by a TrackedSeries for the specified user
	 * 
	 * Note: This does NOT get all series stored in the
	 * 	DB. That doesn't even make sense!
	 * 
	 * At least, for now...
	 * 
	 * @param series
	 * @param user
	 * @return
	 */
	public SeriesList getAllSeriesAsUser(SeriesList series, User user);

	public TrackedSeries getSeriesAsUser(int seriesId, SeriesList series, User user);

	public void getAllUsers(List<User> possibleUsers);
	
	/**
	 * @param settings
	 * @return A list of newly created series that need to be sent to
	 * 	the server for synchronization
	 */
	public SeriesList getSyncNewSeries(SyncSettings settings);

	/**
	 * @param syncSettings
	 * @return A list of TrackData to be synchronized with the server.
	 * 	MUST be sorted in ascending order by series ID
	 */
	public List<TrackData> getSyncNewTracks(SyncSettings syncSettings);
	
	/**
	 * 
	 * @param settings
	 * @return A list of newly created users that need to be sent to
	 * 	the server for synchronization
	 */
	public List<User> getSyncNewUsers(SyncSettings settings);


	public long getLastSyncFor(SyncSettings settings, SyncPage syncPage);
	
	/**
	 * Get a list of recent series for the given user
	 * @param user
	 * @return
	 */
	public List<TrackedSeries> getRecentSeries(SeriesList list, User user);

    /**
	 * Retrieve the User with the given name. If it doesn't
	 * 	exist, create it.
	 * 
	 * @param userName
	 * @return
	 */
	public User getUser(String userName);
	
	/*
	public boolean hasSeries(int seriesId);

	public boolean hasSeries(String folder);
	*/


	public void removeSeries(int profileId, List<Series> toRemove);
	
	/**
	 * Replace the old values in the database for the series 
	 * 	represented by s with the new values it contains. 
	 * 
	 * @param s
	 */
	public void updateSeriesTracking(TrackedSeries s, User u);
	
	/**
	 * Update lots of Tracking data at once; for use with synchronization
	 * @param data
	 */
	public void updateSeriesTracking(TrackData... data);

	/**
	 * 
	 * @param oldIds
	 * @param newIds
	 */
	public void updateSeriesIds(List<IdUpdateData> ids);

	/**
	 * Set the last sync time for the given sync page on the given
	 * 	SyncSettings to "now"
	 * 
	 * @param syncPage
	 */
	public void updateSyncTime(SyncSettings settings, SyncPage syncPage);
	
	public void updateUserIds(List<IdUpdateData> ids);

}
