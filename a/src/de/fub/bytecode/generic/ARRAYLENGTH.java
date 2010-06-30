package de.fub.bytecode.generic;

/** 
 * ARRAYLENGTH -  Get length of array
 * <PRE>Stack: ..., arrayref -&gt; ..., length</PRE>
 *
 * @version $Id: ARRAYLENGTH.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ARRAYLENGTH extends Instruction {
  public ARRAYLENGTH() {
	super(ARRAYLENGTH, (short)1);
  }  
}