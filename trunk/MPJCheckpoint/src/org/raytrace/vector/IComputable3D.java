package org.raytrace.vector;

public interface IComputable3D extends IPoint3D {
	public IComputable3D add(IComputable3D vector);
	public IComputable3D substract(IComputable3D vector);
	public IComputable3D multiply(IComputable3D vector);
	public IComputable3D multiply(float n);
	
	public IComputable3D selfAdd(IComputable3D vector);
	public IComputable3D selfSubstract(IComputable3D vector);
	public IComputable3D selfMultiply(IComputable3D vector);
	public IComputable3D selfMultiply(float n);
	
	public float dot(IComputable3D vector);
}
