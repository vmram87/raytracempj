package org.raytrace.scene.impl;

import org.raytrace.scene.ILight;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.TIntensity;

public class TLight implements ILight{
	private IPoint3D origin;
	private TIntensity intensity;

	
	public TLight(IPoint3D origin, TIntensity intensity){
		this.origin=origin;
		this.intensity=intensity;
	}
	
	@Override
	public IPoint3D getOrigin() {
		return origin;
	}
	
	@Override
	public void setOrigin(IPoint3D origin) {
		this.origin = origin;
	}
	
	@Override
	public TIntensity getIntensity() {
		return intensity;
	}
	
	@Override
	public void setIntensity(TIntensity intensity) {
		this.intensity = intensity;
	}
	
	
	@Override
	public ILight getRandomLight() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
