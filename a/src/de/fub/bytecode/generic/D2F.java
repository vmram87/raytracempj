package de.fub.bytecode.generic;

/** 
 * D2F - Convert double to float
 * <PRE>Stack: ..., value.word1, value.word2 -&gt; ..., result</PRE>
 *
 * @version $Id: D2F.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class D2F extends ConversionInstruction {
  public D2F() {
	super(D2F);
  }  
}