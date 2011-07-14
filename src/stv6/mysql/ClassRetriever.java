package stv6.mysql;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;

import stv6.database.MysqlDatabase;

public class ClassRetriever<T> {	
	private final ResultSet rs;
	
	private Constructor<?> constructor;
	private Object[] initArgs;
	
	public ClassRetriever(MysqlDatabase db, PreparedStatement stmt, Class<T> asClass) throws SQLException {		
		this.rs = db.wrapQuery(stmt);//.executeQuery();
		
		Constructor<?>[] constructors = asClass.getConstructors();
	
		if (constructors.length > 1) {
			// multiple constructors... looked for one marked with
			//	DatabaseConstructor annotation
			for (Constructor<?> c : constructors) {
				if (c.isAnnotationPresent(DatabaseConstructor.class)) {
					constructor = c;
					break;
				}
			}
			
			// sad face
			if (constructor == null)
				throw new RuntimeException("Class " + asClass.getCanonicalName() 
						+ " has no DatabaseConstructor");
		} else {
		
			constructor = asClass.getConstructors()[0];
		}
		
		initArgs = new Object[ constructor.getParameterTypes().length ];		
	}
	
	public void close() {
		try {
			rs.close();
		} catch (SQLException e) {
		}
	}
	
	public boolean next() {
		try {
			return rs.next();
		} catch (SQLException e) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public T get() throws SQLException {
		
		for (int i=0, j=1; i<initArgs.length; i++, j++) 
			initArgs[i] = rs.getObject(j);
		
		try {
			return (T) constructor.newInstance(initArgs);			
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {}
		return null;
	}
	
	public List<T> getAll() throws SQLException {
		LinkedList<T> list = new LinkedList<T>();
		while (next()) 
			list.add(get());
		close();
		return list;
	}
	
	public void getAll(List<T> list) throws SQLException {		
		while (next()) 
			list.add(get());
		close();
	}
	
	/*
	public static <T> ClassRetriever<T> fromResultSet(ResultSet rs, Class<T> asClass) {
		return new ClassRetriever;		
	}
	*/
	
}
