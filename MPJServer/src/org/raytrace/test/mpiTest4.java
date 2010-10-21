package org.raytrace.test;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.algorithm.impl.NRayTraceAlg;
import org.raytrace.scene.IScene;
import org.raytrace.scene.impl.TScene;
import org.raytrace.viewport.IViewPort;
import org.raytrace.viewport.impl.SequenceViewport;

public class mpiTest4 {
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		IRayTraceAlgorithm alg=new NRayTraceAlg();
		//IRayTraceAlgorithm alg=new TRayTrace();
		
		IScene scene = new TScene(alg);
		
		IViewPort viewport=new SequenceViewport(scene);
		
		String param="3 mpj.conf niodev single";
		String[] params=param.split(" ");
		viewport.init(params);
		try {
			viewport.configureFromFile("08.xml");
			//viewport.configureFromObjFile("block.obj");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewport.render();
		System.out.println("Finish render!");
		viewport.saveToIMGFile("test2.bmp");
		System.out.println("Finish ray tracing!");
		viewport.viewportFinalize();
		//viewport.saveConfigToFile("09.xml");

	}
}
