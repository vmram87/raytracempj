package org.raytrace.viewport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.raytrace.object.IMaterialProperty;
import org.raytrace.object.IObject;
import org.raytrace.object.IShape;
import org.raytrace.object.impl.TMaterialProperty;
import org.raytrace.object.impl.TObject;
import org.raytrace.scene.ILight;
import org.raytrace.scene.IScene;
import org.raytrace.scene.impl.TLight;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.TColor;
import org.raytrace.vector.impl.TIntensity;
import org.raytrace.vector.impl.TPoint3D;

public abstract class AbstractViewPort implements IViewPort {
	protected IScene scene;
	protected int width = 800;
	protected int height = 600;
	
	
	protected AbstractViewPort(IScene scene){
		this.scene=scene;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public IScene getScene() {
		return this.scene;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public void setHeight(int height) {
		this.height=height;		
	}

	@Override
	public void setScene(IScene scene) {
		this.scene=scene;	
	}

	@Override
	public void setWidth(int width) {
		this.width=width;		
	}

	@Override
	public boolean configureFromFile(String fileName) throws Exception {
		File srcxmlFile=new File(fileName);
		SAXReader reader=new SAXReader();
		Document srcdoc=reader.read(srcxmlFile);
		Element root=srcdoc.getRootElement();
		
		Element imgElem =(Element)root.elements().get(0);
		List elements=imgElem.elements();
		
		for(Iterator it=elements.iterator();it.hasNext();){
			Element elem=(Element)it.next();
			
			if(elem.getName().equals("width")){
				this.setWidth(Integer.parseInt(elem.getTextTrim()));
			}
			
			else if(elem.getName().equals("height")){
				this.setHeight(Integer.parseInt(elem.getTextTrim()));
			}
			
			else if(elem.getName().equals("objects")){
				List objectElements=elem.elements();
				
				for(Iterator it2=objectElements.iterator();it2.hasNext();){
					Element objectElem=(Element)it2.next();
					String className=objectElem.attribute("type").getValue();
					List params=new ArrayList();
					
					//create a instance of the shape and mertrial of a object
					IShape shape=(IShape)newInstance(className,null);
					IMaterialProperty materialProperty=new TMaterialProperty();
					
					//child elements of shape
					List shapeElements=((Element)objectElem.elements().get(0)).elements();
					
					for(Iterator it3=shapeElements.iterator();it3.hasNext();){
						Element paramElement=(Element)it3.next();
						String paramName=paramElement.getName();
						String paramClassName=paramElement.attributeValue("type");
						Object[] args={paramElement.getTextTrim()};
						Object paramObject=newInstance(paramClassName,args);
						shape.setParamValue(paramName, paramObject);
					}
					
					//child elements of the Material
					List materialElements=((Element)objectElem.elements().get(1)).elements();
					
					//get material property
					for(Iterator it4=materialElements.iterator();it4.hasNext();){
						Element paramElement=(Element)it4.next();
						String paramName=paramElement.getName();
						
						if(paramName.equals("ambient")){
							materialProperty.setAmbient(new TIntensity(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("diffusion")){
							materialProperty.setDiffusion(new TIntensity(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("specular")){
							materialProperty.setSpecular(new TIntensity(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("shininess")){
							materialProperty.setShining(new TIntensity(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("emission")){
							materialProperty.setEmission(new TIntensity(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("reflection")){
							materialProperty.setReflection(Float.parseFloat(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("refraction")){
							materialProperty.setRefraction(Float.parseFloat(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("density")){
							materialProperty.setDensity(Float.parseFloat(paramElement.getTextTrim()));
						}
						
						else if(paramName.equals("power")){
							materialProperty.setPower(Integer.parseInt(paramElement.getTextTrim()));
						}
						
					}					
					
					this.scene.addObject(new TObject(shape,materialProperty));
				}
			}
			
			else if(elem.getName().equals("lights")){
				List lightElements=elem.elements();
				
				for(Iterator it3=lightElements.iterator();it3.hasNext();){
					Element light=(Element)it3.next();
					
					//child elements of light
					List paramElements=light.elements();
					String temp=((Element)(paramElements.get(0))).getTextTrim();
					IPoint3D origin=new TPoint3D(((Element)(paramElements.get(0))).getTextTrim());
					TIntensity intensity=new TIntensity(((Element)(paramElements.get(1))).getTextTrim());
					
					this.scene.addLight(new TLight(origin,intensity));
					
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean saveConfigToFile(String fileName) throws Exception {
		try{
			FileWriter wrFile=new FileWriter(fileName);
			BufferedWriter bwrFile=new BufferedWriter(wrFile);
	
			String strLine;
	
			bwrFile.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
			bwrFile.newLine();
			bwrFile.write("<inputset filename=\"output.xml\">");
			bwrFile.newLine();
			bwrFile.write("\t<image>");
			bwrFile.newLine();
			bwrFile.write("\t\t<width>"+this.width+"</width>");
			bwrFile.newLine();
			bwrFile.write("\t\t<height>"+this.height+"</height>");
			bwrFile.newLine();
			bwrFile.write("\t\t<objects>");
			bwrFile.newLine();
	
	
		   for (int i = 0; i < this.scene.getObjects().size(); i++)
		   {
			   IObject obj=(IObject)this.scene.getObjects().get(i);
	
			   bwrFile.write("\t\t\t<object id=\""+(i+1)+"\" type=\""+obj.getShape().getClass().getName()+"\">");
		       bwrFile.newLine();
		       bwrFile.write("\t\t\t\t<shape>");
		       bwrFile.newLine();
		       
		      
		       List paramList=obj.getShape().getParamNameList();
		       for(Object paramName : paramList){
		    	   bwrFile.write("\t\t\t\t\t<"+paramName+ " type='"+ obj.getShape().getParamObject(paramName.toString()).getClass().getName()
		    			   +"'>" +obj.getShape().getParamObject((String)paramName) +"</"+paramName+">");
		    	   bwrFile.newLine();
		       }     
		       bwrFile.write("\t\t\t\t</shape>");
			   bwrFile.newLine();
			   
			   bwrFile.write("\t\t\t\t<materialproperty>");
			   bwrFile.newLine();
			   
			   bwrFile.write("\t\t\t\t\t<ambient>"+obj.getMaterialProperty().getAmbient()+"</ambient>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<diffusion>" +obj.getMaterialProperty().getDiffusion()+"</diffusion>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<specular>" +obj.getMaterialProperty().getSpecular()+"</specular>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<shininess>"+obj.getMaterialProperty().getShining()+"</shininess>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<emission>"+obj.getMaterialProperty().getEmission()+"</emission>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<reflection>" +obj.getMaterialProperty().getReflection()+"</reflection>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<refraction>" +obj.getMaterialProperty().getRefraction()+"</refraction>" );
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<density>"+obj.getMaterialProperty().getDensity()+"</density>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t\t<power>"+obj.getMaterialProperty().getPower()+"</power>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t</materialproperty>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t</object>");
			   bwrFile.newLine();
	
	
		   }
		   
		   bwrFile.write("\t\t</objects>");
		   bwrFile.newLine();
		   
		   bwrFile.write("\t\t<lights>");
		   bwrFile.newLine();
		   
		   for (int i = 0; i < this.scene.getLights().size(); i++)
		   {
			   ILight light=(ILight)this.scene.getLights().get(i);
			   
			   bwrFile.write("\t\t\t<light id=\""+(i+1)+"\">");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t<origin>"+light.getOrigin()+"</origin>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t\t<intensity>"+light.getIntensity()+"</intensity>");
			   bwrFile.newLine();
			   bwrFile.write("\t\t\t</light>");
			   bwrFile.newLine();
		   }
		   
		   bwrFile.write("\t\t</lights>");
		   bwrFile.newLine();
		   bwrFile.write("\t</image>");
		   bwrFile.newLine();
		   bwrFile.write("</inputset>");
		   bwrFile.newLine();
		   
	
		   bwrFile.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		return true;
	}	
	
	//if args == null , use the default constructor
	public Object newInstance(String className, Object[] args) throws Exception {    
	    Class newoneClass = Class.forName(className);   
	    
	    if(args==null)
	    	return newoneClass.newInstance();
	                                                                                 
	    Class[] argsClass = new Class[args.length];                                  
	                                                                                 
	    for (int i = 0, j = args.length; i < j; i++) {                               
	        argsClass[i] = args[i].getClass();                                       
	     }                                                                            
	                                                                                 
	    Constructor cons = newoneClass.getConstructor(argsClass);                    
		                                                                                  
	    return cons.newInstance(args);                                               
		                                                                                  
	} 
	
	protected static String getFileExtension(String fileName){
		int pos=fileName.lastIndexOf(".");
		return fileName.substring(pos+1).toLowerCase();
	}

}
