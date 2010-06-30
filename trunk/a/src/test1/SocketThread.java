package test1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketThread implements Runnable{
	private Socket client = null; 
	data d = null;
	Counter c =null;
	
	public SocketThread(Socket client, data d, Counter c){
		this.client = client;
		this.d=d;
		this.c=c;
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
						c.setWait(true);
						//w.wait();
					//}
					
				}
				else if(str.equals("end")){
					c.setWait(true);
					c.setOver(true);
					break;
				}
			}
		}catch(Exception e){
			System.out.println(e.getStackTrace());
		}
		
	}

}
