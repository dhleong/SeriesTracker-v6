package stv6.http.pools;

import java.util.Iterator;
import java.util.LinkedList;

import stv6.http.Client;

public class ParentList implements ClientList, Client {
	
	private LinkedList<ClientPool> children;
	
	public ParentList( ChildList list ) {
		children = new LinkedList<ClientPool>();
		children.add( new ClientPool(list) );
		
		// theoretically, list.size() == MAX_CHILDREN, 
		//  so let's go ahead and make a new empty pool
		children.add( new ClientPool() );
	}

	@Override
	public boolean add(Client newClient) {
		ClientPool pool = (ClientPool) children.getLast();
		if (pool.size() < ClientPool.MAX_CHILDREN)
			pool.add(newClient);
		else {
			System.out.println("New ClientPool");
			ClientPool newPool = new ClientPool();
			newPool.add(newClient);
			children.add(newPool);
		}
		
		// always return true;
		return true;
	}
	
	public boolean isDead() {
		return children.size() == 0;
	}

	@Override
	public int size() {
		return children.size();
	}

	@Override
	public Iterator<Client> iterator() {
		return new TestIterator(this); //new ParentsIterator( this );
	}
	
	public void process() {
		// nop
	}
	
	private static class TestIterator implements Iterator<Client> {
		private Iterator<ClientPool> iter;
		
		public TestIterator(ParentList list) {
			iter = list.children.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Client next() {
			return iter.next();
		}

		@Override
		public void remove() {
			iter.remove();
		}
		
	}	

}
