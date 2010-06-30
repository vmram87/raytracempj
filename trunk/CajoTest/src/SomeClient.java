import gnu.cajo.invoke.Remote;

import java.sql.Timestamp;

public class SomeClient {
   public static void main(String args[]) throws Exception {
      Object object = Remote.getItem("//localhost:1198/someName2");
      byte[] arr = new byte [100000];
      for(int i=0;i<arr.length;i++)
    	  arr[i] = (byte)i;
      
      System.out.println(new Timestamp(System.currentTimeMillis()));
      
      Remote.invoke(object, "test", arr);
      
      System.out.println(new Timestamp(System.currentTimeMillis()));
   }
}

