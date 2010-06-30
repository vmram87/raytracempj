package de.fub.bytecode.generic;

/** 
 * ALOAD - Load reference from local variable
 * Stack ... -> ..., objectref
 *
 * @version $Id: ALOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ALOAD extends LocalVariableInstruction implements PushInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ALOAD() {
	super(ALOAD, ALOAD_0);
  }  
  public ALOAD(int n) {
	super(ALOAD, ALOAD_0, n);
  }  
}