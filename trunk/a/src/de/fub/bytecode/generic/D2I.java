package de.fub.bytecode.generic;

/** 
 * D2I - Convert double to int
 * <PRE>Stack: ..., value.word1, value.word2 -&gt; ..., result</PRE>
 *
 * @version $Id: D2I.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class D2I extends ConversionInstruction {
  public D2I() {
	super(D2I);
  }  
}