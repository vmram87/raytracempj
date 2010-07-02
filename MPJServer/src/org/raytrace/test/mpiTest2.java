package org.raytrace.test;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.algorithm.impl.TRayTrace;
import org.raytrace.scene.IScene;
import org.raytrace.scene.impl.TScene;
import org.raytrace.viewport.IViewPort;
import org.raytrace.viewport.impl.MPIViewport;

public class mpiTest2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		IRayTraceAlgorithm alg=new TRayTrace();
		
		IScene scene = new TScene(alg);
		
		IViewPort viewport=new MPIViewport(scene);
		
		viewport.init(args);
		try {
			viewport.configureFromFile("08.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewport.render();
		viewport.saveToIMGFile("test2.gif");
		viewport.viewportFinalize();
		//viewport.saveConfigToFile("09.xml");

	}

}
