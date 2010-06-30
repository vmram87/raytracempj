package de.fub.bytecode.generic;

/** 
 * SWAP - Swa top operand stack word
 * <PRE>Stack: ..., word2, word1 -&gt; ..., word1, word2</PRE>
 *
 * @version $Id: SWAP.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class SWAP extends StackInstruction {
  public SWAP() {
	super(SWAP);
  }  
}