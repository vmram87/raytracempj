package de.fub.bytecode.generic;

/** 
 * LXOR - Bitwise XOR long
 * <PRE>Stack: ..., value1, value2 -&gt; ..., result</PRE>
 *
 * @version $Id: LXOR.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @authXOR  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class LXOR extends ArithmeticInstruction {
  public LXOR() {
	super(LXOR);
  }  
}