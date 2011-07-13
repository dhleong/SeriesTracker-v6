package stv6.sync;

import java.io.IOException;
import java.net.UnknownHostException;

import stv6.http.HttpRequestor;

public class SyncSettings {
	public static enum SyncPage {
		/**
		 * Get new Series that we don't have locally
		 */
		GET("get.php"),
		
		/**
		 * Sending new series/users to the server to get
		 * 	new IDs and such.
		 * 
		 * This should probably be a blocking event
		 */
		NEW("new.php"),
		
		/**
		 * Sending/receive tracking data to/from the server
		 */
		TRACK("track.php"),
		
		/**
		 * Pull a user from the server for local reference
		 * 
		 * NOT an automatic event
		 */
		RETRIEVE("retrieve.php");
		
		
		private final String fileName;
		private SyncPage(String fileName) {
			this.fileName = fileName;
		}
		
		public String getFileName() {
			return fileName;
		}
	}
	
	private final String baseUrl, password;
	
	public SyncSettings(String baseUrl, String password) {
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl+"/";
		this.password = password;
	}

	public String getBaseUrl() {
		return baseUrl;
	}
	
	/**
	 * @return Encrypted version of the password
	 */
	public String getPassword() {
		return password;
	}
	
	protected HttpRequestor getRequestorFor(SyncPage page) throws UnknownHostException, IOException {
		return HttpRequestor.post(getUrl(page));
	}
	
	public String getUrl(SyncPage page) {
		return baseUrl + page.getFileName();
	}
	
	public boolean isUsingPassword() {
		return password != null;
	}
}
