
import java.io.IOException;
import java.sql.Timestamp;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

public class SomeObject {
	int t;
	
	public SomeObject() throws Exception{
		bind();
		t=1;
	}
	
	public void test(byte[] arr){
		byte[] a = new byte [arr.length];
		for(int i = 0; i < arr.length; i++){
			a[i] = arr[i];
			//System.out.print(a[i]);		
		}
		System.out.println(new Timestamp(System.currentTimeMillis()));
	}
	
   protected String string = "this thing";
   public String foo() {
      System.out.println("foo method called!");
      return string;
   }
   public String baz(String string) {
      System.out.println("baz method called!");
      try { return this.string; }
      finally { this.string = string; }
   }
   
   public void bind() throws IOException{
	   Remote.config(null, 1198, null, 0);
	   ItemServer.bind(this, "someName");
	   ItemServer.bind(this, "someName2");
   }
   
   public static void main(String args[]) throws Exception {
	   SomeObject obj = new SomeObject(); 
      
      System.out.println("The server is running!");
   }
}
