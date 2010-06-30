package de.fub.bytecode.generic;

/** 
 * LALOAD - Load long from array
 * <PRE>Stack: ..., arrayref, index -&gt; ..., value1, value2</PRE>
 *
 * @version $Id: LALOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class LALOAD extends ArrayInstruction {
  public LALOAD() {
	super(LALOAD);
  }  
}