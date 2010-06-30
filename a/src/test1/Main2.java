package test1;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import be.ac.kuleuven.cs.ttm.ttm.Computation;
import be.ac.kuleuven.cs.ttm.ttm.Factory;
import be.ac.kuleuven.cs.ttm.ttm.Scheduler;

public class Main2 implements Runnable {

	public static Computation comp1, comp2 = null;

    public static Counter s = null;

    private Main2(){
    }

  

    public void run() {
	s = new Counter(1);
	comp1 = Factory.getInstance().createComputation(s);
	//comp1 = new Computation(c1);
	//comp2 = new Computation(c2);
	comp1.start();
	Scheduler.getInstance().start();
	try{
	Thread.currentThread().sleep(5000);
	} catch (Exception e) {}
    }

    public static void main (String[] args) {
	Main2 m = new Main2();
	Thread t = new Thread(m);
	t.start();
	
	try {
		Thread.currentThread().sleep(1000);
		while(!s.isOver()){
		    while(!s.isWait()){}
		    
		    if(!s.isOver()){
		    	
		    	ByteArrayOutputStream bos  = new ByteArrayOutputStream();
		    	FileOutputStream fos =  new FileOutputStream("serial");
			      ObjectOutputStream oos = new ObjectOutputStream(fos);
			      oos.writeObject(comp1);
			      byte[] bytes = bos.toByteArray();
			      
			      
			    System.out.println("sleep 50s");
			    Thread.currentThread().sleep(5000);
			    
			    System.out.println("begin resume");
			      //ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			      //ObjectInputStream ois = new ObjectInputStream(bis);
			      //comp2 = (Computation)ois.readObject();
			      //s.setOver(true);
			      comp1.resume();
			      
			    //comp1.resume();
			    Thread.currentThread().sleep(10000);
			    
			    System.out.println("resume complete");
		    }
		}
		
		comp2.resume();
		System.out.println("sleep 10 s");
		Thread.currentThread().sleep(10000);
		
	    
	    
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

}
