package de.fub.bytecode.generic;

/** 
 * IF_ICMPGE - Branch if int comparison succeeds
 *
 * <PRE>Stack: ..., value1, value2 -&gt; ...</PRE>
 *
 * @version $Id: IF_ICMPGE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class IF_ICMPGE extends IfInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  IF_ICMPGE() {}  
  public IF_ICMPGE(InstructionHandle target) {
	super(IF_ICMPGE, target);
  }  
  /**
   * @return negation of instruction
   */
  public IfInstruction negate() {
	return new IF_ICMPLT(target);
  }  
}