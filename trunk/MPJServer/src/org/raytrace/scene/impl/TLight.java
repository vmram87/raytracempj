package org.raytrace.scene.impl;

import org.raytrace.scene.ILight;
import org.raytrace.vector.IComputable3D;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.TIntensity;
import org.raytrace.vector.impl.TPoint3D;
import org.raytrace.vector.impl.TVector;

public class TLight implements ILight{
	private IPoint3D origin = new TPoint3D();
	private TIntensity intensity =new TIntensity();
	

	public TLight(){
	}
	
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


	@Override
	public TIntensity evalAmbient(TIntensity materialAmbient) {		
		return new TIntensity(this.intensity.multiply(materialAmbient));
	}

	@Override
	public TIntensity evalDiffuse(TVector N, TVector inLine,
			TIntensity materialDiffuse) {
		IComputable3D IdKd =  this.intensity.multiply(materialDiffuse);
		float NdotL = Math.max(N.dot(inLine), 0.0f);
		return new TIntensity(IdKd.selfMultiply(NdotL));
	}

	@Override
	public TIntensity evalSpecular(TVector N, TVector inLine,
			TVector cameraDirection, TIntensity materialSpecular,
			float shininess) {
		IComputable3D IsKs =  this.intensity.multiply(materialSpecular);
		
		TVector H = new TVector(inLine.add(N));
		H.normalize();
		
		//float NdotL = Math.max(N.dot(inLine), 0.0f);
		
		float NdotH = (float) Math.pow(Math.max(N.dot(H), 0.0f), shininess);
		
		//if(NdotL <= 0)
			//NdotH = 0;
		
		return new TIntensity(IsKs.selfMultiply(NdotH));
	}
	
	
	
}
