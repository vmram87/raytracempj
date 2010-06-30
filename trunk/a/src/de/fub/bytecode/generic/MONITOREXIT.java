package de.fub.bytecode.generic;

/** 
 * MONITOREXIT - Exit monitor for object
 * <PRE>Stack: ..., objectref -&gt; ...</PRE>
 *
 * @version $Id: MONITOREXIT.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class MONITOREXIT extends Instruction {
  public MONITOREXIT() {
	super(MONITOREXIT, (short)1);
  }  
}