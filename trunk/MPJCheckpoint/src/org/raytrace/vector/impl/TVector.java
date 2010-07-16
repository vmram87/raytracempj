package org.raytrace.vector.impl;

import org.raytrace.vector.IPoint3D;

public class TVector extends CommonVector {
	
	public TVector(){
		super(0,0,0);
	}

	public TVector(IPoint3D point) {
		super(point);
	}
	
	public TVector(IPoint3D point1,IPoint3D point2) {
		super(point1.getX()-point2.getX(),point1.getY()-point2.getY(),point1.getZ()-point2.getZ());
	}

	public TVector(float x, float y, float z) {
		super(x, y, z);
	}
	
	/*length of the vector*/
	public float norm(){
		return (float) Math.sqrt(dot(this));
	}
	
	public boolean normalize(){
		float norm = this.norm();
    	if(norm == 0.0) return false;

    	float invnorm = 1.0f / norm;
    	setX(getX()*invnorm);
    	setY(getY()*invnorm);
    	setZ(getZ()*invnorm);

    	return true;
	}

}
