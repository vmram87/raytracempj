package org.raytrace.scene;

import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.TIntensity;
import org.raytrace.vector.impl.TVector;

public interface ILight {
	public IPoint3D getOrigin();
	public void setOrigin(IPoint3D point);
	public TIntensity getIntensity();
	public void setIntensity(TIntensity intensity);
	
	
	public ILight getRandomLight();
	
}
