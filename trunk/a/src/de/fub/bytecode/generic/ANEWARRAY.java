package de.fub.bytecode.generic;

/** 
 * ANEWARRAY -  Create new array of references
 * <PRE>Stack: ..., count -&gt; ..., arrayref</PRE>
 *
 * @version $Id: ANEWARRAY.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ANEWARRAY extends CPInstruction
  implements LoadClass, AllocationInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ANEWARRAY() {}  
  public ANEWARRAY(int index) {
	super(ANEWARRAY, index);
  }  
}