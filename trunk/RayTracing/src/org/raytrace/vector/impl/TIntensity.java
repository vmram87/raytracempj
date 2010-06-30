package org.raytrace.vector.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raytrace.vector.IPoint3D;

public class TIntensity extends CommonVector {
	
	public TIntensity(){
		super(0,0,0);
	}

	public TIntensity(float x, float y, float z) {
		super(x, y, z);
	}

	public TIntensity(IPoint3D point) {
		super(point);
	}

	public TIntensity(String textTrim) throws Exception {
		String regex="\\(\\s*(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)\\s*\\)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(textTrim);
		
		if(m.find()){
			this.setX(Float.parseFloat(m.group(1)));
			this.setY(Float.parseFloat(m.group(3)));
			this.setZ(Float.parseFloat(m.group(5)));
		}
		else{
			throw new Exception("point can't create from the input string!");
		}
	}
	
	public static void main(String[] args) throws Exception{
		TIntensity t= new TIntensity("(1,2.222 ,3 )");
		System.out.println(t);
	}

}
