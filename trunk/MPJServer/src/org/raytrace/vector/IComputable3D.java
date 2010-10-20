package org.raytrace.vector;

public interface IComputable3D extends IPoint3D {
	public IComputable3D add(IPoint3D vector);
	public IComputable3D substract(IPoint3D vector);
	public IComputable3D multiply(IPoint3D vector);
	public IComputable3D multiply(float n);
	
	public IComputable3D selfAdd(IPoint3D vector);
	public IComputable3D selfSubstract(IPoint3D vector);
	public IComputable3D selfMultiply(IPoint3D vector);
	public IComputable3D selfMultiply(float n);
	
	public float dot(IPoint3D vector);
}
