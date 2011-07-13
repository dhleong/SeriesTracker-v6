package stv6.mysql.retrievers;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import stv6.database.MysqlDatabase;
import stv6.mysql.ClassRetriever;
import stv6.series.BasicSeries;

public class SeriesRetriever extends ClassRetriever<BasicSeries> {

	public SeriesRetriever(MysqlDatabase db, PreparedStatement stmt)
			throws SQLException {
		super(db, stmt, BasicSeries.class);
	}

}
