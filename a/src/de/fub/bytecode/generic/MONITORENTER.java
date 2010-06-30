package de.fub.bytecode.generic;

/** 
 * MONITORENTER - Enter monitor for object
 * <PRE>Stack: ..., objectref -&gt; ...</PRE>
 *
 * @version $Id: MONITORENTER.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class MONITORENTER extends Instruction {
  public MONITORENTER() {
	super(MONITORENTER, (short)1);
  }  
}