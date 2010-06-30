package de.fub.bytecode.generic;

/** 
 * DALOAD - Load double from array
 * <PRE>Stack: ..., arrayref, index -&gt; ..., result.word1, result.word2</PRE>
 *
 * @version $Id: DALOAD.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DALOAD extends ArrayInstruction {
  public DALOAD() {
	super(DALOAD);
  }  
}