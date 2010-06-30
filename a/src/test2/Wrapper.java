package test2;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import test1.data;
import be.ac.kuleuven.cs.ttm.ttm.Scheduler;

public class Wrapper implements Serializable {
	data d = new data();
	
	public Wrapper(Agent a){
		ServerSocket server =null;
		int id=1;
		try {
			server= new ServerSocket(26781);
			
			Socket client= server.accept();
		
			//clientThread =new Thread(socketThread);
			
			Thread clientThread =new Thread(new SocketThread(client,d,a));
			clientThread.start();
			//Factory.getInstance().createComputation(clientThread).start();
		    
			
	    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    
	}
	
	public String hello(){
		return new Integer(d.get()).toString();
	}
}
