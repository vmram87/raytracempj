package org.raytrace.object.impl;

import org.raytrace.object.TShape;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.CommonVector;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class AxisCuboid extends TShape {
	private float smallX,bigX,smallY,bigY,smallZ,bigZ;
	
	public AxisCuboid(){
		paramList.add("smallX");		
		getMethodMap.put("smallX", "getSmallX");
		setMethodMap.put("smallX", "setSmallX");
		paramList.add("bigX");		
		getMethodMap.put("bigX", "getBigX");
		setMethodMap.put("bigX", "setBigX");
		
		paramList.add("smallY");		
		getMethodMap.put("smallY", "getSmallY");
		setMethodMap.put("smallY", "setSmallY");
		paramList.add("bigY");		
		getMethodMap.put("bigY", "getBigY");
		setMethodMap.put("bigY", "setBigY");
		
		paramList.add("smallZ");		
		getMethodMap.put("smallZ", "getSmallZ");
		setMethodMap.put("smallZ", "setSmallZ");
		paramList.add("bigZ");		
		getMethodMap.put("bigZ", "getBigZ");
		setMethodMap.put("bigZ", "setBigZ");
	}
	
	public AxisCuboid(float x1, float x2, float y1, float y2, float z1, float z2) throws Exception{
		if(x1 >= x2 || y1 >= y2 || z1 >= z2)
			throw new Exception("x1 should < x2, and y1 should < y2 and z1 should < z2");
		this.smallX = x1; 	this.bigX = x2;
		this.smallY = y1;	this.bigY = y2;
		this.smallZ = z1;	this.bigZ = z2;
		
		paramList.add("smallX");		
		getMethodMap.put("smallX", "getSmallX");
		setMethodMap.put("smallX", "setSmallX");
		paramList.add("bigX");		
		getMethodMap.put("bigX", "getBigX");
		setMethodMap.put("bigX", "setBigX");
		
		paramList.add("smallY");		
		getMethodMap.put("smallY", "getSmallY");
		setMethodMap.put("smallY", "setSmallY");
		paramList.add("bigY");		
		getMethodMap.put("bigY", "getBigY");
		setMethodMap.put("bigY", "setBigY");
		
		paramList.add("smallZ");		
		getMethodMap.put("smallZ", "getSmallZ");
		setMethodMap.put("smallZ", "setSmallZ");
		paramList.add("bigZ");		
		getMethodMap.put("bigZ", "getBigZ");
		setMethodMap.put("bigZ", "setBigZ");
	}
	
	
	
	public float getSmallX() {
		return smallX;
	}

	public void setSmallX(Float smallX) {
		this.smallX = smallX;
	}

	public float getBigX() {
		return bigX;
	}

	public void setBigX(Float bigX) {
		this.bigX = bigX;
	}

	public float getSmallY() {
		return smallY;
	}

	public void setSmallY(Float smallY) {
		this.smallY = smallY;
	}

	public float getBigY() {
		return bigY;
	}

	public void setBigY(Float bigY) {
		this.bigY = bigY;
	}

	public float getSmallZ() {
		return smallZ;
	}

	public void setSmallZ(Float smallZ) {
		this.smallZ = smallZ;
	}

	public float getBigZ() {
		return bigZ;
	}

	public void setBigZ(Float bigZ) {
		this.bigZ = bigZ;
	}

	@Override
	public String getShapeType() {
		return this.getClass().getName();
	}
	
	@Override
	public TVector getNormalLine(IPoint3D point) throws Exception {
		if(Math.abs(point.getX() - this.smallX) < 0.01)
			return new TVector(-1, 0, 0);
		else if(Math.abs(point.getX() - this.bigX) < 0.01)
			return new TVector(1, 0, 0);
		else if(Math.abs(point.getY() - this.smallY) < 0.01)
			return new TVector(0, -1, 0);
		else if(Math.abs(point.getY() - this.bigY) < 0.01)
			return new TVector(0, 1, 0);
		else if(Math.abs(point.getZ() - this.smallZ) < 0.01)
			return new TVector(0, 0, -1);
		else if(Math.abs(point.getZ() - this.bigZ) < 0.01)
			return new TVector(0, 0, 1);
		else
			throw new Exception("Point " + point + " is not on the cuboid!");
	}


	@Override
	public boolean rayIntersection(TRay ray, ReferFloatValue t,
			ReferIntValue cLoad) {
		
		float value = 0;
		TVector n;
		
		if(ray.getDirection().getX() != 0){
			if(ray.getDirection().getX() > 0){
				n = new TVector(-1, 0 , 0);
				value = ((-1)*smallX - n.dot(new CommonVector(ray.getOrigin()))) / (n.dot(ray.getDirection()));
				
				IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(value));
				if(intersection.getY() >= smallY && intersection.getY() <= bigY 
						&& intersection.getZ() >= smallZ && intersection.getZ() <= bigZ)
				{
					if(value < 0.1f || value >= t.getfValue()){
						return false;
					}
					else{
						t.setfValue(value);
						return true;
					}
				}
			}
			else{
				n = new TVector(1, 0 , 0);
				value = (bigX - n.dot(new CommonVector(ray.getOrigin()))) / (n.dot(ray.getDirection()));
				
				IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(value));
				if(intersection.getY() >= smallY && intersection.getY() <= bigY 
						&& intersection.getZ() >= smallZ && intersection.getZ() <= bigZ)
				{
					if(value < 0.1f || value >= t.getfValue()){
						return false;
					}
					else{
						t.setfValue(value);
						return true;
					}
				}
			}
		}
		
		
		if(ray.getDirection().getY() != 0){
			if(ray.getDirection().getY() > 0){
				n = new TVector(0, -1 , 0);
				value = ((-1)*smallY - n.dot(new CommonVector(ray.getOrigin()))) / (n.dot(ray.getDirection()));
				
				IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(value));
				if(intersection.getX() >= smallX && intersection.getX() <= bigX 
						&& intersection.getZ() >= smallZ && intersection.getZ() <= bigZ)
				{
					if(value < 0.1f || value >= t.getfValue()){
						return false;
					}
					else{
						t.setfValue(value);
						return true;
					}
				}
			}
			else{
				n = new TVector(0, 1 , 0);
				value = (bigY - n.dot(new CommonVector(ray.getOrigin()))) / (n.dot(ray.getDirection()));
				
				IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(value));
				if(intersection.getX() >= smallX && intersection.getX() <= bigX 
						&& intersection.getZ() >= smallZ && intersection.getZ() <= bigZ)
				{
					if(value < 0.1f || value >= t.getfValue()){
						return false;
					}
					else{
						t.setfValue(value);
						return true;
					}
				}
			}
		}
		
		if(ray.getDirection().getZ() != 0){
			if(ray.getDirection().getZ() > 0){
				n = new TVector(0, 0 , -1);
				value = ((-1)*smallZ - n.dot(new CommonVector(ray.getOrigin()))) / (n.dot(ray.getDirection()));
				
				IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(value));
				if(intersection.getX() >= smallX && intersection.getX() <= bigX 
						&& intersection.getY() >= smallY && intersection.getY() <= bigY)
				{
					if(value < 0.1f || value >= t.getfValue()){
						return false;
					}
					else{
						t.setfValue(value);
						return true;
					}
				}
			}
			else{
				n = new TVector(0, 0 , 1);
				value = (bigZ - n.dot(new CommonVector(ray.getOrigin()))) / (n.dot(ray.getDirection()));
				
				IPoint3D intersection = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(value));
				if(intersection.getX() >= smallX && intersection.getX() <= bigX 
						&& intersection.getY() >= smallY && intersection.getY() <= bigY)
				{
					if(value < 0.1f || value >= t.getfValue()){
						return false;
					}
					else{
						t.setfValue(value);
						return true;
					}
				}
			}
		}
		
		return false;
	}


}
