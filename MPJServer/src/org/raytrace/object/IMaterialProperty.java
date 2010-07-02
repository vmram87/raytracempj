package org.raytrace.object;

import org.raytrace.vector.impl.TIntensity;

public interface IMaterialProperty {
	public TIntensity getAmbient() ;
	public void setAmbient(TIntensity ambient);
	public TIntensity getDiffusion();
	public void setDiffusion(TIntensity diffusion);
	public TIntensity getSpecular() ;
	public void setSpecular(TIntensity specular) ;
	public TIntensity getShining() ;
	public void setShining(TIntensity shining) ;
	public TIntensity getEmission() ;
	public void setEmission(TIntensity emission) ;
	public float getReflection() ;
	public void setReflection(float reflection);
	public float getRefraction() ;
	public void setRefraction(float refraction) ;
	public float getDensity() ;
	public void setDensity(float density) ;
	public int getPower() ;
	public void setPower(int power);
	
	public IMaterialProperty getRandomMaterialProperty();
	
}
