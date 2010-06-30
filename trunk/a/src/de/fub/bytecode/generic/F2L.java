package de.fub.bytecode.generic;

/** 
 * F2L - Convert float to long
 * <PRE>Stack: ..., value -&gt; ..., result.word1, result.word2</PRE>
 *
 * @version $Id: F2L.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class F2L extends ConversionInstruction {
  public F2L() {
	super(F2L);
  }  
}