package de.fub.bytecode.generic;

/** 
 * ILOAD - Load int from local variable
 * Stack ... -> ..., result
 *
 * @version $Id: ILOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ILOAD extends LocalVariableInstruction implements PushInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ILOAD() {
	super(ILOAD, ILOAD_0);
  }  
  public ILOAD(int n) {
	super(ILOAD, ILOAD_0, n);
  }  
}