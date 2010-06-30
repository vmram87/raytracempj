package org.raytrace.vector;

import org.raytrace.vector.impl.TPoint3D;


public class test {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Float a=new Float("1");
		Integer b = new Integer("23");
		TPoint3D p = new TPoint3D();
		Class.forName("org.raytrace.vector.impl.TPoint3D");
		
		
		System.out.println(p.getClass().getName());

	}

}
