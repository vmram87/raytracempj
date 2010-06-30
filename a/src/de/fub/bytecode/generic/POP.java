package de.fub.bytecode.generic;

/**
 * POP - Pop top operand stack word
 *
 * <PRE>Stack: ..., word -&gt; ...</PRE>
 *
 * @version $Id: POP.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class POP extends StackInstruction {
  public POP() {
	super(POP);
  }  
}