package stv6.sync;

import java.io.IOException;
import java.net.UnknownHostException;

public class Synchronizer {
	
	private static final Synchronizer instance_ = new Synchronizer();
	
	private Synchronizer() {
		
	}

	/**
	 * @param settings
	 * @return False if we should stop the reloading process,
	 * 	else true
	 * @throws IOException 
	 * @throws UnknownHostException If there's no internet
	 */
	public boolean synchronize(SyncSettings settings) throws UnknownHostException, IOException {
		/* first, block for blocking sync actions, then
		 * 	use non-blocking APIs so everything else can continue
		 *  while we do non-blocking stuff in the background
		 */		
		if (!SyncNewHandler.handle(settings))
			return false;

		// awesome; now start up async stuff
		SyncGetHandler.handle(settings); // get new series
		
		SyncTrackHandler.handle(settings); // synchronize tracking data
		
		return true;
	}
	
	public static Synchronizer getInstance() {
		return instance_;
	}
}
