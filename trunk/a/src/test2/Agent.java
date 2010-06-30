package test2;

import be.ac.kuleuven.cs.ttm.ttm.*;
import java.util.*;

public class Agent implements Runnable, java.io.Serializable {
  Computation task;
  Vector sites;
  boolean wait =false;
  boolean over=false;
  
  
  int i=0;

  public Agent() {
    sites = new Vector();
    sites.addElement("altavista");
    sites.addElement("yahoo");
    task = Factory.getInstance().createComputation(this);
    task.start();
  }

  private void move(String destination) {
    Migrator.move(this, destination);
    Scheduler.getInstance().currentComputation().yield(true);
    wait = false;
  }

  public void resume() {
    task.resume();
  }
  
 


  public boolean isWait() {
	return wait;
}

public void setWait(boolean wait) {
	this.wait = wait;
}

public boolean isOver() {
	return over;
}

public void setOver(boolean over) {
	this.over = over;
}

public void run() {
	System.out.println("Start");
    
      String currentLocation = (String)sites.elementAt(0);  
      
      Wrapper w = new Wrapper(this);
      while(!over){
    	  
    	  if(wait){
    		  if(over)
    			  break;
    		  System.out.println("Have a look at " + i  + "...");
    		  move(new Integer(i++).toString());
    	      System.out.println("return at "+i);
    	      //System.out.println("w.hellor at "+w.hello());
    	  }
      }
      
      
    
    
    System.out.println("Stop");
  }
}
    











