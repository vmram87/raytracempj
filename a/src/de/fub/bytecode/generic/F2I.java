package de.fub.bytecode.generic;

/** 
 * F2I - Convert float to int
 * <PRE>Stack: ..., value -&gt; ..., result</PRE>
 *
 * @version $Id: F2I.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class F2I extends ConversionInstruction {
  public F2I() {
	super(F2I);
  }  
}