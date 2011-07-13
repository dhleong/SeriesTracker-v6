package stv6.mysql.retrievers;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import stv6.User;
import stv6.database.MysqlDatabase;
import stv6.mysql.ClassRetriever;

public class UserRetriever extends ClassRetriever<User> {
	public UserRetriever(MysqlDatabase db, PreparedStatement stmt)
			throws SQLException {
		super(db, stmt, User.class);
	}
}
