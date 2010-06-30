package de.fub.bytecode.generic;

/** 
 * FNEG - Negate float
 * <PRE>Stack: ..., value -&gt; ..., result</PRE>
 *
 * @version $Id: FNEG.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class FNEG extends ArithmeticInstruction {
  public FNEG() {
	super(FNEG);
  }  
}