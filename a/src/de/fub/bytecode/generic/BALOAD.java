package de.fub.bytecode.generic;

/** 
 * BALOAD - Load byte or boolean from array
 * <PRE>Stack: ..., arrayref, index -&gt; ..., value</PRE>
 *
 * @version $Id: BALOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class BALOAD extends ArrayInstruction {
  public BALOAD() {
	super(BALOAD);
  }  
}