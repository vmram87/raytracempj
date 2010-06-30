package test1;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import be.ac.kuleuven.cs.ttm.ttm.Scheduler;

public class Counter implements Runnable, Serializable {

    

    private boolean wait=false;
    private String str;
    private boolean over=false;
    //volatile Object w = new Object();
    int id;
    
    
    
    public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

	public boolean isWait() {
		return wait;
	}

	public void setWait(boolean wait) {
		this.wait = wait;
	}

	private int telOp(int a) {
	try {
	if (wait) {
	    Scheduler.getInstance().currentComputation().yield(true);
	    wait = false;
	}
        a = a + 1;
	} catch (Exception e) {
	  e.printStackTrace();
	  System.exit(1);
	}
	return a;
    }

    public void waitforit() {
	wait = true;
    }

    public Counter(int ident) {
      id=ident;
    }

    public void run() {
		System.out.println("Started the counter.run()");
		Wrapper w = new Wrapper (this);
    }
    
   
    
    
    
}
