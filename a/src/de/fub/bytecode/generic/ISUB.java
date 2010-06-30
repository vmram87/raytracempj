package de.fub.bytecode.generic;

/** 
 * ISUB - Substract ints
 * <PRE>Stack: ..., value1, value2 -&gt; result</PRE>
 *
 * @version $Id: ISUB.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ISUB extends ArithmeticInstruction {
  public ISUB() {
	super(ISUB);
  }  
}