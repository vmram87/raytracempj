package org.raytrace.test;

import runtime.starter.MPJRun;

public class deamonMPITest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args){
		 try {
			 String[] params={"-np","2","-dev","niodev","org.raytrace.test.mpiTest"};
		      MPJRun client = new MPJRun(params);
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		    }
	}

}
