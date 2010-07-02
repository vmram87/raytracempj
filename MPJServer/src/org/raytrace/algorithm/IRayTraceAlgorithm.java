package org.raytrace.algorithm;

import java.util.List;

import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;

public interface IRayTraceAlgorithm {
	public TColor rayTrace(List lights,List objects, IPoint3D viewPoint, int x, int y, float z,
			float rayNegInfinity,float rayInfinity, int superSamplingCoef, int rayMaxLevel, ReferIntValue cLoad);

}
