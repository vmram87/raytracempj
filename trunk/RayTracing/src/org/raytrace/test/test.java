package org.raytrace.test;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.algorithm.impl.TRayTrace;
import org.raytrace.scene.IScene;
import org.raytrace.scene.impl.TScene;
import org.raytrace.viewport.IViewPort;
import org.raytrace.viewport.impl.SequenceViewport;

public class test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/*
		IRayTraceAlgorithm alg=new TRayTrace();
		
		IScene scene = new TScene(alg);
		
		IViewPort viewport=new SequenceViewport(scene);
		
		viewport.init(args);
		try {
			viewport.configureFromFile("config07.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewport.render();
		viewport.saveToIMGFile("test.gif");*/
		//viewport.saveConfigToFile("09.xml");
		
		float[] a;
		a=new float [10];
		a=new float[11];
		a[2]=0;
		System.out.println(a[2]);

	}

}
