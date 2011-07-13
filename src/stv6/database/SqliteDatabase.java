package stv6.database;

public class SqliteDatabase extends MysqlDatabase {
	
	private static final String CONN_FORMAT = "jdbc:sqlite:%s";
	
	private final String dbfile;
	
	public SqliteDatabase(String dbfile) {
		super(null, null, null, null); // blah
		
		this.dbfile = dbfile;
	}
	
	@Override
	protected String getConnectionString() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return String.format(CONN_FORMAT, dbfile);
	}
	
	/**
	 * Sqlite doesn't like the KEY (column) syntax,
	 * 	and Mysql doesn't like CREATE INDEX IF NOT EXISTS...
	 */
	@Override
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
					  "PRIMARY KEY  (`profile_id`)"+
					  //"KEY `file_name` (`file_name`)"+
					");", 
					
				"CREATE INDEX IF NOT EXISTS `idx_file_name` ON `profiles` (`file_name`);",
					
				"CREATE TABLE IF NOT EXISTS `profile_items` ("+
					  "`profile_id` mediumint(9) NOT NULL,"+
					  "`series_id` mediumint(9) NOT NULL,"+
					  "PRIMARY KEY  (`profile_id`,`series_id`)"+
					");",
					
				"CREATE TABLE IF NOT EXISTS `series` ("+
					  "`series_id` mediumint(9) NOT NULL,"+
					  "`series_name` varchar(255) NOT NULL,"+
					  "`added` int(11) NOT NULL,"+
					  "PRIMARY KEY  (`series_id`)"+
					  //"KEY `series_name` (`series_name`)"+
					");",				
				"CREATE INDEX IF NOT EXISTS `idx_series_name` ON `series` (`series_name`);",
				"CREATE INDEX IF NOT EXISTS `idx_added` ON `series` (`added`);",
								
				"CREATE TABLE IF NOT EXISTS `sync` ("+					  
				  "`sync_url` varchar(255) NOT NULL,"+
				  "`last_get` int(11) NOT NULL default '0',"+
				  "`last_new` int(11) NOT NULL default '0',"+
				  "`last_track` int(11) NOT NULL default '0',"+
				  "PRIMARY KEY  (`sync_url`)"+
				");",
					
				"CREATE TABLE IF NOT EXISTS `tracks` ("+
					  "`series_id` mediumint(9) NOT NULL,"+
					  "`user_id` mediumint(9) NOT NULL,"+
					  "`episode` smallint(6) NOT NULL default '-1',"+
					  "`last_view` int(11) NOT NULL default '0',"+
					  "PRIMARY KEY  (`series_id`,`user_id`)"+
					");",		
					
				"CREATE TABLE IF NOT EXISTS `users` ("+
					  "`user_id` mediumint(9) NOT NULL,"+
					  "`user_name` varchar(31) NOT NULL,"+
					  "`added` int(11) NOT NULL,"+
					  "PRIMARY KEY  (`user_id`)"+
					");",
					
				"CREATE INDEX IF NOT EXISTS `idx_added` ON `users` (`added`);",
					  
			};
	}
}
