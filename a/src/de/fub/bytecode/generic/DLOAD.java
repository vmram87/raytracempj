package de.fub.bytecode.generic;

/** 
 * DLOAD - Load double from local variable
 * Stack ... -> ..., result.word1, result.word2
 *
 * @version $Id: DLOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DLOAD extends LocalVariableInstruction implements PushInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  DLOAD() {
	super(DLOAD, DLOAD_0);
  }  
  public DLOAD(int n) {
	super(DLOAD, DLOAD_0, n);
  }  
}