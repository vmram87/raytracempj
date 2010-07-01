import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
		pId = getPID();
		userName = System.getProperty("user.name");
		url1 = "/tmp/hsperfdata_"+userName+"/"+pId;
		url2 = "./"+pId;
		(new Thread(customThread)).start();
	}
	
	public CustomSemaphore s = new CustomSemaphore(1);
	ServerSocket server =null;
	Socket client = null;
	private String pId = null;
	private String userName = null;
    // src directory
    static String url1 = null;
    // dst directory
    static String url2 = "./";
	
	public void preProcess(){
		//try {
		//	s.acquire();
			System.out.println("acquire in pre process");
			try {
				
				Thread.currentThread().sleep(3000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("checkpoint wait end!");
			
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//}
			try {
				File src = new File(url1);
				File dst = new File(url2);
				if(dst.exists())
					dst.createNewFile();
				
				copyFile(src, dst);
			} catch (IOException e) {
				System.out.print("pre process: Exception in copy file!");
				e.printStackTrace();
			}
			
		
		System.out.println("pre checkpoint !");
	}
	
	public void processRestart(){		
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
				server= new ServerSocket(26782);
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
					if(str!=null){
						System.out.println("id:"+id++);
						System.out.println(str);
						
						
						out.println("has receive....");
		
						out.flush();
					}
		
				
				}
			
			} catch (IOException e) {
				e.printStackTrace();
				try {
					server= new ServerSocket(26782);
				} catch (IOException e2) {
					System.out.println("restart server bind fail!, may already bound");
					e2.printStackTrace();
				}
				System.out.println("listen again!");
			}
			
			}
		
		}
	
		
	};
	
	public static String getPID() {
	    String processName =
	        java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	    return processName.split("@")[0];

	}
	
	public static void copyFile(File sourceFile,File targetFile) 
	throws IOException{
        // buffer the input stream
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff=new BufferedInputStream(input);
 
        //buffer the output stream
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff=new BufferedOutputStream(output);
        
        //buffer array
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len =inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        
        outBuff.flush();
        
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    } 
	
	
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
