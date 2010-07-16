package org.raytrace.vector.impl;

import org.raytrace.vector.IComputable3D;
import org.raytrace.vector.IPoint3D;

public class TColor extends CommonVector {
	
	public TColor(){
		super(0,0,0);
	}

	public TColor(float x, float y, float z) {
		super(x, y, z);
	}

	public TColor(IPoint3D point) {
		super(point);
	}
	
	
	
	public float getR(){
		return getX();
	}
	
	public float getG(){
		return getY();
	}
	
	public float getB(){
		return getZ();
	}
	
	public void setR(float r){
		setX(r);
	}
	
	public void setG(float g){
		setY(g);
	}
	
	public void setB(float b){
		setZ(b);
	}

}
