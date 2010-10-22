package org.raytrace.object.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.raytrace.object.IShape;
import org.raytrace.object.TShape;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.CommonVector;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TPoint3D;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class Plane extends TShape {
	
	private TPoint3D point = new TPoint3D();
	private TVector normal = new TVector();
	
	public Plane(){
		paramList.add("point");		
		getMethodMap.put("point", "getPoint");
		setMethodMap.put("point", "setPoint");
		
		paramList.add("normal");		
		getMethodMap.put("normal", "getNormal");
		setMethodMap.put("normal", "setNormal");
	}
	
	public Plane(TPoint3D point, TVector normal){
		this.point = point;
		this.normal = normal;
		
		paramList.add("point");	
		getMethodMap.put("point", "getPoint");
		setMethodMap.put("point", "setPoint");
		
		paramList.add("normal");		
		getMethodMap.put("normal", "getNormal");
		setMethodMap.put("normal", "setNormal");
	}
	
	

	public TPoint3D getPoint() {
		return point;
	}

	public void setPoint(TPoint3D point) {
		this.point = point;
	}

	public TVector getNormal() {
		return normal;
	}

	public void setNormal(TVector normal) {
		this.normal = normal;
	}

	@Override
	public TVector getNormalLine(IPoint3D point, TVector inLines) {
		return new TVector(normal);
	}

	@Override
	public String getShapeType() {
		return this.getClass().getName();
	}

	@Override
	public boolean rayIntersection(TRay ray, ReferFloatValue t,
			ReferIntValue cLoad) {
		float RdotN = this.normal.dot(ray.getDirection());
		if(Math.abs(RdotN - 0) < 0.01f)
			return false;
		
		CommonVector cPoint = new CommonVector(point);
		CommonVector rPoint = new CommonVector(ray.getOrigin());
		float tValue = (normal.dot(cPoint) - normal.dot(rPoint ))/RdotN;
		if(tValue <= 0.01f || tValue >= t.getfValue())
			return false;
		
		t.setfValue(tValue);
		return true;
	}

}
