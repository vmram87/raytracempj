package test2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import test1.data;


public class SocketThread implements Runnable{
	private Socket client = null; 
	data d = null;
	Agent a =null;
	
	public SocketThread(Socket client, data d, Agent a){
		this.client = client;
		this.d=d;
		this.a=a;
	}

	
	@Override
	public void run() {
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream());
			String str="";
			while (true) {
				//System.out.println(telOp(1));
				System.out.println("before:"+str);
				str=in.readLine();
				System.out.println("d:"+d.get());
				System.out.println(str);
				
				
				out.println("has receive....");

				out.flush();

				if(str.equals("checkpoint")){
					//synchronized(w){
						a.setWait(true);
						//w.wait();
					//}
					
				}
				else if(str.equals("end")){
					a.setOver(true);
					a.setWait(true);					
					//break;
				}
			}
		}catch(Exception e){
			System.out.println(e.getStackTrace());
		}
		
	}

}
