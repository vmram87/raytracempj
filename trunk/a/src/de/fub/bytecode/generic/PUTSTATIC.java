package de.fub.bytecode.generic;

import de.fub.bytecode.Constants;

/** 
 * PUTSTATIC - Put static field in class
 * <PRE>Stack: ..., objectref, value -&gt; ...</PRE>
 * OR
 * <PRE>Stack: ..., objectref, value.word1, value.word2 -&gt; ...</PRE>
 *
 * @version $Id: PUTSTATIC.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class PUTSTATIC extends FieldInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  PUTSTATIC() {}  
  public PUTSTATIC(int index) {
	super(Constants.PUTSTATIC, index);
  }  
  public int consumeStack(ConstantPoolGen cpg)
   { return getFieldSize(cpg); }   
}