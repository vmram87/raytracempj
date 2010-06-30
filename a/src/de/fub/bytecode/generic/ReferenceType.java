package de.fub.bytecode.generic;

/** 
 * Super class for objects and arrays.
 *
 * @version $Id: ReferenceType.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ReferenceType extends Type {
  /** Class is non-abstract but not instantiable from the outside
   */
  ReferenceType() {
	super(T_OBJECT, "<null object>");
  }  
  protected ReferenceType(byte t, String s) {
	super(t, s);
  }  
  public String toString() { return signature; }  
}