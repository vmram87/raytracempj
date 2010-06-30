package de.fub.bytecode.generic;

/**
 * ISHL - Arithmetic shift left int
 * <PRE>Stack: ..., value1, value2 -&gt; ..., result</PRE>
 *
 * @version $Id: ISHL.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ISHL extends ArithmeticInstruction {
  public ISHL() {
	super(ISHL);
  }  
}