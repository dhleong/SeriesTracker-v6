package stv6.http.pools;

import java.util.Iterator;
import java.util.LinkedList;

import stv6.http.Client;

public class ChildList implements ClientList {
	
	private LinkedList<Client> children;
	
	public ChildList() {
		children = new LinkedList<Client>();
	}

	@Override
	public boolean add(Client c) {
		if (size() < ClientPool.MAX_CHILDREN) {
			// just go ahead and add
			children.addLast(c);
			return true;
		} 

		return false;
	}
	
	@Override
	public Iterator<Client> iterator() {
		return children.iterator();
	}
		
	public int size() {
		return children.size();
	}


}
