package de.fub.bytecode.generic;

/** 
 * F2D - Convert float to double
 * <PRE>Stack: ..., value -&gt; ..., result.word1, result.word2</PRE>
 *
 * @version $Id: F2D.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class F2D extends ConversionInstruction {
  public F2D() {
	super(F2D);
  }  
}