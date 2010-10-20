package org.raytrace.viewport;

import org.raytrace.scene.IScene;

public interface IViewPort {
	public boolean init(String[] args);
	public void viewportFinalize();
	public boolean configureFromFile(String fileName) throws Exception;
	public boolean saveToIMGFile(String fileName);
	public boolean saveConfigToFile(String fileName) throws Exception;
	public boolean render() throws Exception;
	
	public IScene getScene();
	public void setScene(IScene scene);
	public int getWidth();
	public void setWidth(int width);
	public int getHeight();
	public void setHeight(int height);
}
