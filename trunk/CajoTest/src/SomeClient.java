import gnu.cajo.invoke.Remote;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class SomeClient {
	
   public static void main(String args[]) throws Exception {
      HashMap<String, Object> machineObjectMap = new HashMap<String, Object> ();
    
    try{
      FileReader rdFile = new FileReader("machines");
      BufferedReader brdFile = new BufferedReader(rdFile);


      String strLine;
      String connectpreFix = "//";
      String connectpostFix = ":1198/someName";

      while ((strLine = brdFile.readLine()) != null) {
    	  strLine = strLine.trim();
    	  if(strLine.equals(""))
    		  continue;

    	  try{
    		  Object o = Remote.getItem(connectpreFix + strLine + connectpostFix); 
    		  machineObjectMap.put(strLine, o);
    	  }
    	  catch(Exception e){
    		  System.out.println("machine " + strLine + " not connected!");
    	  }
      }

      brdFile.close();

    }

    catch (IOException e) {

      e.printStackTrace();

    }
    
      

      String s = "";
      
      InputStreamReader input = new InputStreamReader(System.in);
      BufferedReader reader = new BufferedReader(input);
      
      
      while(true){
	      try{
	    	  s = reader.readLine();
	      }
	      catch(Exception e){}
	      
	      s = s.trim();
	      
	      if(s.equals(""))
	    	  continue;
	      
	      if(s.equals("exit"))
	    	  break;
	      
	      String[] p = s.split(" ");
	      if(p.length != 2){
	    	  System.out.println("command error!");
	    	  continue;
	      }
	      
	      try{
		      Object tempObj = null;
		      if(!p[1].equals("all")){
		    	  tempObj = machineObjectMap.get(p[1]);
		    	  Remote.invoke(tempObj, p[0], null);
		      }
		      else{
		    	  Iterator it = machineObjectMap.entrySet().iterator();
		    	  while(it.hasNext()){
		    		  Entry entry = (Entry) it.next();
		    		  tempObj = entry.getValue();
		    		  Remote.invoke(tempObj, p[0], null);
		    	  }
		      }
	      }catch(Exception e){
	    	  e.printStackTrace();
	    	  continue;
	      }
	    	  
      }
      
      
      
      //Remote.shutdown();
   }
}

