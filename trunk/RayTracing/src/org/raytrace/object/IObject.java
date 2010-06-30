package org.raytrace.object;

import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public interface IObject {
	public IShape getShape();
	public void setShape(IShape shape);
	public IMaterialProperty getMaterialProperty();
	public void setMaterialProperty(IMaterialProperty materialProperty);
	public boolean rayIntersection(TRay ray,ReferFloatValue t, ReferIntValue cLoad);
	
	public IObject getRandomObject();
}
