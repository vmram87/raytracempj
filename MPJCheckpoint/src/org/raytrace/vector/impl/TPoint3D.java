package org.raytrace.vector.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raytrace.vector.IPoint3D;

public class TPoint3D implements IPoint3D {
	
	private float x;
	private float y;
	private float z;
	
	public TPoint3D(){
		x=y=z=0;
	}
	
	public TPoint3D(float x, float y, float z){
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	public TPoint3D(IPoint3D point){
		this.x=point.getX();
		this.y=point.getY();
		this.z=point.getZ();
	}
	

	public TPoint3D(String textTrim) throws Exception {
		String regex="\\(\\s*((-)?\\d+(\\.\\d+)?)\\s*,\\s*((-)?\\d+(\\.\\d+)?)\\s*,\\s*((-)?\\d+(\\.\\d+)?)\\s*\\)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(textTrim);
		
		if(m.find()){
			this.x=Float.parseFloat(m.group(1));
			this.y=Float.parseFloat(m.group(4));
			this.z=Float.parseFloat(m.group(7));
		}
		else{
			throw new Exception("point can't be created from the input string!");
		}
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
	
	public static void main(String[] args) throws Exception{
		TPoint3D point=new TPoint3D("( -3.2323 ,-1.001   ,  -3 )");
		
		System.out.println(point);
	}
}
