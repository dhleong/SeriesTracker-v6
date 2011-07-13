package stv6.http.pools;

import java.util.Iterator;

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
public class ClientPool implements Runnable, Client, ClientList {
	/**
	 * Max # of kids before reorganizing
	 */
	public static final int MAX_CHILDREN = 63;
	
	private ClientList children;
	private boolean isParent = false; // if true, that means all children are actually pools
	
	private Thread thisThread;
	
	private static int ID = 0;
	public int id;
	
	public ClientPool() {
		children = new ChildList();
		thisThread = new Thread(this);
		thisThread.start();
		
		id = ++ID;
	}
	
	public ClientPool(ChildList kids) {
		//children = new ParentList( kids );
		children = kids;
		thisThread = new Thread(this);
		thisThread.start();
		
		id = ++ID;
	}
	
	public boolean add(Client c) {
		synchronized(children) {
			boolean added = children.add(c);
			if (added)
				return true;
			else {
				// make a parent?
				children = new ParentList( (ChildList) children );
				children.add(c);
				isParent = true;
				return false;
			} 
		}
	}

	/**
	 * If this is true, it means all of the clients we had are done
	 */
	@Override
	public boolean isDead() {
		synchronized(children) {
			return !isParent && size() == 0;
		}
	}

	@Override
	public Iterator<Client> iterator() {
		return children.iterator();
	}
	
	/**
	 * 
	 */
	@Override
	public void process() {
		
	}


	@Override
	public void run() {
		while (thisThread == Thread.currentThread()) {
			if (isParent && size() == 0) {
				// revert from being a parent
				isParent = false;
				children = new ChildList();
			}
			
			synchronized( children ) {
				Iterator<Client> kids = children.iterator();
				Client current;
				while(kids.hasNext()) {
					current = kids.next();
					
					// if it's dead, prune it
					if (current.isDead()) {
						kids.remove();
						continue;
					}
					
					// process 
					current.process();
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public int size() {
		synchronized(children) {
			return children.size();
		}
	}
}
