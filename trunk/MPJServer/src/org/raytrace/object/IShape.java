package org.raytrace.object;

import java.util.List;

import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public interface IShape {
	//return true only when there is an intersection and the dist between intersection point and ray position is less than t
	public boolean rayIntersection(TRay ray, ReferFloatValue t, ReferIntValue cLoad);
	public TVector getNormalLine(IPoint3D point) throws Exception;
	
	public String getShapeType();
	public List getParamNameList();
	public Object getParamObject(String paramName) throws Exception;
	public void setParamValue(String paramName, Object value) throws Exception;
	
	public IShape getRandomShape();
}
