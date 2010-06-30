package test1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class client {

	static Socket server;

	 

	public static void main(String[] args){
		while(true){

			try {
				server=new Socket(InetAddress.getLocalHost(),26781);
			
		
				BufferedReader in=new BufferedReader(new InputStreamReader(server.getInputStream()));
			
				PrintWriter out=new PrintWriter(server.getOutputStream());
			
				BufferedReader wt=new BufferedReader(new InputStreamReader(System.in));
			
				 
			
				while(true){
			
				String str=wt.readLine();
			
				out.println(str);
			
				out.flush();
			
				if(str.equals("end")){
			
				break;
			
				}
			
				System.out.println(in.readLine());
			
				}
			
				server.close();
			
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("sleep 2 s and then connect");
				try {
					Thread.currentThread().sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}				
				
			}
		}

	} 

}
