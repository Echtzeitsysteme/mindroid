package org.mindroid.api.endpoint;

public interface ClientEndpoint {
	/**
	 * Connect to server
	 */
	public void connect();
	
	/**
	 * checks if client is ready 
	 * returns true if connection is established.
	 * @return
	 */
	public boolean isClientReady();
	
	/**
	 * Stops the client
	 */
	public void stop();
}
