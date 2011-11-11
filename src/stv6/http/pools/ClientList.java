package stv6.http.pools;

import stv6.http.Client;

public interface ClientList {//extends Iterable<Client> {
	
	/**
	 * @param c
	 * @return True if successfully added, else false
	 * 	if there's no room
	 */
	public boolean add(Client c);
	
//	public int size();
}
