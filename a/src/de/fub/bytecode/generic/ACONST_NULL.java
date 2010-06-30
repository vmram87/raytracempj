package de.fub.bytecode.generic;

/** 
 * ACONST_NULL -  Push null
 * <PRE>Stack: ... -&gt; ..., null</PRE>
 *
 * @version $Id: ACONST_NULL.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ACONST_NULL extends Instruction implements PushInstruction {
  public ACONST_NULL() {
	super(ACONST_NULL, (short)1);
  }      
}