package de.fub.bytecode.generic;

/** 
 * Denotes reference such as java.lang.String.
 *
 * @version $Id: ObjectType.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public final class ObjectType extends ReferenceType {
  private String class_name; // Class name of type

  /**
   * @param class_name fully qualified class name, e.g. java.lang.String
   */ 
  public ObjectType(String class_name) {
	super(T_REFERENCE, "L" + class_name.replace('.', '/') + ";");
	this.class_name = class_name;
  }  
  public boolean equals(Object type) {
	return (type instanceof ObjectType)?
	  ((ObjectType)type).class_name.equals(class_name) : false;
  }  
  /**
   * @return name of referenced class
   */
  public String getClassName() { return class_name; }  
}