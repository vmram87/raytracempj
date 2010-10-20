package org.raytrace.object.impl;

import org.raytrace.object.IMaterialProperty;
import org.raytrace.vector.impl.TIntensity;

public class TMaterialProperty implements IMaterialProperty{
	private TIntensity ambient=new TIntensity();
	private TIntensity diffusion=new TIntensity();
	private TIntensity specular=new TIntensity();
	private float shining = 0;
	private TIntensity emission=new TIntensity();
	
	private float reflection=0;
	private float refraction=0;
	private float density;
	private int power;
	
	public TIntensity getAmbient() {
		return ambient;
	}
	public void setAmbient(TIntensity ambient) {
		this.ambient = ambient;
	}
	public TIntensity getDiffusion() {
		return diffusion;
	}
	public void setDiffusion(TIntensity diffusion) {
		this.diffusion = diffusion;
	}
	public TIntensity getSpecular() {
		return specular;
	}
	public void setSpecular(TIntensity specular) {
		this.specular = specular;
	}
	
	public float getShining() {
		return shining;
	}
	public void setShining(float shining) {
		this.shining = shining;
	}
	public TIntensity getEmission() {
		return emission;
	}
	public void setEmission(TIntensity emission) {
		this.emission = emission;
	}
	public float getReflection() {
		return reflection;
	}
	public void setReflection(float reflection) {
		this.reflection = reflection;
	}
	public float getRefraction() {
		return refraction;
	}
	public void setRefraction(float refraction) {
		this.refraction = refraction;
	}
	public float getDensity() {
		return density;
	}
	public void setDensity(float density) {
		this.density = density;
	}
	public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
	
	
	
	@Override
	public IMaterialProperty getRandomMaterialProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
