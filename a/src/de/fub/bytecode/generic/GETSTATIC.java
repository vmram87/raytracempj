package de.fub.bytecode.generic;

import de.fub.bytecode.Constants;

/** 
 * GETSTATIC - Fetch static field from class
 * <PRE>Stack: ..., -&gt; ..., value</PRE>
 * OR
 * <PRE>Stack: ..., -&gt; ..., value.word1, value.word2</PRE>
 *
 * @version $Id: GETSTATIC.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class GETSTATIC extends FieldInstruction implements PushInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  GETSTATIC() {}  
  public GETSTATIC(int index) {
	super(Constants.GETSTATIC, index);
  }  
  public int produceStack(ConstantPoolGen cpg)
   { return getFieldSize(cpg); }   
}