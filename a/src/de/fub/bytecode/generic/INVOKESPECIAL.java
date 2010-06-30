package de.fub.bytecode.generic;

/** 
 * INVOKESPECIAL - Invoke instance method; special handling for superclass, private
 * and instance initialization method invocations
 *
 * <PRE>Stack: ..., objectref, [arg1, [arg2 ...]] -&gt; ...</PRE>
 *
 * @version $Id: INVOKESPECIAL.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class INVOKESPECIAL extends InvokeInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  INVOKESPECIAL() {}  
  public INVOKESPECIAL(int index) {
	super(INVOKESPECIAL, index);
  }  
}