package de.fub.bytecode.generic;

/** 
 * Denotes array type, such as int[][]
 *
 * @version $Id: ArrayType.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public final class ArrayType extends ReferenceType {
  private int  dimensions;
  private Type basic_type;

  /**
   * Convenience constructor for array type, e.g. int[]
   *
   * @param type array type, e.g. T_INT
   */ 
  public ArrayType(byte type, int dimensions) {
	this(BasicType.getType(type), dimensions);
  }  
  /**
   * Constructor for array of given type
   *
   * @param type type of array (may be an array itself)
   */ 
  public ArrayType(Type type, int dimensions) {
	super(T_ARRAY, "<dummy>");

	if((dimensions < 1) || (dimensions > MAX_BYTE))
	  throw new ClassGenException("Invalid number of dimensions: " + dimensions);

	switch(type.getType()) {
	case T_ARRAY:
	  ArrayType array = (ArrayType)type;
	  this.dimensions = dimensions + array.dimensions;
	  basic_type      = array.basic_type;
	  break;
	  
	case T_VOID:
	  throw new ClassGenException("Invalid type: void[]");

	default: // Basic type or reference
	  this.dimensions = dimensions;
	  basic_type = type;
	  break;
	}

	StringBuffer buf = new StringBuffer();
	for(int i=0; i < dimensions; i++)
	  buf.append('[');

	buf.append(basic_type.getSignature());

	signature = buf.toString();
  }  
  /**
   * Convenience constructor for reference array type, e.g. Object[]
   *
   * @param class_name complete name of class (java.lang.String, e.g.)
   */ 
  public ArrayType(String class_name, int dimensions) {
	this(new ObjectType(class_name), dimensions);
  }  
  public boolean equals(Object type) {
	if(type instanceof ArrayType) {
	  ArrayType array = (ArrayType)type;
	  return (array.dimensions == dimensions) && array.basic_type.equals(basic_type);
	}
	else
	  return false;
  }  
  /**
   * @return basic type of array, i.e. for int[][][] the basic type is int
   */
  public Type getBasicType() {
	return basic_type;
  }  
  /**
   * @return number of dimensions of array
   */
  public int getDimensions() { return dimensions; }  
  /**
   * @return element type of array, i.e. for int[][][] the element type is int[][]
   */
  public Type getElementType() {
	if(dimensions == 1)
	  return basic_type;
	else
	  return new ArrayType(basic_type, dimensions - 1);
  }  
}