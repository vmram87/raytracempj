package de.fub.bytecode.generic;

/** 
 * IFNONNULL - Branch if reference is not null
 *
 * <PRE>Stack: ..., reference -&gt; ...</PRE>
 *
 * @version $Id: IFNONNULL.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class IFNONNULL extends IfInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  IFNONNULL() {}  
  public IFNONNULL(InstructionHandle target) {
	super(IFNONNULL, target);
  }  
  /**
   * @return negation of instruction
   */
  public IfInstruction negate() {
	return new IFNULL(target);
  }  
}