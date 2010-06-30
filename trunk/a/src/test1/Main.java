package test1;

import be.ac.kuleuven.cs.ttm.ttm.*;

public class Main implements Runnable {

    public static Computation comp1, comp2 = null;

    public static Counter c1, c2 = null;

    private Main(){
    }

    public static Computation getComp(int i) {
	if (1 == i) {
	    return comp1;
	} else {
	    return comp2;
	}
    }

    public void run() {
	c1 = new Counter(1);
	c2 = new Counter(2);
	comp1 = Factory.getInstance().createComputation(c1);
	comp2 = Factory.getInstance().createComputation(c2);
	//comp1 = new Computation(c1);
	//comp2 = new Computation(c2);
	comp1.start();
	comp2.start();
	Scheduler.getInstance().start();
	try{
	Thread.currentThread().sleep(5000);
	} catch (Exception e) {}
    }

    public static void main (String[] args) {
	Main m = new Main();
	Thread t = new Thread(m);
	t.start();
	try {
	    Thread.currentThread().sleep(2000);
	    System.out.println("requesting to stop Counter 1");
	    c1.waitforit();
	    Thread.currentThread().sleep(2000);
	    System.out.println("requesting to stop Counter 2");
	    c2.waitforit();
	    Thread.currentThread().sleep(3000);
	    System.out.println("resuming Counter 1");
	    comp1.resume();
	    Thread.currentThread().sleep(4000);
	    System.out.println("resuming Counter 2");
	    comp2.resume();
	    Thread.currentThread().sleep(3000);
	    System.out.println("requesting to stop Counter 2");
	    c2.waitforit();
	    Thread.currentThread().sleep(3000);
	    System.out.println("requesting to stop Counter 1");
	    c1.waitforit();
	    Thread.currentThread().sleep(5000);
	    System.out.println("stopping execution");
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
