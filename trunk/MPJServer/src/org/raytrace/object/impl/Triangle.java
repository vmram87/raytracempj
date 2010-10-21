package org.raytrace.object.impl;

import org.raytrace.object.TShape;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TPoint3D;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class Triangle extends TShape {
	private TPoint3D[] vertextList = {new TPoint3D(), new TPoint3D(), new TPoint3D()};
	private TVector[] normalList = {new TVector(), new TVector(), new TVector()};
	private TVector planeNorm = new TVector();
	private float area;
	private boolean haveComputeNormAndArea = false;
	private boolean setNormal = false;
	private final double PI = 3.1415926535;
	
	public Triangle(){
		paramList.add("vertext1");	
		getMethodMap.put("vertext1", "getVertext1");
		setMethodMap.put("vertext1", "setVertext1");
		
		paramList.add("vertext2");		
		getMethodMap.put("vertext2", "getVertext2");
		setMethodMap.put("vertext2", "setVertext2");
		
		paramList.add("vertext3");		
		getMethodMap.put("vertext3", "getVertext3");
		setMethodMap.put("vertext3", "setVertext3");
		
		paramList.add("normal1");	
		getMethodMap.put("normal1", "getNormal1");
		setMethodMap.put("normal1", "setNormal1");
		
		paramList.add("normal2");	
		getMethodMap.put("normal2", "getNormal2");
		setMethodMap.put("normal2", "setNormal2");
		
		paramList.add("normal3");	
		getMethodMap.put("normal3", "getNormal3");
		setMethodMap.put("normal3", "setNormal3");
	}

	
	public TPoint3D getVertext1() {
		return this.vertextList[0];
	}


	public void setVertext1(TPoint3D vertext1) {
		this.vertextList[0]= vertext1;
	}


	public TPoint3D getVertext2() {
		return this.vertextList[1];
	}


	public void setVertext2(TPoint3D vertext2) {
		this.vertextList[1] = vertext2;
	}


	public TPoint3D getVertext3() {
		return this.vertextList[2];
	}


	public void setVertext3(TPoint3D vertext3) {
		this.vertextList[2] = vertext3;
	}
	
	
	public TVector getNormal1() {
		return normalList[0];
	}


	public void setNormal1(TVector normal1) {
		normalList[0] = normal1;
		setNormal = true;
	}


	public TVector getNormal2() {
		return normalList[1];
	}


	public void setNormal2(TVector normal2) {
		normalList[1] = normal2;
		setNormal = true;
	}


	public TVector getNormal3() {
		return normalList[2];
	}


	public void setNormal3(TVector normal3) {
		this.normalList[2] = normal3;
		setNormal = true;
	}

	
	

	@Override
	public String getShapeType() {
		return this.getClass().getName();
	}

	@Override
	public TVector getNormalLine(IPoint3D point) throws Exception {
		if(!haveComputeNormAndArea){
			
			planeNorm = cross(new TVector(vertextList[2], vertextList[1]), 
					new TVector(vertextList[0], vertextList[1]));
			planeNorm.normalize();
			area = calcArea(vertextList[0], vertextList[1],vertextList[2]);
			if(setNormal == false){
				normalList[0] = new TVector(planeNorm);
				normalList[0] = new TVector(planeNorm);
				normalList[0] = new TVector(planeNorm);
			}
			
			haveComputeNormAndArea = true;
		}
		
		float area1 = calcArea(point, vertextList[0], vertextList[1]);
		float area2 = calcArea(point, vertextList[1], vertextList[2]);
		float area3 = calcArea(point, vertextList[2], vertextList[0]);
		
		float weight0 = area2 / area;
		float weight1 = area3 / area;
		float weight2 = area1 / area;
		
		
		TVector nLine =  new TVector(normalList[0].multiply(weight0).add(normalList[1].multiply(weight1).add(normalList[2].multiply(weight2))));
		if(nLine.getZ() > 0){
			nLine.selfMultiply(-1);
		}
		
		return nLine;
	}

	

	@Override
	public boolean rayIntersection(TRay ray, ReferFloatValue t,
			ReferIntValue cLoad) {
		if(!haveComputeNormAndArea){

			planeNorm = cross(new TVector(vertextList[2], vertextList[1]), 
					new TVector(vertextList[0], vertextList[1]));
			planeNorm.normalize();
			area = calcArea(vertextList[0], vertextList[1],vertextList[2]);
			if(setNormal == false){
				normalList[0] = new TVector(planeNorm);
				normalList[0] = new TVector(planeNorm);
				normalList[0] = new TVector(planeNorm);
			}
			
			haveComputeNormAndArea = true;
		}
		
		float  dp= - planeNorm.dot(vertextList[0]);
		float denum = planeNorm.dot(ray.getDirection());
		
		if(Math.abs(denum - 0) < 0.01)
			return false;
		
		float tValue = - (planeNorm.dot(ray.getOrigin()) + dp) / denum;
		if(tValue < 0.01 || tValue >= t.getfValue())
			return false;
		
		IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(tValue));
		TVector v1 = new TVector(vertextList[0], intersection);
		TVector v2 = new TVector(vertextList[1], intersection);
		TVector v3 = new TVector(vertextList[2], intersection);
		
		double angle = 
			Math.abs(Math.acos(v1.dot(v2) / (v1.norm()*v2.norm()))) + 
			Math.abs(Math.acos(v1.dot(v3) / (v1.norm()*v3.norm()))) +
			Math.abs(Math.acos(v2.dot(v3) / (v2.norm()*v3.norm())));
		
		if(Math.abs(angle - 2*PI) < 0.001){
			t.setfValue(tValue);
			return true;
		}
		else
			return false;
	}
	
	protected static float calcArea(IPoint3D p0, IPoint3D p1, IPoint3D p2){
		TVector v1 = new TVector(p0, p1);
		TVector v2 = new TVector(p2, p0);
		
		float v1_length = v1.norm();
		float v2_length = v2.norm();
		
		v1.normalize();
		v2.normalize();
		
		float tmp = v1.dot(v2);
		if(tmp > 1)
			tmp = 1.0f;
		else if(tmp < -1)
			tmp = -1.0f;
		
		float angle = (float) Math.acos(tmp);
		
		return (float) (0.5 * v1_length * v2_length * Math.sin(angle));
	}

	protected static TVector cross(TVector v1, TVector v2){
		float a = v1.getY() * v2.getZ() - v2.getY() * v1.getZ();
		float b = v2.getX() * v1.getZ() - v1.getX() * v2.getZ();
		float c = v1.getX() * v2.getY() - v2.getX() * v1.getY();
		return new TVector(a, b, c);
	}
}
