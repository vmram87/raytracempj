package org.qing.factory;

import runtime.starter.MPJRun;

public class ClientFactory {
	private static MPJRun client = null;
	private ClientFactory(){}
	
	public static void initClient(String[] args) throws Exception{
		client = new MPJRun(args);
	}
	
	public static synchronized MPJRun getClient(){
		return client;
	}
	
}
