package test1;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import be.ac.kuleuven.cs.ttm.ttm.Scheduler;

public class Wrapper implements Serializable {
	public Wrapper(Counter c){
		ServerSocket server =null;
		int id=1;
		try {
			server= new ServerSocket(26781);
			
			Socket client= server.accept();
		
			//clientThread =new Thread(socketThread);
			data d = new data();
			Thread clientThread =new Thread(new SocketThread(client,d,c));
			clientThread.start();
			//Factory.getInstance().createComputation(clientThread).start();
			while (!c.isOver()) {
				if (c.isWait()) {
					//clientThread.suspend();
				    Scheduler.getInstance().currentComputation().yield(true);
				    //synchronized(w){
				    	System.out.println("return from resume");
					    c.setWait(false);
					    
					   // w.notify();
				    //}
				    //clientThread.resume();
				}
			}
			System.out.println("id in parent thread:"+id);
			
			client.close();
		    
			
	    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    
	}
}
