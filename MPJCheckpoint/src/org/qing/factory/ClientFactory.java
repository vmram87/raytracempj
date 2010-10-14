package org.qing.factory;

import runtime.starter.MPJRun;

public class ClientFactory {
	private static MPJRun client = null;
	private ClientFactory(){}
	private static boolean canStart = true;
	private static Object lock = new Object();
	
	public static void initClient(String[] args) throws Exception{
		synchronized (lock){
			client = new MPJRun(args);
			canStart = false;
		}
	}
	
	public static synchronized MPJRun getClient(){
		return client;
	}
	
	public static  boolean isCanStarted(){
		synchronized (lock){
			return canStart;
		}
		
	}

	public static void setCanStart(boolean canStart) {
		synchronized(lock){
			ClientFactory.canStart = canStart;
		}
		
	}
	
}
