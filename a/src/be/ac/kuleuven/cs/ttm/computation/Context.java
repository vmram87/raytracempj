/** This file is part of the BRAKES framework v0.3
  * Developed by: 
  *   Distributed Systems and Computer Networks Group (DistriNet)
  *   Katholieke Universiteit Leuven  
  *   Department of Computer Science
  *   Celestijnenlaan 200A
  *   3001 Leuven (Heverlee)
  *   Belgium
  * Project Manager and Principal Investigator: 
  *                        Pierre Verbaeten(pv@cs.kuleuven.ac.be)
  * Licensed under the Academic Free License version 1.1 (see COPYRIGHT)
  */

package be.ac.kuleuven.cs.ttm.computation;

/**
 * Besides computation, the context is used to capture the state of
 * 	the JVM stack.
 *
 * @version $Id: Context.java,v 1.2 2000/11/06 14:47:35 cvs Exp $
 * @author <A HREF="http://www.cs.kuleuven.ac.be/~tim">Tim Coninx</A>
 */
public class Context implements java.io.Serializable {

  private int[] istack;
  private float[] fstack;
  private double[] dstack;
  private long[] lstack;
  private Object[] astack;
  private Object[] tstack; // contains the this variables
  private int iTop, fTop, dTop, lTop, aTop, tTop;

  public Context() {
	istack = new int[10];
	lstack = new long[5];
	dstack = new double[5];
	fstack = new float[5];
	astack = new Object[10];
	tstack = new Object[5];
  }

  static public double curPopDouble() {
    return ThreadMapping.getInstance().getContext().popDouble();
  }  

  static public float curPopFloat() {
    return ThreadMapping.getInstance().getContext().popFloat();
  }

  static public int curPopInt() {
    int i = ThreadMapping.getInstance().getContext().popInt();
    return i;
  }

  static public long curPopLong() {
    return ThreadMapping.getInstance().getContext().popLong();
  }

  static public Object curPopObject() {
    Object o = ThreadMapping.getInstance().getContext().popObject();
    return o;
  }

  static public Object curPopThis() {
    Object o = ThreadMapping.getInstance().getContext().popThis();
    return o;
  }

  static public void curPushDouble(double d) {
    ThreadMapping.getInstance().getContext().pushDouble(d);
  }

  static public void curPushFloat(float f) {
    ThreadMapping.getInstance().getContext().pushFloat(f);
  }

  static public void curPushInt(int i) {
    ThreadMapping.getInstance().getContext().pushInt(i);
  }

  static public void curPushLong(long l) {
    ThreadMapping.getInstance().getContext().pushLong(l);
  }

  static public void curPushObject(Object o) {
    ThreadMapping.getInstance().getContext().pushObject(o);
  }

  static public void curPushThis(Object o) {
    ThreadMapping.getInstance().getContext().pushThis(o);
  }

  static public Context getCurrentContext() {
    return ThreadMapping.getInstance().getContext();
  }  

  public double popDouble() {
    return dstack[--dTop];
  }  

  public float popFloat() {
    return fstack[--fTop];
  }  

  public int popInt() {
    Debug.println("istack (Context " + this.toString() + " is size : " + iTop);
    return istack[--iTop];
  }  

  public long popLong() {
    return lstack[--lTop];
  }

  public Object popObject() {
    Debug.println("astack (Context " + this.toString() + " is size : " + aTop);
    return astack[--aTop];
  }  

  public Object popThis() {
    Debug.println("tstack (Context " + this.toString() + " is size : " + tTop);
    return tstack[--tTop];
  }  

  public void pushDouble(double d) {
	dstack[dTop++] = d;
	if (dTop == dstack.length) {
	  double[] hlp = new double[dstack.length+10];
	  System.arraycopy(dstack, 0, hlp, 0, dstack.length);
	  dstack = hlp;
	}
  }  

  public void pushFloat(float f) {
	fstack[fTop++] = f;
	if (fTop == fstack.length) {
	  float[] hlp = new float[fstack.length+10];
	  System.arraycopy(fstack, 0, hlp, 0, fstack.length);
	  fstack = hlp;
	}
  }  

  public void pushInt(int i) {
	istack[iTop++] = i;
	Debug.println("pushed int " + i + " on iTop (size " + iTop + ") of Context " + this.toString());
	if (iTop == istack.length) {
	  int[] hlp = new int[istack.length+10];
	  System.arraycopy(istack, 0, hlp, 0, istack.length);
	  istack = hlp;
	}
  }  

  public void pushLong(long l) {
	lstack[lTop++] = l;
	if (lTop == lstack.length) {
	  long[] hlp = new long[lstack.length+10];
	  System.arraycopy(lstack, 0, hlp, 0, lstack.length);
	  lstack = hlp;
	}
  }  

  public void pushObject(Object o) {
	astack[aTop++] = o;
	if (aTop == astack.length) {
	  Object[] hlp = new Object[astack.length+10];
	  System.arraycopy(astack, 0, hlp, 0, astack.length);
	  astack = hlp;
	}
  }  

  public void pushThis(Object o) {
	tstack[tTop++] = o;
	if (tTop == tstack.length) {
		Object[] hlp = new Object[tstack.length + 10];
		System.arraycopy(tstack, 0, hlp, 0, tstack.length);
		tstack = hlp;
	}
  }
}
