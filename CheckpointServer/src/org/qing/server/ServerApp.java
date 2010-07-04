package org.qing.server;

public class ServerApp {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ServerThread server = new ServerThread();
		
		server.init(args);
		
		server.waitToEnd();
		

	}

}
