package test2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

import be.ac.kuleuven.cs.ttm.ttm.Scheduler;

public class Main {
	static Agent agentObj;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		FileInputStream fin = new FileInputStream("serial2");
		ObjectInputStream ois = new ObjectInputStream(fin);
		agentObj = (Agent)ois.readObject();
		
		agentObj.resume();
		Scheduler.getInstance().start();
	}

}
