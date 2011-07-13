package stv6.http;

public interface Client {

	/**
	 * @return True if we're done and can be cleaned
	 */
	public boolean isDead();

	/**
	 * Read the request, handle it
	 */
	public void process();
}
