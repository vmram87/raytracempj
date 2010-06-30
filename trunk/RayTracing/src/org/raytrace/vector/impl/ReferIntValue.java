package org.raytrace.vector.impl;

public class ReferIntValue {
	private int iValue;
	
	public ReferIntValue(int iValue){
		this.iValue=iValue;
	}

	public int getiValue() {
		return iValue;
	}

	public void setiValue(int iValue) {
		this.iValue = iValue;
	}
	
	public void add(int value){
		this.iValue+=value;
	}
}
