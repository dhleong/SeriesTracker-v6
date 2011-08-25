package stv6.database;

import java.io.File;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import stv6.Profile;
import stv6.STClient;
import stv6.User;
import stv6.mysql.retrievers.SeriesRetriever;
import stv6.mysql.retrievers.TrackDataRetriever;
import stv6.mysql.retrievers.UserRetriever;
import stv6.series.BasicSeries;
import stv6.series.Series;
import stv6.series.SeriesList;
import stv6.series.TrackedSeries;
import stv6.sync.IdUpdateData;
import stv6.sync.SyncSettings;
import stv6.sync.SyncSettings.SyncPage;
import stv6.sync.TrackData;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

public class MysqlDatabase implements Database {
    
    /**
     * Max number of recent series to retrieve from a RECENT query
     */
    private static final int GET_RECENT_COUNT = 15;
	
	protected enum QueryType {
		SERIES_CREATE("INSERT INTO series (series_id, series_name, added) " +
						"VALUES (?, ?, ?)"), 
		SERIES_GET("SELECT series_id, series_name " +
						"FROM series ORDER BY series_name ASC"), 
		SERIES_GET_NEW("SELECT series_id, series_name " +
						"FROM series WHERE added > ? " +
						"ORDER BY series_id ASC"),
		SERIES_GET_NEXT_ID("SELECT MAX(series_id)+1 FROM series"),
		PROFILE_CREATE("INSERT INTO profiles " +
						"(profile_id, profile_name, file_name) " +
						"VALUES (?, ?, ?)"), 
		PROFILE_GET_NEXT_ID("SELECT MAX(profile_id)+1 FROM profiles"), 
		PROFILE_GET_FROM_FILE("SELECT profile_id, profile_name FROM profiles " +
						"WHERE file_name = ? LIMIT 1"),  
		PROFILE_SERIES_GET("SELECT series_id, series_name FROM (" +
						"SELECT series_id FROM profile_items WHERE profile_id = ?" +
						") AS _ NATURAL JOIN series"), 
		PROFILE_SERIES_ADD("INSERT INTO profile_items" +
						"(profile_id, series_id) " +
						"VALUES (?, ?)"), 
		/* We don't have the LIMIT clause, because sqlite doesn't seem to like it */
		PROFILE_SERIES_REMOVE("DELETE FROM profile_items " +
							"WHERE profile_id = ? and series_id = ? "),							
		SYNC_GET_NEW_TRACKS("SELECT series_id, user_id, episode, last_view " +
						"FROM tracks WHERE last_view > ? " +
						"ORDER BY series_id ASC"),
		SYNC_UPDATE_USER_IDS("UPDATE users SET user_id = ? WHERE user_id = ?"),
		SYNC_UPDATE_SERIES_IDS("UPDATE series SET series_id = ? WHERE series_id = ?"),
		SYNC_UPDATE_TRACK_BY_USER("UPDATE tracks SET user_id = ? WHERE user_id = ?"),
		SYNC_UPDATE_TRACK_BY_SERIES("UPDATE tracks SET series_id = ? WHERE series_id = ?"),
		TRACK_UPDATE("REPLACE INTO tracks " +
						"(series_id, user_id, episode, last_view) " +
						"VALUES (?, ?, ?, ?)"), 
		TRACK_GET("SELECT series_id, episode, last_view " +
						"FROM tracks WHERE user_id = ?"), 
		TRACK_GET_ONE("SELECT series_id, episode, last_view " +
						"FROM tracks WHERE user_id = ? AND series_id = ? " +
						"LIMIT 1"), 
		TRACK_GET_RECENT("SELECT series_id, episode, last_view " +
                        "FROM tracks WHERE user_id = ? " +
                        "ORDER BY last_view DESC " +
                        "LIMIT ?"),
		USER_GET("SELECT user_id, user_name " +
						"FROM users WHERE user_name LIKE ? " +
						"LIMIT 1"), 
		USER_GET_ALL("SELECT user_id, user_name FROM users " +
				"WHERE user_name <> '" + STClient.DEFAULT_USERNAME + "'"), 
		USER_GET_NEW("SELECT user_id, user_name FROM users " +
				"WHERE added > ? ORDER BY user_id ASC"),
		USER_GET_NEXT_ID("SELECT MAX(user_id)+1 FROM users"), 
		USER_CREATE("INSERT INTO users (user_id, user_name, added) " +
						"VALUES (?, ?, ?)");
		
		private final String sql;
		
		QueryType(String sql) {
			this.sql = sql;
		}
		
		public String getSql() {
			return sql;
		}
	}
	
	private static final int SYNC_TIME_SELECT = 0;
	private static final int SYNC_TIME_UPDATE = 1;
	private static final int SYNC_TIME_INIT = 2;
	
	private static final String[][] syncTimeQueries = {
	
		/** GET */
		{
			"SELECT last_get FROM sync WHERE sync_url = ? LIMIT 1",
			"UPDATE sync SET last_get = ? WHERE sync_url = ?",
			"INSERT INTO sync (`sync_url`, `last_get`) VALUES (?, ?)"
		},
		
		/** NEW */
		{
			"SELECT last_new FROM sync WHERE sync_url = ? LIMIT 1",
			"UPDATE sync SET last_new = ? WHERE sync_url = ?",
			"INSERT INTO sync (`sync_url`, `last_new`) VALUES (?, ?)"
		},
		
		/** TRACK */
		{
			"SELECT last_track FROM sync WHERE sync_url = ? LIMIT 1",
			"UPDATE sync SET last_track = ? WHERE sync_url = ?",
			"INSERT INTO sync (`sync_url`, `last_track`) VALUES (?, ?)"
		}
	};
	
	private static final String CONN_FORMAT = "jdbc:mysql://%s/%s?user=%s&password=%s";
	
	private final String host, db, user, pass;
	protected Connection conn;
	/*protected HashMap<QueryType, PreparedStatement> queries = 
		new HashMap<QueryType, PreparedStatement>();
		*/
	
	/*
	private HashMap<Integer, BasicSeries> seriesById;
	private LinkedList<BasicSeries> seriesOrdered;
	*/
	
	private int lastSeriesId = 0;
	
	public MysqlDatabase(String host, String db, String user, String pass) {
		this.host = host;
		this.db = db;
		this.user = user;
		this.pass = pass;
		
		/*
		seriesById = new HashMap<Integer, BasicSeries>();
		seriesOrdered = new LinkedList<BasicSeries>();
		*/
	}

	@Override
	public synchronized void addExistingSeries(int profileId, List<Series> toInsert) {
		if (toInsert.size() == 0)
			return;
		
		try {
			PreparedStatement stmt = prepare(QueryType.PROFILE_SERIES_ADD);
			
			for (Series s : toInsert) {
				stmt.setInt(1, profileId);
				stmt.setInt(2, s.getId());
				stmt.addBatch();
			}
			
			wrapBatch(stmt);
			
		} catch (BatchUpdateException e) {
			e.printStackTrace();
		      try {
		          conn.rollback();
		        } catch (Exception e2) {
		          e.printStackTrace();
		        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void addNewSeries(int profileId, List<Series> toCreate) {
		if (toCreate.size() == 0)
			return;
		
		try {			
			PreparedStatement createStmt = prepare(QueryType.SERIES_CREATE);
			PreparedStatement addStmt = prepare(QueryType.PROFILE_SERIES_ADD);
			
			long now = Profile.getNowSeconds();
			int id;
			for (Series s : toCreate) {
				id = createNewSeriesId();
							
				createStmt.setInt(1, id);
				createStmt.setString(2, s.getName());
				createStmt.setLong(3, now);
				createStmt.addBatch();

				if (profileId > -1) {
					addStmt.setInt(1, profileId);
					addStmt.setInt(2, id);
					addStmt.addBatch();
				}
			}
			
			if (profileId > -1)
				wrapBatch(createStmt, addStmt);
			else {
				addStmt.close();
				wrapBatch(createStmt);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean connectionIsValid() {
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `series` LIMIT 1");
			stmt.execute();
			stmt.close();
			return true;
		} catch (CommunicationsException e) {
			return false;
		} catch (SQLException e) {
			// ?
			return true;
		}
	}
	
	@Override
	public synchronized void createNewSeries(List<Series> toCreate) {
		addNewSeries(-1, toCreate);
	}

	@Override
	public int createNewSeriesId() {
		return lastSeriesId++;
	}
	
	/**
	 * Try to create our tables if they don't exist
	 */
	private void ensureSchema() {		 
		try {
			Statement stmt = conn.createStatement();			
			for (String s : getSchema())
				stmt.addBatch(s);
			
			wrapBatch(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Could not initialize DB!");
			System.exit(1);
		}
	}

	@Override
	public synchronized void fillSeries(SeriesList series, int profileId) {
		try {
			PreparedStatement stmt = prepare(QueryType.PROFILE_SERIES_GET);
			stmt.setInt(1, profileId);
			SeriesRetriever rs = new SeriesRetriever(this, stmt);
			while (rs.next()) 
				series.add(rs.get());
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}		
	}
	
	@Override
	public void getAllSeries(SeriesList list) {
		getAllSeries(list, true);
	}
	
	@Override
	public synchronized void getAllSeries(SeriesList list, boolean overwrite) {		
		try {
			PreparedStatement stmt = prepare(QueryType.SERIES_GET);
			SeriesRetriever rs = new SeriesRetriever(this, stmt);
			while (rs.next()) 
				list.add(rs.get(), overwrite);
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized SeriesList getAllSeriesAsUser(SeriesList list, User user) {
		SeriesList ret = new SeriesList();
		try {
			PreparedStatement stmt = prepare(QueryType.TRACK_GET);
			stmt.setInt(1, user.getId());
			ResultSet rs = wrapQuery(stmt);//.executeQuery();
			int id;
			BasicSeries original;
			HashSet<Integer> hasIds = new HashSet<Integer>(); // for ensuring we get everything
			while (rs.next()) {
				id = rs.getInt(1);
				if (list.contains(id)) {
					original = (BasicSeries) list.getById(id);
					hasIds.add(id);
					ret.add( new TrackedSeries(original, rs.getInt(2), rs.getLong(3)) );
				}
			}
			stmt.close();
			
			if (ret.size() < list.size()) {
				// we don't have tracks for all series; make them
				for (Series s : list) {
					if (!hasIds.contains(s.getId()))
						ret.add( new TrackedSeries((BasicSeries) s, -1, 0) );
				}
			}
			
			
		} catch (SQLException e) {			
			e.printStackTrace();
			System.err.println(" -> Couldn't fill tracks");
			System.exit(1);		
		}
		
		return ret;
	}

	@Override
	public synchronized void getAllUsers(List<User> list) {
		try {
			PreparedStatement stmt = prepare(QueryType.USER_GET_ALL);
			UserRetriever rs = new UserRetriever(this, stmt);
			rs.getAll(list);
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();			
		}
	}

	private Connection getConnection() throws SQLException {
		//try {			
		    return DriverManager.getConnection(getConnectionString());
		  
		    /*
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}

		return null;*/
	}

	/*
	@Override
	public Series getSeries(int seriesId) {
		return seriesById.get(seriesId);
	}*/
	
	protected String getConnectionString() {
		return String.format(CONN_FORMAT, host, db, user, pass);
	}
	
	/**
	 * Get the last sync time for the given sync page on the given SyncSettings
	 * 
	 * @param syncPage
	 */
	@Override
	public synchronized long getLastSyncFor(SyncSettings settings, SyncPage syncPage) {
		long time = 0;
		
		try {
			PreparedStatement stmt = prepare(syncTimeQueries[syncPage.ordinal()][SYNC_TIME_SELECT]);
			stmt.setString(1, settings.getBaseUrl());
			ResultSet rs = wrapQuery(stmt);//.executeQuery();
			
			if (rs.next())
				time = rs.getLong(1);
			
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return time;
	}
	
	/**
	 * If there is no existing id in the DB for this
	 * 	file, 
	 */
	@Override
	public synchronized int getProfileId(File file) {
		String fname = file.getName();
		int pos = fname.lastIndexOf(File.separatorChar);
		if (pos > -1)
			fname = fname.substring(pos);
		
		// try to just retrieve the ID from the db
		try {			
			PreparedStatement stmt = prepare(QueryType.PROFILE_GET_FROM_FILE);
			stmt.setString(1, fname);
			ResultSet rs = wrapQuery(stmt);//.executeQuery();
			int id = (rs.next()) ? rs.getInt(1) : -1;
			stmt.close();
			
			if (id > -1)
				return id;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// no good; create a new id and put it in the DB for future reference
		try {
			PreparedStatement stmt = prepare(QueryType.PROFILE_GET_NEXT_ID);
			ResultSet rs = wrapQuery(stmt);//.executeQuery();
			int profileId = -1;
			if (rs.next()) {
				profileId = Math.max(rs.getInt(1), 1);
				stmt.close();
				
				stmt = prepare(QueryType.PROFILE_CREATE);				
				stmt.setInt(1, profileId);
				stmt.setString(2, "");
				stmt.setString(3, fname);
				wrapUpdate(stmt);//.executeUpdate();
				stmt.close();

				return profileId;
			}
			
			throw new SQLException();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error: Could not create profile entry for: " + file.getName());
			System.exit(1);
		}
		
		// we should never get here
		return -1;
	}


	@Override
	public synchronized int getProfileId(String profileName) {
		// TODO
		return 1;
	}
	
	@Override
    public List<TrackedSeries> getRecentSeries(SeriesList list, User user) {
        List<TrackedSeries> ret = new LinkedList<TrackedSeries>();
        try {
            PreparedStatement stmt = prepare(QueryType.TRACK_GET_RECENT);
            stmt.setInt(1, user.getId());
            stmt.setInt(2, GET_RECENT_COUNT);
            ResultSet rs = wrapQuery(stmt);//.executeQuery();
            int id;
            BasicSeries original;
            HashSet<Integer> hasIds = new HashSet<Integer>(); // for ensuring we get everything
            while (rs.next()) {
                id = rs.getInt(1);
                if (list.contains(id)) {
                    original = (BasicSeries) list.getById(id);
                    hasIds.add(id);
                    ret.add( new TrackedSeries(original, rs.getInt(2), rs.getLong(3)) );
                }
            }
            stmt.close();
            
            // don't care? we only want recent...
            /*
            if (ret.size() < list.size()) {
                // we don't have tracks for all series; make them
                for (Series s : list) {
                    if (!hasIds.contains(s.getId()))
                        ret.add( new TrackedSeries((BasicSeries) s, -1, 0) );
                }
            }
            */
            
        } catch (SQLException e) {          
            e.printStackTrace();
            System.err.println(" -> Couldn't fill tracks");
            System.exit(1);     
        }
        
        return ret;
    }

    protected String[] getSchema() {
		return new String[] { 
				"CREATE TABLE IF NOT EXISTS `config` ("+
					  "`config_key` varchar(31) NOT NULL,"+
					  "`config_value` varchar(255) NOT NULL,"+
					  "PRIMARY KEY  (`config_key`)"+
					");",
			  
			  	"CREATE TABLE IF NOT EXISTS `profiles` ("+
					  "`profile_id` mediumint(9) NOT NULL,"+
					  "`profile_name` varchar(63) NOT NULL,"+
					  "`file_name` varchar(127) NOT NULL,"+
					  "PRIMARY KEY  (`profile_id`),"+
					  "KEY `file_name` (`file_name`)"+
					");", 
					
				"CREATE TABLE IF NOT EXISTS `profile_items` ("+
					  "`profile_id` mediumint(9) NOT NULL,"+
					  "`series_id` mediumint(9) NOT NULL,"+
					  "PRIMARY KEY  (`profile_id`,`series_id`)"+
					");",
					
				"CREATE TABLE IF NOT EXISTS `series` ("+
					  "`series_id` mediumint(9) NOT NULL,"+
					  "`series_name` varchar(255) NOT NULL,"+
					  "PRIMARY KEY  (`series_id`),"+
					  "KEY `series_name` (`series_name`)"+
					");",
					
				"CREATE TABLE IF NOT EXISTS `sync` ("+					  
					  "`sync_url` varchar(255) NOT NULL,"+
					  "`last_get` int(11) NOT NULL default '0' " +
					  	"COMMENT 'Last time we got new Series',"+
					  "`last_new` int(11) NOT NULL default '0' " +
					  	"COMMENT 'Last time we submitted new Series',"+
					  "`last_track` int(11) NOT NULL default '0' " +
					  	"COMMENT 'Last time we submitted tracking data',"+
					  "PRIMARY KEY  (`sync_url`)"+
					");",					
					
				"CREATE TABLE IF NOT EXISTS `tracks` ("+
					  "`user_id` mediumint(9) NOT NULL,"+
					  "`series_id` mediumint(9) NOT NULL,"+
					  "`episode` smallint(6) NOT NULL default '-1',"+
					  "`last_view` int(11) NOT NULL default '0',"+
					  "PRIMARY KEY  (`user_id`,`series_id`)"+
					");",		
					
				"CREATE TABLE IF NOT EXISTS `users` ("+
					  "`user_id` mediumint(9) NOT NULL,"+
					  "`user_name` varchar(31) NOT NULL,"+
					  "PRIMARY KEY  (`user_id`)"+
					");"
			};
	}
	
	@Override
	public synchronized TrackedSeries getSeriesAsUser(int seriesId, SeriesList list, User user) {
		if (!list.contains(seriesId))
			return null;
		
		try {
			PreparedStatement stmt = prepare(QueryType.TRACK_GET_ONE);
			stmt.setInt(1, user.getId());
			stmt.setInt(2, seriesId);
			ResultSet rs = wrapQuery(stmt);//.executeQuery();			
			
			BasicSeries original = (BasicSeries) list.getById(seriesId);
			
			if (rs.next()) {
				TrackedSeries ret = new TrackedSeries(original, rs.getInt(2), rs.getLong(3));
				stmt.close(); // make sure we've closed it!
				return ret;
			} else
				return new TrackedSeries(original, -1, 0);					
			
		} catch (SQLException e) {			
			e.printStackTrace();
			System.err.println(" -> Couldn't fill tracks");
			System.exit(1);		
		}
		
		return null;
	}

	/*
	@Override
	public Collection<BasicSeries> getSeries() {
		return seriesOrdered;
	}
	*/
	
	@Override
	public synchronized SeriesList getSyncNewSeries(SyncSettings settings) {
		SeriesList ret = new SeriesList();
		try {
			long lastSync = getLastSyncFor(settings, SyncPage.NEW);
			
			PreparedStatement stmt = prepare(QueryType.SERIES_GET_NEW);
			stmt.setLong(1, lastSync);
			//System.out.println( stmt.toString() );
			SeriesRetriever sr = new SeriesRetriever(this, stmt);			
			while (sr.next())
				ret.add(sr.get());
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
		return ret;
	}

	@Override
    public synchronized List<TrackData> getSyncNewTracks(SyncSettings syncSettings) {
		List<TrackData> ret = new LinkedList<TrackData>();
		try {
			PreparedStatement stmt = prepare(QueryType.SYNC_GET_NEW_TRACKS);
			stmt.setLong(1, getLastSyncFor(syncSettings, SyncPage.TRACK));
			TrackDataRetriever rs = new TrackDataRetriever(this, stmt);
			rs.getAll(ret);
			stmt.close();
		} catch (SQLException e) {			
			e.printStackTrace();
		}
		
		return ret;
	}

	@Override
	public synchronized List<User> getSyncNewUsers(SyncSettings settings) {
		List<User> ret = new LinkedList<User>();
		try {
			long lastSync = getLastSyncFor(settings, SyncPage.NEW);
			
			PreparedStatement stmt = prepare(QueryType.USER_GET_NEW);
			stmt.setLong(1, lastSync);
			//System.out.println( stmt.toString() );
			UserRetriever rs = new UserRetriever(this, stmt);			
			rs.getAll(ret);
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/*
	@Override
	public boolean hasSeries(int seriesId) {
		return seriesById.containsKey(seriesId);
	}

	@Override
	public boolean hasSeries(String folder) {
		for (Series s : seriesOrdered) {			
			if (s.getName().equalsIgnoreCase(folder)) 
				return true;		
		}		
		return false;
	}*/

	@Override
	public synchronized User getUser(String userName) {
		try {
			PreparedStatement stmt = prepare(QueryType.USER_GET);
			stmt.setString(1, userName);			
			UserRetriever rs = new UserRetriever(this, stmt);
			if (rs.next()) {
				User ret = rs.get();
				stmt.close();
				return ret;
			} else {
				// create the user
				stmt = prepare(QueryType.USER_GET_NEXT_ID);
				ResultSet rs2 = wrapQuery(stmt);//.executeQuery();

				int id = -1;
				if (rs2.next()) {
					id = Math.max(rs2.getInt(1), 1); // make sure it's at least 1
					stmt.close();
					
					User u = new User(id, userName);
					stmt = prepare(QueryType.USER_CREATE);
					stmt.setInt(1, id);
					stmt.setString(2, userName);
					stmt.setLong(3, Profile.getNowSeconds());
					wrapUpdate(stmt);//.executeUpdate();
					stmt.close();
					return u;
				}
				
				// just in case...
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();			
		} 
		
		return null;
	}
	
	private PreparedStatement prepare(String query) throws SQLException {
		if (!connectionIsValid()) {
			System.out.println("DB: Dead connection; reconnecting");
			reconnect();
		}
		
		return conn.prepareStatement(query);
	}
	
	private PreparedStatement prepare(QueryType type) throws SQLException {
		return prepare(type.getSql());
	}

	public synchronized void reconnect() {
		try {
			// it doesn't really matter if this fails
			try { conn.close(); } catch (Exception e) {}
			
			conn = getConnection();
			
			ensureSchema();
			
			//prepareStatements();			
		} catch (SQLException e) {
			System.err.println("Couldn't prepare MySQL connection");
			e.printStackTrace();
			System.exit(1);		
		}
	}

	@Override
	public synchronized void reload() {
		
		reconnect();
		
		try {
			PreparedStatement stmt = prepare(QueryType.SERIES_GET_NEXT_ID);
			ResultSet rs = wrapQuery(stmt);//.executeQuery();
			if (rs.next())
				lastSeriesId = rs.getInt(1);
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Error: Could not complete Mysql reload.");
			e.printStackTrace();
			System.exit(1);
		}
		
		/*
		seriesById.clear();
		seriesOrdered.clear();
		
		try {
			SeriesRetriever rs = new SeriesRetriever(prepare(QueryType.SERIES_GET));
			BasicSeries curr;
			while (rs.next()) {
				curr = rs.get();
				seriesById.put( curr.getId(), curr );
				seriesOrdered.add( curr );
				
	            if (curr.getId() > lastId )
	            	lastId = curr.getId();	
			}
			
			
		} catch (SQLException e) {			
			e.printStackTrace();
			System.err.println(" -> Couldn't reload MysqlDatabase");
			System.exit(1);		
		}
		*/
	}

	@Override
	public synchronized void removeSeries(int profileId, List<Series> toRemove) {
		if (toRemove.size() == 0)
			return;
		
		try {
			PreparedStatement stmt = prepare(QueryType.PROFILE_SERIES_REMOVE);
			
			for (Series s : toRemove) {
				stmt.setInt(1, profileId);
				stmt.setInt(2, s.getId());
				stmt.addBatch();
			}
			
			wrapBatch(stmt);
			
		} catch (BatchUpdateException e) {
			e.printStackTrace();
		      try {
		          conn.rollback();
		        } catch (Exception e2) {
		          e.printStackTrace();
		        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public synchronized void updateSeriesTracking(TrackedSeries s, User u) {
		updateSeriesTracking(
				new TrackData(s.getId(), u.getId(), s.getLastEpisode(), s.getLastView())
		);
	}
	
	@Override
	public synchronized void updateSeriesTracking(TrackData... data) {
		try {
			PreparedStatement stmt = prepare(QueryType.TRACK_UPDATE);
			
			for (TrackData d : data) {
				stmt.setInt(1, d.seriesId);
				stmt.setInt(2, d.userId);
				stmt.setInt(3, d.episode);
				stmt.setLong(4, d.lastView);
				stmt.addBatch();
			}
			
			// gogogo
			wrapBatch(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * @param idsTable Either "series" or "users"
	 */
	private synchronized void updateIds(QueryType idsTable, QueryType trackQry, 
			List<IdUpdateData> ids) {		
		try {		
			PreparedStatement idsStmt = prepare(idsTable);
			PreparedStatement tracksStmt = prepare(trackQry);
			
			// work backwards; this should gracefully prevent 
			//  duplicate id issues, since new ids will always
			//  be increasing...
			int oldId, newId;			
			for (int i=ids.size()-1; i>=0; i--) {			
				newId = ids.get(i).newId;
				oldId = ids.get(i).oldId;
				
				idsStmt.setInt(1, newId);
				idsStmt.setInt(2, oldId);
				idsStmt.addBatch();				
				//System.out.println(oldId + " => " + newId );
				
				tracksStmt.setInt(1, newId);
				tracksStmt.setInt(2, oldId);
				tracksStmt.addBatch();
				//trackswrapUpdate(stmt);//.executeUpdate();
			}
			
			wrapBatch(idsStmt, tracksStmt);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
	}

	@Override
	public synchronized void updateSeriesIds(List<IdUpdateData> ids) {
		/*
		updateIds(QueryType.SYNC_UPDATE_SERIES_IDS, QueryType.SYNC_UPDATE_TRACK_BY_SERIES, 
				oldIds, newIds);
		*/
		
		int startId = ids.get(ids.size()-1).oldId;
		if (lastSeriesId > startId)
			startId = createNewSeriesId();
		
		// first, direct oldIds to some unique spot; then, once there,
		//  we put them into their final destination
		List<IdUpdateData> oldToUnique = new ArrayList<IdUpdateData>();
		List<IdUpdateData> uniqueToNew = new ArrayList<IdUpdateData>();
		for (int i=0; i<ids.size(); i++, startId++) {
			oldToUnique.add(new IdUpdateData(ids.get(i).oldId, startId));
			uniqueToNew.add(new IdUpdateData(startId, ids.get(i).newId));
		}
		
		// holy crap update
		updateIds(QueryType.SYNC_UPDATE_SERIES_IDS, QueryType.SYNC_UPDATE_TRACK_BY_SERIES, oldToUnique);
		updateIds(QueryType.SYNC_UPDATE_SERIES_IDS, QueryType.SYNC_UPDATE_TRACK_BY_SERIES, uniqueToNew);
	}

	/**
	 * Set the last sync time for the given sync page on the given
	 * 	SyncSettings to "now"
	 * 
	 * @param syncPage
	 */
	@Override
	public synchronized void updateSyncTime(SyncSettings settings, SyncPage syncPage) {
		long now = Profile.getNowSeconds();
		
		try {
			PreparedStatement stmt = prepare(syncTimeQueries[syncPage.ordinal()][SYNC_TIME_UPDATE]);
			stmt.setLong(1, now);
			stmt.setString(2, settings.getBaseUrl());			
			int updates = wrapUpdate(stmt);//.executeUpdate();
			stmt.close();

			if (updates == 0) {
				// no existing entry; create it
				stmt = prepare(syncTimeQueries[syncPage.ordinal()][SYNC_TIME_INIT]);
				stmt.setString(1, settings.getBaseUrl());
				stmt.setLong(2, now);
				wrapUpdate(stmt);//executeUpdate();
				stmt.close();
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void updateUserIds(List<IdUpdateData> ids) {		
		updateIds(QueryType.SYNC_UPDATE_USER_IDS, QueryType.SYNC_UPDATE_TRACK_BY_USER, 
				ids);		
	}

	/**
	 * Wraps batch update statements with synchronization
	 * 	and cleans up by closing them
	 * @param stmt
	 * @throws SQLException 
	 */
	protected void wrapBatch(Statement... stmts) throws SQLException {
		synchronized(conn) {
			conn.setAutoCommit(false);
			try {
				for(Statement stmt : stmts) {
					stmt.executeBatch();
				}
			} catch (BatchUpdateException e) {
				conn.rollback();
				conn.setAutoCommit(true);
				return;
			} catch (CommunicationsException e) {
				/*
				System.err.println("DB: Dead connection; reconnecting");
				reconnect();
				wrapBatch(stmts);*/
				System.err.println("DB: Dead connection at wrapUpdate... This shouldn't happen!");
				return;
			}
			conn.commit();
			for(Statement stmt : stmts)
				stmt.close();
			conn.setAutoCommit(true);
		}
	}
	
	protected int wrapUpdate(PreparedStatement stmt) throws SQLException {
		try {
			return stmt.executeUpdate();
		} catch (CommunicationsException e) {
			/*
			System.err.println("DB: Dead connection; reconnecting");
			reconnect();
			return wrapUpdate(stmt);*/
			System.err.println("DB: Dead connection at wrapUpdate... This shouldn't happen!");
			return 0;
		}
	}
	
	public ResultSet wrapQuery(PreparedStatement stmt) throws SQLException {
		try {
			return stmt.executeQuery();
		} catch (CommunicationsException e) {
			/*
			 * DB validity-checking moved to #prepare()
			System.err.println("DB: Dead connection; reconnecting");
			
			reconnect();
			
			return wrapQuery(stmt);*/
			System.err.println("DB: Dead connection at wrapQuery... This shouldn't happen!");
			return null;
		}
	}	
}
