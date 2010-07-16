package org.raytrace.object.impl;

import org.raytrace.object.IMaterialProperty;
import org.raytrace.object.IObject;
import org.raytrace.object.IShape;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class TObject implements IObject {
	private IShape shape;
	private IMaterialProperty materialProperty;
	
	public TObject(IShape shape, IMaterialProperty materialProperty){
		this.shape=shape;
		this.materialProperty=materialProperty;
	}

	@Override
	public IMaterialProperty getMaterialProperty() {
		return this.materialProperty;
	}
	

	@Override
	public IShape getShape() {
		return this.shape;
	}

	@Override
	public boolean rayIntersection(TRay ray, ReferFloatValue t, ReferIntValue cLoad) {
		return this.shape.rayIntersection(ray, t, cLoad);
	}

	@Override
	public void setMaterialProperty(IMaterialProperty materialProperty) {
		this.materialProperty=materialProperty;
	}

	@Override
	public void setShape(IShape shape) {
		this.shape=shape;
	}
	
	
	
	@Override
	public IObject getRandomObject() {
		// TODO Auto-generated method stub
		return null;
	}

}
