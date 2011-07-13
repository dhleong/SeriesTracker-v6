package stv6;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import stv6.database.Database;
import stv6.database.SqliteDatabase;
import stv6.http.HttpRequestor;
import stv6.http.HttpResponseCallback;
import stv6.http.request.Request;
import stv6.http.request.Response;
import stv6.http.request.variables.VariableList;
import stv6.sync.IdUpdateData;

public class Test {
	public static final void main(String[] args) {
		test3();
	}
	
	static void test1() {
		try {
			PrintWriter pw = new PrintWriter(System.out);
			HttpRequestor req = HttpRequestor.post("http://localhost/tests/st_sync/new.php");
			Request r = req.getRequest();
			VariableList vl = r.getPostVars();
			vl.put("sn[]", "Beautiful Life");			
			vl.put("si[]", "15");
			vl.put("sn[]", "Chuno");
			vl.put("si[]", "1");
			
			vl.put("un[]", "Daniel");
			vl.put("ui[]", "2");
			
			r.writeHeaders(pw);
			r.writeBody(pw);
			System.out.println("---");
			req.request(new HttpResponseCallback() {

				@Override
				public void callback(Response r) {
					System.out.println(r.getBody());
				}
				
			});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void test2() {

		Database db = new SqliteDatabase("test.db");
		db.reload();
		
		Thread f = new Thread(new test2_class(db));
		
		f.start();
	}
	
	private static class test2_class implements Runnable {
		Database db;
		private test2_class(Database db) {
			this.db = db;
		}
		
		@Override
		public void run() {
			
			List<IdUpdateData> ids = new ArrayList<IdUpdateData>();

			/*
			ids.add(new IdUpdateData(1, 2));
			ids.add(new IdUpdateData(2, 3));
			ids.add(new IdUpdateData(3, 4));
			ids.add(new IdUpdateData(4, 5));		
			//*/
			
			//*
			ids.add(new IdUpdateData(5, 4));
			ids.add(new IdUpdateData(4, 3));
			ids.add(new IdUpdateData(3, 2));
			ids.add(new IdUpdateData(2, 1));
			//*/
			
			db.updateUserIds(ids);
				
		}
	}
	
	private static void test3() {
		Profile.getInstance().initialize(new String[0]);
		//Profile.getInstance().reload();
		Profile.getInstance().loadFromFile(new File("profiles/test.profile"));
		Profile.getInstance().reload();
	}
	
}
