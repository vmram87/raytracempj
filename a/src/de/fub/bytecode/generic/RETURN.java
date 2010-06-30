package de.fub.bytecode.generic;

/** 
 * RETURN -  Return from void method
 * <PRE>Stack: ... -&gt; &lt;empty&gt;</PRE>
 *
 * @version $Id: RETURN.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class RETURN extends ReturnInstruction {
  public RETURN() {
	super(RETURN);
  }  
}