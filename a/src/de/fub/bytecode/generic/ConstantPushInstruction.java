package de.fub.bytecode.generic;

/**
 * Denotes a push instruction that produces a literal on the stack
 * such as  SIPUSH, BIPUSH, ICONST, etc.
 *
 * @version $Id: ConstantPushInstruction.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>

 * @see ICONST
 * @see SIPUSH
 */
public interface ConstantPushInstruction extends PushInstruction {
  public Number getValue();  
}