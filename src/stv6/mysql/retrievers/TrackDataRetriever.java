package stv6.mysql.retrievers;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import stv6.database.MysqlDatabase;
import stv6.mysql.ClassRetriever;
import stv6.sync.TrackData;

public class TrackDataRetriever extends ClassRetriever<TrackData> {

	public TrackDataRetriever(MysqlDatabase db, PreparedStatement stmt)
			throws SQLException {
		super(db, stmt, TrackData.class);
	}

}
