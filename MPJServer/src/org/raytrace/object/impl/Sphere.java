package org.raytrace.object.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raytrace.object.IShape;
import org.raytrace.object.TShape;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TPoint3D;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class Sphere extends TShape {
	private TPoint3D center;
	private Float radius;
	
	public Sphere(){
		this.center=new TPoint3D(0,0,0);
		this.radius=0.0f;
		paramList.add("center");
		getMethodMap.put("center", "getCenter");
		setMethodMap.put("center", "setCenter");
		
		paramList.add("radius");		
		getMethodMap.put("radius", "getRadius");
		setMethodMap.put("radius", "setRadius");

	}
	
	public Sphere(TPoint3D origin, float radius){
		this.center=origin;
		this.radius=radius;
		
		paramList.add("center");
		getMethodMap.put("center", "getCenter");
		setMethodMap.put("center", "setCenter");
		
		paramList.add("radius");		
		getMethodMap.put("radius", "getRadius");
		setMethodMap.put("radius", "setRadius");
	}


	public TPoint3D getCenter() {
		return center;
	}

	public void setCenter(TPoint3D center) {
		this.center = center;
	}

	public Float getRadius() {
		return radius;
	}

	public void setRadius(Float radius) {
		this.radius = radius;
	}


	@Override
	public String getShapeType() {
		return this.getClass().getName();
	}
	
	
	@Override
	public boolean rayIntersection(TRay ray, ReferFloatValue tReturn, ReferIntValue cLoad) {
		boolean retValue = false;
		
		TVector v = new TVector(ray.getOrigin(),this.center);
		
		float a = ray.getDirection().dot(ray.getDirection());
		float b = 2 * ray.getDirection().dot(v);
		float c = v.dot(v) - (this.radius * this.radius);
		
		float D = b * b - 4 * a * c;

		//load
	   cLoad.add(25);
	
	   if (D < 0.0f) return false;
	
	   float d = (float) Math.sqrt(D);
	   float t0 = (- b - d) / (2 * a);
	   float t1 = (- b + d) / (2 * a);
	   float t=tReturn.getfValue();
	
	   if (t0 > 0.1f && t0 < t)
	   {
	      t = t0;
	      tReturn.setfValue(t);
	      retValue = true;
	   }
	
	   if (t1 > 0.1f && t1 < t)
	   {
		  t = t1;
		  tReturn.setfValue(t);
	      retValue = true;
	   }
	
	   //load
	   cLoad.add(34);	   
	   
	   return retValue; 	
	}

	@Override
	public TVector getNormalLine(IPoint3D point) {
		 return new TVector(point,center);
		 
	}
	
	
	public static void main(String[] args) throws Exception{
		Sphere s=new Sphere();    
	    s.setParamValue("radius", new Float(2.3));
	    
	    System.out.println(s.getParamObject("radius"));
	    
	    
	}

}
