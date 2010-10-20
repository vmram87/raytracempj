package org.raytrace.object;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.raytrace.vector.IPoint3D;
import org.raytrace.vector.impl.ReferFloatValue;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TRay;
import org.raytrace.vector.impl.TVector;

public abstract class TShape implements IShape {

	protected List paramList=new ArrayList<String>();
	protected Map getMethodMap=new HashMap<String,String>();
	protected Map setMethodMap=new HashMap<String,String>();

	@Override
	public List getParamNameList() {
		return this.paramList;
	}

	@Override
	public Object getParamObject(String paramName) throws Exception {
		return this.invokeMethod((String)getMethodMap.get(paramName), null);
	}

	@Override
	public void setParamValue(String paramName, Object value) throws Exception {
		Object[] args={value};
		this.invokeMethod((String)setMethodMap.get(paramName), args);
	}
	
	private Object invokeMethod(String methodName, Object[] args) throws Exception {   
	    Class ownerClass = this.getClass();   
	    if(args==null){
	    	Method method = ownerClass.getMethod(methodName, null);      
		    return method.invoke(this, null);    
	    }
	    else{
		    Class[] argsClass = new Class[args.length];   
		    for (int i = 0, j = args.length; i < j; i++) {   
		         argsClass[i] = args[i].getClass();  
		    }
		    Method method = ownerClass.getMethod(methodName, argsClass);      
		 	return method.invoke(this, args);    
	    }    
	    
	}
	
	@Override
	public IShape getRandomShape() {
		// TODO Auto-generated method stub
		return null;
	}
}
