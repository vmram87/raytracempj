package de.fub.bytecode.generic;

/** 
 * DRETURN -  Return double from method
 * <PRE>Stack: ..., value.word1, value.word2 -&gt; &lt;empty&gt;</PRE>
 *
 * @version $Id: DRETURN.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DRETURN extends ReturnInstruction {
  public DRETURN() {
	super(DRETURN);
  }  
}