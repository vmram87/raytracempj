package org.raytrace.vector.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public TVector(String textTrim) throws Exception{
		String regex="\\(\\s*((-)?\\d+(\\.\\d+)?)\\s*,\\s*((-)?\\d+(\\.\\d+)?)\\s*,\\s*((-)?\\d+(\\.\\d+)?)\\s*\\)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(textTrim);
		
		if(m.find()){
			this.setX(Float.parseFloat(m.group(1)));
			this.setY(Float.parseFloat(m.group(4)));
			this.setZ(Float.parseFloat(m.group(7)));
		}
		else{
			throw new Exception("point can't create from the input string!");
		}
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
