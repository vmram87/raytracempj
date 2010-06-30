package de.fub.bytecode.generic;

/** 
 * Denotes basic type such as int.
 *
 * @version $Id: BasicType.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public final class BasicType extends Type {
  /**
   * Constructor for basic types such as int, long, `void'
   *
   * @param type one of T_INT, T_BOOLEAN, ..., T_VOID
   * @see de.fub.bytecode.Constants
   */
  public BasicType(byte type) {
	super(type, SHORT_TYPE_NAMES[type]);

	if((type < T_BOOLEAN) || (type > T_VOID))
	  throw new ClassGenException("Invalid type: " + type);
  }    
  public static final BasicType getType(byte type) {
	switch(type) {
	case T_VOID:    return VOID;
	case T_BOOLEAN: return BOOLEAN;
	case T_BYTE:    return BYTE;
	case T_SHORT:   return SHORT;
	case T_CHAR:    return CHAR;
	case T_INT:     return INT;
	case T_LONG:    return LONG;
	case T_DOUBLE:  return DOUBLE;
	case T_FLOAT:   return FLOAT;

	default:
	  throw new ClassGenException("Invalid type: " + type);
	}
  }  
}