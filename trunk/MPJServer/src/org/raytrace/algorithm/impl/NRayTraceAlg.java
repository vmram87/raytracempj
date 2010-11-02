package org.raytrace.algorithm.impl;

import java.util.List;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.object.IObject;
import org.raytrace.scene.ILight;
import org.raytrace.vector.IComputable3D;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;
import org.raytrace.vector.impl.TIntensity;
import org.raytrace.vector.impl.TPoint3D;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class NRayTraceAlg implements IRayTraceAlgorithm {
	private List objects;
	private List lights;
	private IPoint3D viewPoint;
	private float rayInfinity;
	private int rayMaxLevel;
	@Override
	public TColor rayTrace(List lights, List objects, IPoint3D viewPoint,
			int x, int y, float z, float rayNegInfinity, float rayInfinity,
			int superSamplingCoef, int rayMaxLevel, ReferIntValue cLoad) throws Exception {
		
		this.objects = objects;
		this.lights = lights;
		this.viewPoint = viewPoint;
		this.rayInfinity = rayInfinity;
		this.rayMaxLevel = rayMaxLevel;

		
		TColor returnColor=new TColor(0.0f, 0.0f, 0.0f);
		float incr =invSqrt(superSamplingCoef);
		float ONE_OVER_SRAY_SUPERSAMPLING_COEF=(float) (1.0/superSamplingCoef);;
		
		//0.98, because the incr is not an accurate number, so use some deviation
		for (float fracx = (float)x ; fracx < x + 0.98f; fracx += incr ){
			for (float fracy = (float)y ; fracy < y + 0.98f; fracy += incr )
			{
				TVector direction = new TVector(new TPoint3D(fracx, fracy, z), viewPoint);
				direction.normalize();
				TRay ray = new TRay(viewPoint, direction);
				
				returnColor.selfAdd(tracer(ray, 0, cLoad).multiply(ONE_OVER_SRAY_SUPERSAMPLING_COEF));
			}
		}
		
		return SetColor(returnColor);
	}
	
	TColor SetColor(TColor color)
	{
		color.setR( Math.min(color.getR() * 255.0f, 255.0f));
		color.setG( Math.min(color.getG() * 255.0f, 255.0f));
		color.setB( Math.min(color.getB() * 255.0f, 255.0f));
		return color;
	}
	
	IObject intersectingObject(List objects, TRay ray, ReferFloatValue t, boolean any, ReferIntValue cLoad){
		IObject retValue = null;

		for (int i = 0; i < objects.size(); i++)
		{
			//load
	      cLoad .add(1);

	      	IObject o=(IObject)objects.get(i);
			if(o.rayIntersection(ray, t, cLoad))
			{
	        	if(any)
	        		return o;
	        		
	        	retValue = o;
	        }
	    }

	    return retValue;
	}
	
	
	TColor tracer(TRay ray, int depth, ReferIntValue cLoad) throws Exception
	{

	      TColor color = new TColor();
	      
	      ReferFloatValue t = new ReferFloatValue(rayInfinity);
	      //acquire nearest intercept point
	      IObject object = this.intersectingObject(objects,ray, t, false, cLoad);
	      
	      if(object == null)
				return color;
	      
	      IPoint3D newOrigin = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(t.getfValue()));
	      TVector N = object.getShape().getNormalLine(newOrigin, ray.getDirection());  
   
	      
	      if (!N.normalize())
		         return color;
			
	      for(int i = 0; i < lights.size(); i++)
	      {
	    	  ILight light=(ILight)lights.get(i);
	    	  TVector inLine = new TVector(light.getOrigin(), newOrigin);
	    	  inLine.normalize();
	    	  float lightProjection = N.dot(inLine);
	    	  
	    	  if(lightProjection <= 0.0f) continue;

	    	  float t2 = inLine.norm();
	    	  if(t2 == 0.0f) continue;

	    	  t2 = (float) (1.0 / t2); //InvSqrt(t);
	    	  inLine.selfMultiply(t2);
	    	  lightProjection = t2 * lightProjection;
	    	  
	    	  TRay lightRay=new TRay(newOrigin, inLine);

	    	  t.setfValue(rayInfinity);
	    	  if (null != intersectingObject(objects, lightRay, t, true, cLoad)){
	    		  //if in the shadow continue
	    		  continue;
	    	  }
	    	  
	    	  
	    	  TIntensity ambient = evalAmbient(light.getIntensity(), object.getMaterialProperty().getAmbient()); 
	    	  
	    	  inLine.normalize();
	    	  TIntensity diffuse = evalDiffuse(light.getIntensity(), N, inLine, object.getMaterialProperty().getDiffusion());
	    	  
	    	  TVector V = new TVector(viewPoint, newOrigin);
	    	  V.normalize();
	    	  TIntensity specular = evalSpecular(light.getIntensity(), N, inLine, V, object.getMaterialProperty().getSpecular(), 
	    			  object.getMaterialProperty().getShining());
	    	  
	    	  color.selfAdd( ambient.add(diffuse).add(specular));
	      }

	      depth ++;
	      if(this.rayMaxLevel == depth)
	    	  return color;
	      else
	      {

	           //计算射线和物体交点处的反射射线 Reflect;	 

	    	  float refl = 2.0f * ray.getDirection().dot(N);
	    	  TRay reflectRay = new TRay();
	    	  reflectRay.setOrigin(newOrigin);
	    	  TVector direction = new TVector(ray.getDirection(),N.multiply(refl));
	    	  direction.normalize();
	    	  reflectRay.setDirection(direction);
	          
	    	  TColor c = tracer(reflectRay, depth, cLoad);
	    	  c.selfMultiply(object.getMaterialProperty().getReflection());
	          color.selfAdd(c);
	          return color;

	      }

	}
	

	private TIntensity evalAmbient(TIntensity lightIntensity, TIntensity materialAmbient) {		
		return new TIntensity(lightIntensity.multiply(materialAmbient));
	}

	
	private TIntensity evalDiffuse(TIntensity lightIntensity, TVector N, TVector inLine,
			TIntensity materialDiffuse) {
		IComputable3D IdKd =  lightIntensity.multiply(materialDiffuse);
		float NdotL = Math.max(N.dot(inLine), 0.0f);
		return new TIntensity(IdKd.selfMultiply(NdotL));
	}

	
	private TIntensity evalSpecular(TIntensity lightIntensity, TVector N, TVector inLine,
			TVector cameraDirection, TIntensity materialSpecular,
			float shininess) {
		IComputable3D IsKs =  lightIntensity.multiply(materialSpecular);
		
		TVector H = new TVector(inLine.add(N));
		H.normalize();
		
		//float NdotL = Math.max(N.dot(inLine), 0.0f);
		
		float NdotH = (float) Math.pow(Math.max(N.dot(H), 0.0f), shininess);
		
		//if(NdotL <= 0)
			//NdotH = 0;
		
		return new TIntensity(IsKs.selfMultiply(NdotH));
	}
	
	//quick inverse and spare algorithm
	public static float invSqrt(float x){
		float xhalf = 0.5f * x;
		int i = 0x5F3759DF - (Float.floatToIntBits(x)>>1);
		x = Float.intBitsToFloat(i);
		x = x * (1.5f - xhalf * x * x);
		return x;
	}

}
