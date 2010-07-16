package org.raytrace.viewport.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.raytrace.scene.IScene;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;
import org.raytrace.viewport.AbstractViewPort;

public class SequenceViewport extends AbstractViewPort {
	protected List<TColor> imageMatrix = new ArrayList<TColor> ();
	
	public SequenceViewport(IScene scene) {
		super(scene);
	}

	@Override
	public boolean init(String[] args) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean saveToIMGFile(String fileName) {
		try{
			File file = new File(fileName);   
			
			int segment;
	        
			 BufferedImage bi = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);   
	 
			 for(int y = 0; y < this.height; y++){
				for(int x = 0; x < this.width; x++){
					segment = y*this.width + x;
					TColor c=this.imageMatrix.get(segment);
					Color color=new Color(c.getR()/255, c.getG()/255, c.getB()/255);
					bi.setRGB(x, height-1-y, color.getRGB());
				}
			 }
			    
			 ImageIO.write(bi, getFileExtension(fileName), file);  		    
		}
		
		catch(Exception e){
			e.printStackTrace();
			return false;
			
		}
		
		return true;
	}

	@Override
	public boolean render() {
		ReferIntValue cLoad = new ReferIntValue(0);

		for(int y = 0; y < this.height; y++)
		{
			for(int x = 0; x < this.width; x++)
			{
				//LOAD_MEASUREMENT
				cLoad .add(20);

				this.imageMatrix.add(this.scene.rayTrace(x, y, cLoad));
			}
		}

		return true;
	}
	
	public static void main(String[] args){
		try{
			String fileName="1.gif";
			File file = new File(fileName);   
			
			int segment;
	        
			 BufferedImage bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);   
	 
			 for(int y = 0; y < 600; y++){
				for(int x = 0; x < 800; x++){
					segment = y*600 + x;
					TColor c=new TColor(1,0,0);
					Color color=new Color(c.getR(), c.getG(), c.getB());
					bi.setRGB(x, y, color.getRGB());
				}
			 }
			    
			 ImageIO.write(bi, getFileExtension(fileName), file);  		    
		}
		
		catch(Exception e){
			e.printStackTrace();
			
		}
	}

	@Override
	public void viewportFinalize() {
		// TODO Auto-generated method stub
		
	}
	
	

}
