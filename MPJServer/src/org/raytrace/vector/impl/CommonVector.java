package org.raytrace.vector.impl;

import org.raytrace.vector.IComputable3D;
import org.raytrace.vector.IPoint3D;

public class CommonVector implements IComputable3D {
	
	private float x,y,z;
	
	public CommonVector(){
		x=y=z=0;
	}
	
	public CommonVector(IPoint3D point){
		this.x=point.getX();
		this.y=point.getY();
		this.z=point.getZ();
	}
	
	public CommonVector(float x, float y, float z){
		this.x=x;
		this.y=y;
		this.z=z;
	}

	@Override
	public float dot(IPoint3D vector) {
		return this.getX()*vector.getX()+this.getY()*vector.getY()+this.getZ()*vector.getZ();
	}
	
	@Override
	public IComputable3D add(IPoint3D vector) {
		return new CommonVector(x+vector.getX(),y+vector.getY(),z+vector.getZ());
	}	

	@Override
	public IComputable3D multiply(float n) {
		return new CommonVector(x*n,y*n,z*n);
	}

	@Override
	public IComputable3D substract(IPoint3D vector) {
		return new CommonVector(x-vector.getX(),y-vector.getY(),z-vector.getZ());
	}
	
	

	@Override
	public IComputable3D multiply(IPoint3D vector) {
		return new CommonVector(x*vector.getX(),y*vector.getY(),z*vector.getZ());
	}

	@Override
	public IComputable3D selfMultiply(IPoint3D vector) {
		setX(this.getX()*vector.getX());
		setY(this.getY()*vector.getY());
		setZ(this.getZ()*vector.getZ());
		return this;
	}

	@Override
	public IComputable3D selfAdd(IPoint3D vector) {
		setX(this.getX()+vector.getX());
		setY(this.getY()+vector.getY());
		setZ(this.getZ()+vector.getZ());
		return this;
	}

	@Override
	public IComputable3D selfMultiply(float n) {
		setX(this.getX()*n);
		setY(this.getY()*n);
		setZ(this.getZ()*n);
		return this;
	}

	@Override
	public IComputable3D selfSubstract(IPoint3D vector) {
		setX(this.getX()-vector.getX());
		setY(this.getY()-vector.getY());
		setZ(this.getZ()-vector.getZ());
		return this;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getZ() {
		return z;
	}

	@Override
	public void setX(float x) {
		this.x=x;
	}

	@Override
	public void setY(float y) {
		this.y=y;
	}

	@Override
	public void setZ(float z) {
		this.z=z;
	}
	
	@Override
	public String toString(){
		return "(" + x + ", " + y + ", " + z + ")";
	}

}
