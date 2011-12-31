package stv6.http.pools;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import stv6.http.Client;

/**
 * A client pool should allow for multiple, threaded
 * 	clients, without the risk of over-running Java's
 * 	thread limit. Because, really, we probably don't
 * 	need a separate thread for each client
 * 
 * For now, we'll be lazy
 * 
 * @author dhleong
 *
 */
public class ClientPool implements ClientList {
    
    /**
     * If we don't get data within 15 seconds, timeout
     */
	public static final long TIMEOUT_DELAY = 15000;

    private final class ClientThread implements Runnable {

	    private final boolean mRunning = true;
		public String name;

        @Override
        public void run() {
            while (mRunning) {
                try {
                    mClientsWaiting.acquire();
                } catch (final InterruptedException e) {
                    // we shouldn't typically get interrupted,
                    //  so probably we just want to stop
                    break;
                }
                
                if (DEBUG) System.out.println("+ " + name);
                Client current = mClients.removeFirst();
                if (DEBUG) System.out.println("= " + name);
                
                // let someone else work until we're ready
                long timeout = System.currentTimeMillis() + TIMEOUT_DELAY;
                while (!current.isReady() && System.currentTimeMillis() < timeout) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {}
                }
                if (!current.isReady()) {
                    // timed out
                    current.timeout();
                    if (DEBUG) System.out.println("> " + name);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                
                if (DEBUG) System.out.println("~ " + name);
                
                // process 
                current.process(name);
                if (DEBUG) System.out.println("- " + name);
                
                Thread.yield();
            }
        }

    }

    /**
	 * Max # of kids before reorganizing
	 */
	public static final int DEFAULT_THREADS = 8;
    public static final boolean DEBUG = false;
    
    private final ClientThread[] mThreads;
    
    private final Semaphore mClientsWaiting = new Semaphore(0);
    private final LinkedList<Client> mClients = new LinkedList<Client>();
	
	public ClientPool() {
	    this(DEFAULT_THREADS);
	}
	
	public ClientPool(int threadCount) {
	    mThreads = new ClientThread[threadCount];

        for (int i=0; i<threadCount; i++) {
            mThreads[i] = new ClientThread();
            // go ahead and start the thread
            mThreads[i].name = "ClientPoolThread-" + i;
            final Thread t = new Thread(mThreads[i], mThreads[i].name);
            t.setDaemon(true); // daemon thread please
            t.start(); // gogogo
        }
	}
	
	@Override
    public boolean add(Client c) {
	    mClients.addFirst(c);

        // v-up
        mClientsWaiting.release();
        return true;
	}

}
