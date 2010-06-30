package de.fub.bytecode.generic;

/** 
 * SALOAD - Load short from array
 * <PRE>Stack: ..., arrayref, index -&gt; ..., value</PRE>
 *
 * @version $Id: SALOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class SALOAD extends ArrayInstruction {
  public SALOAD() {
	super(SALOAD);
  }  
}