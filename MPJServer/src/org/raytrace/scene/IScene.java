package org.raytrace.scene;

import java.util.List;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.object.IObject;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;

public interface IScene {
	public List getLights();
	public List getObjects();
	public void addLight(ILight light);
	public void addObject(IObject object);
	public ILight removeLight(int index);
	public IObject removeObject(int index);
	public void clearLights();
	public void clearObjects();
	public boolean setAlgorithm(IRayTraceAlgorithm algorithm);
	public void setViewPoint(IPoint3D viewPoint);
	public IPoint3D getViewPoint();
	
	public TColor rayTrace(int x, int y, ReferIntValue cLoad) throws Exception;
	
	public IScene generateRandomScene();
}
