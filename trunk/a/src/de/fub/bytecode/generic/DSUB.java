package de.fub.bytecode.generic;

/** 
 * DSUB - Substract doubles
 * <PRE>Stack: ..., value1.word1, value1.word2, value2.word1, value2.word2 -&gt;</PRE>
 *        ..., result.word1, result.word2
 *
 * @version $Id: DSUB.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DSUB extends ArithmeticInstruction {
  public DSUB() {
	super(DSUB);
  }  
}