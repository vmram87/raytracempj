package org.raytrace.scene.impl;

import java.util.List;
import java.util.Vector;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.context.RayTraceContext;
import org.raytrace.object.IObject;
import org.raytrace.scene.ILight;
import org.raytrace.scene.IScene;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;
import org.raytrace.vector.impl.TPoint3D;

public class TScene implements IScene {
	private Vector lights=new Vector<ILight>(); 
	private Vector objects=new Vector<IObject>();
	private IRayTraceAlgorithm algorithm;
	private IPoint3D viewPoint = new TPoint3D();
	
	
	public void setViewPoint(IPoint3D viewPoint) {
		this.viewPoint = viewPoint;
	}


	public IPoint3D getViewPoint() {
		return viewPoint;
	}


	public TScene(IRayTraceAlgorithm algorithm){
		this.algorithm=algorithm;
	}
	

	@Override
	public List getLights() {
		return this.lights;
	}


	@Override
	public List getObjects() {
		return this.objects;
	}


	@Override
	public void addLight(ILight light) {
		lights.add(light);
	}

	@Override
	public void addObject(IObject object) {
		objects.add(object);
	}

	@Override
	public void clearLights() {
		lights.clear();
	}

	@Override
	public void clearObjects() {
		objects.clear();
	}

	@Override
	public IScene generateRandomScene() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILight removeLight(int index) {
		return (ILight)lights.remove(index);
	}

	@Override
	public IObject removeObject(int index) {
		return (IObject)objects.remove(index);
	}

	@Override
	public boolean setAlgorithm(IRayTraceAlgorithm algorithm) {
		//check if the algorithm is valid
		//...
		
		this.algorithm=algorithm;
		return true;
	}
	
	
	@Override
	public TColor rayTrace(int x, int y, ReferIntValue cLoad) throws Exception {
		RayTraceContext context=RayTraceContext.getContext();
		
		//temporally not use, because here we just use the negative infinity as the viewpoint 
		//IPoint3D viewPoint=new TPoint3D();
		//z is also not use, just use z for parameter
		float z=0;
		return algorithm.rayTrace(lights, objects,viewPoint, x, y, z, context.getRayNegInfinity(), context.getRayInfinity(),
				context.getSuperSamplingCoef(), context.getRayMaxLevel(), cLoad);		
	}

}
