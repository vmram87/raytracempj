package de.fub.bytecode.generic;

/** 
 * DUP2_X1 - Duplicate two top operand stack words and put three down
 * <PRE>Stack: ..., word3, word2, word1 -&gt; ..., word2, word1, word3, word2, word1</PRE>
 *
 * @version $Id: DUP2_X1.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DUP2_X1 extends StackInstruction {
  public DUP2_X1() {
	super(DUP2_X1);
  }  
}