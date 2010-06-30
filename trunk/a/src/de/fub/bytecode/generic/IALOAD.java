package de.fub.bytecode.generic;

/** 
 * IALOAD - Load int from array
 * <PRE>Stack: ..., arrayref, index -&gt; ..., value</PRE>
 *
 * @version $Id: IALOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class IALOAD extends ArrayInstruction {
  public IALOAD() {
	super(IALOAD);
  }  
}