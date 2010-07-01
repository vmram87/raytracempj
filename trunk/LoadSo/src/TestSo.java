import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TestSo {
	
	static{
		System.loadLibrary("blcr");
	}
	
	public native  void setCallBack();
	public native  int checkpoint();
	
	public TestSo(){
		(new Thread(customThread)).start();
	}
	
	public CustomSemaphore s = new CustomSemaphore(1);
	ServerSocket server =null;
	Socket client = null;
	
	public void preProcess(){
		//try {
		//	s.acquire();
			System.out.println("acquire in pre process");
			try {
				
				Thread.currentThread().sleep(5000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				System.out.println("close socket!");
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("checkpoint wait end!");
			
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//}
		
		System.out.println("pre checkpoint !");
	}
	
	public void proceeRestart(){
		System.out.println("restart!");
		System.out.println("leaving checkpoint");
		//s.signal();
	}
	
	public void processContinue(){
		System.out.println("continue!");
		System.out.println("leaving checkpoint");
		//s.signal();
	}
	

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		System.loadLibrary("cr");
		
		TestSo t=new TestSo();
		t.setCallBack();
		
		int i=0;
		while (true){
			if(i==3){
				t.checkpoint();
			}
			
			if(i==5){
				t.checkpoint();
			}
			
			System.out.println("i:"+i++);
			Thread.currentThread().sleep(3000);
			
		}
	}
	
	Runnable customThread = new Runnable() {

		@Override
		public void run() {
			int id=100;
			try {
				server= new ServerSocket(26781);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
				
			while(true){
			
			try {
							
				client= server.accept();
		
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out = new PrintWriter(client.getOutputStream());
				String str="";
				while (true) {
					//System.out.println(telOp(1));
					System.out.println("before:"+str);
					str=in.readLine();
					System.out.println("id:"+id++);
					System.out.println(str);
					
					
					out.println("has receive....");
	
					out.flush();
		
				
				}
			
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("listen again!");
			}
			
			}
		
		}
	
		
	};
	
	
	class CustomSemaphore {

	    private int s ;
	    
	    public CustomSemaphore(int s) {
	      this.s = s ;
	    }
	    
	    public synchronized void acquire() throws InterruptedException {
	      while (s == 0) wait(0) ;
	      s-- ;
	    }
	    
	    public synchronized void signal() {
	      s++ ;
	      notify() ;
	    }
	  }	
	

}
