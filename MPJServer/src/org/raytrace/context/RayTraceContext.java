package org.raytrace.context;

public class RayTraceContext {
	private static RayTraceContext context=null;
	
	private float rayNegInfinity=-1000;
	private float rayInfinity=10000;
	private float nearestVPointDist=0.1f;
	private int rayMaxLevel=2;
	private int superSamplingCoef=16;
	
	private RayTraceContext(){}
	
	public static synchronized RayTraceContext getContext(){
		if(context==null){
			context=new RayTraceContext();
		}
		return context;
	}
	
	
	//config IO
	public boolean configFromFile(String fileName){
		
		
		return true;
	}
	
	public boolean saveConfigToFile(String fileName){
		
		
		return true;
	}
	
	
	
	//getter setter
	public float getRayNegInfinity() {
		return rayNegInfinity;
	}

	public void setRayNegInfinity(float rayNegInfinity) {
		this.rayNegInfinity = rayNegInfinity;
	}

	public float getRayInfinity() {
		return rayInfinity;
	}

	public void setRayInfinity(float rayInfinity) {
		this.rayInfinity = rayInfinity;
	}

	public float getNearestVPointDist() {
		return nearestVPointDist;
	}

	public void setNearestVPointDist(float nearestVPointDist) {
		this.nearestVPointDist = nearestVPointDist;
	}

	public int getRayMaxLevel() {
		return rayMaxLevel;
	}

	public void setRayMaxLevel(int rayMaxLevel) {
		this.rayMaxLevel = rayMaxLevel;
	}

	public int getSuperSamplingCoef() {
		return superSamplingCoef;
	}

	public void setSuperSamplingCoef(int superSamplingCoef) {
		this.superSamplingCoef = superSamplingCoef;
	}
	
	 
}
