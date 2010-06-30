package test2;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import be.ac.kuleuven.cs.ttm.ttm.Computation;
import be.ac.kuleuven.cs.ttm.ttm.Factory;
import be.ac.kuleuven.cs.ttm.ttm.Scheduler;

public class Migrator implements Runnable {
  
  Job job;
  Computation computation;
  
  static class Job {
    Agent agent;
    String destination;

    public Job(Agent agent, String destination) {
      this.agent = agent;
      this.destination = destination;
    }
  }

  public Migrator(Agent agent, String destination) {
    this.job = new Job(agent, destination);
    computation = Factory.getInstance().createComputation(this);
    computation.start();
  }

  static public void move(Agent agent, String destination) {
    new Migrator(agent, destination);
  }

  public void run() {
    System.out.println("Moving agent to " + job.destination);
    try {
      //ByteArrayOutputStream bos  = new ByteArrayOutputStream();
    	FileOutputStream fos = new FileOutputStream ("serial2");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(job.agent);
      //byte[] bytes = bos.toByteArray();

      //ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      //ObjectInputStream ois = new ObjectInputStream(bis);
      //jobagent = (Agent)ois.readObject();
      job.agent.resume();
    }
    catch(Exception ex) {
      System.out.println(ex);
    }
  }
  

  public static void main(String args[]) {
    new Agent();
    Scheduler.getInstance().start();
  }
}
    


  
