package de.fub.bytecode.generic;

/**
 * I2L - Convert int to long
 * <PRE>Stack: ..., value -&gt; ..., result.word1, result.word2</PRE>
 *
 * @version $Id: I2L.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class I2L extends ConversionInstruction {
  public I2L() {
	super(I2L);
  }  
}