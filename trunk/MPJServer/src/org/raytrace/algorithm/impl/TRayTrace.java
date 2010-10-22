package org.raytrace.algorithm.impl;

import java.util.List;

import org.raytrace.algorithm.IRayTraceAlgorithm;
import org.raytrace.object.IObject;
import org.raytrace.scene.ILight;
import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;
import org.raytrace.vector.impl.TIntensity;
import org.raytrace.vector.impl.TPoint3D;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public class TRayTrace implements IRayTraceAlgorithm {

	@Override
	public TColor rayTrace(List lights, List objects, IPoint3D viewPoint, int x, int y, float z,
			float rayNegInfinity, float rayInfinity, int superSamplingCoef,
			int rayMaxLevel, ReferIntValue cLoad) throws Exception {
		
		TColor returnColor=new TColor(0.0f, 0.0f, 0.0f);
		float incr =(float) (1/Math.sqrt(superSamplingCoef));
		float ONE_OVER_SRAY_SUPERSAMPLING_COEF=(float) (1.0/superSamplingCoef);
		for (float fracx = (float)x ; fracx < x + 1.0f; fracx += incr )
		for (float fracy = (float)y ; fracy < y + 1.0f; fracy += incr )
		{
			//load
			cLoad .add(2);
			
			TVector direction = new TVector(new TPoint3D(fracx,fracy, 0), viewPoint);
			direction.normalize();
			returnColor.selfAdd(
					exposure(
							sceneRaytrace(lights, objects, rayInfinity, rayMaxLevel,
									new TRay(viewPoint, direction), cLoad)
					)
					.multiply(ONE_OVER_SRAY_SUPERSAMPLING_COEF));
		}
	
		return SetColor(sRGBEncode(returnColor));
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
	
	private TColor sceneRaytrace(List lights, List objects, float rayInfinity,
			int rayMaxLevel, TRay ray, ReferIntValue cLoad) throws Exception {
		
		TColor color=new TColor(0.0f, 0.0f, 0.0f);
		float coef = 1.0f;
		int level = 0;

		do 
		{
			ReferFloatValue t = new ReferFloatValue(rayInfinity);
			IObject object = this.intersectingObject(objects,ray, t, false, cLoad);
			if(object == null)
				break;

			IPoint3D newOrigin = (new TVector(ray.getOrigin())).add(ray.getDirection().multiply(t.getfValue()));
			TVector n = object.getShape().getNormalLine(newOrigin, ray.getDirection());

			//load
	      cLoad .add(9);
	

	      if (!n.normalize())
	         break;

	      //load
	      cLoad .add(13);

			for(int i = 0; i < lights.size(); i++)
			{
				ILight light=(ILight)lights.get(i);
				TVector d = new TVector(light.getOrigin(), newOrigin);
				float lightProjection = n.dot(d);

				//load
	         cLoad .add( 3 + 5);

				if(lightProjection <= 0.0f) continue;

				float t2 = d.norm();

				//load
	         cLoad .add( 15);

				if(t2 == 0.0f) continue;

				t2 = (float) (1.0 / t2); //InvSqrt(t);
				d.selfMultiply(t2);
				lightProjection = t2 * lightProjection;
//				if(!d.Normalize(t)) continue;
				
				TRay lightRay=new TRay(newOrigin, d);

				t.setfValue(rayInfinity);
				boolean objInShadow = (null != intersectingObject(objects, lightRay, t, true, cLoad));

				//load
	         cLoad .add( 12);

				if(objInShadow) continue;

				diffuse(color,lightRay.getDirection(), n, light.getIntensity(),
					object.getMaterialProperty().getDiffusion(), coef, cLoad);

//		inline void Specular(TVector lightDir, TVector rayDir, TVector surfNormal,
//			float lightProjection, int matPower, float coef, TColor specular, TColor lightIntensity)

				specular(color,lightRay.getDirection(), ray.getDirection(),
					n, lightProjection, object.getMaterialProperty().getPower(), coef,
					object.getMaterialProperty().getSpecular(), light.getIntensity(), cLoad);
			}

			coef *= object.getMaterialProperty().getReflection();
			float refl = 2.0f * ray.getDirection().dot(n);
			ray.setOrigin(newOrigin);
			ray.setDirection(new TVector(ray.getDirection(),n.multiply(refl)));
			level++;

			//load
	      cLoad .add(16);

		} while ((coef > 0.0f) && (level < rayMaxLevel));

	   return color;
	}
	
	
	
	private TColor specular(TColor color, TVector lightDir, TVector rayDir,
			TVector surfNormal, float lightProjection, int matPower, float coef,
			TIntensity specular, TIntensity lightIntensity, ReferIntValue cLoad) {
		
		float viewProjection = rayDir.dot(surfNormal);
		TVector BlinnDir = new TVector(lightDir , rayDir);
		float innerProduct = BlinnDir.dot(BlinnDir);

		//load
      cLoad .add(11);
      
		if(innerProduct == 0.0f)
			return color;

		float Blinn = (float) (invSqrt(innerProduct) * Math.max(lightProjection - viewProjection, 0.0f));
		Blinn = (float) (coef * Math.pow(Blinn, matPower));

		color.selfAdd(specular.multiply(lightIntensity).multiply(Blinn));

		//load
      cLoad .add( 24 );

		return color;
	}


	private TColor diffuse(TColor color, TVector lightDir, TVector surfNormal,
			TIntensity lcolor, TIntensity diffusion, float coef,
			ReferIntValue cLoad) {
		
		float lambert = lightDir.dot(surfNormal) * coef;
		color.selfAdd((lcolor.multiply(diffusion).multiply(lambert)));

		//load
      cLoad .add(15);

		return color;
	}


	private TColor sRGBEncode(TColor color){
		color.setR((float)((color.getR() <= 0.0031308f) ? (12.92f * color.getR()) :
			(1.055f * Math.pow(color.getR(), 0.4166667f) - 0.055f)));
		
		color.setG((float)((color.getG() <= 0.0031308f) ? (12.92f * color.getG()) :
			(1.055f * Math.pow(color.getG(), 0.4166667f) - 0.055f)));
		
		color.setB((float)((color.getB() <= 0.0031308f) ? (12.92f * color.getB()) :
			(1.055f * Math.pow(color.getB(), 0.4166667f) - 0.055f)));

		return color;
	}
	

	
	private TColor exposure(TColor color){
		float exposure = -1.00f;

		color.setR((float)(1.0f - Math.exp(color.getR() * exposure)));
		color.setG((float)(1.0f - Math.exp(color.getG() * exposure)));
		color.setB((float)(1.0f - Math.exp(color.getB() * exposure)));

		return color;
	}
	
	public static void main(String[] args){
		TColor color =new TColor(1,2,3);
		TRayTrace a= new TRayTrace();
		a.sRGBEncode(color);
		System.out.println(color);
	}
	
	public static float invSqrt(float x){
		float xhalf = 0.5f * x;
		int i = 0x5F3759DF - (Float.floatToIntBits(x)>>1);
		x = Float.intBitsToFloat(i);
		x = x * (1.5f - xhalf * x * x);
		return x;
	}

}
