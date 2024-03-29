package de.fub.bytecode.generic;

/** 
 * ASTORE - Store reference into local variable
 * Stack ..., objectref -> ... 
 *
 * @version $Id: ASTORE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ASTORE extends LocalVariableInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ASTORE() {
	super(ASTORE, ASTORE_0);
  }  
  public ASTORE(int n) {
	super(ASTORE, ASTORE_0, n);
  }  
}