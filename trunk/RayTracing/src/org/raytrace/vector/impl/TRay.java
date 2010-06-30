package org.raytrace.vector.impl;

import org.raytrace.vector.IPoint3D;


public class TRay {
	private IPoint3D origin;
	private TVector direction;
	
	public TRay(){
		origin=new TPoint3D(0,0,-1000);
		direction=new TVector(0,0,1);
	}
	
	public TRay(IPoint3D origin, TVector direction){
		this.origin=origin;
		this.direction=direction;
	}

	public IPoint3D getOrigin() {
		return origin;
	}

	public void setOrigin(IPoint3D origin) {
		this.origin = origin;
	}

	public TVector getDirection() {
		return direction;
	}

	public void setDirection(TVector direction) {
		this.direction = direction;
	}
	
	
}
