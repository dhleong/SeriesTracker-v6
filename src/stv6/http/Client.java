package stv6.http;

public interface Client {

	/**
	 * Basically an alias for Socket#ready()
	 * @return True if our socket is ready to be read
	 */
	public boolean isReady();

	/**
	 * Read the request, handle it
	 * @param name TODO
	 */
	public void process(String name);

	/**
	 * Mark the client as timed out. Implementations should
	 * close any connections as politely as possible
	 */
    public void timeout();
}
