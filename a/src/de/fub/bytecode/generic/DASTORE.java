package de.fub.bytecode.generic;

/** 
 * DASTORE -  Store into double array
 * <PRE>Stack: ..., arrayref, index, value.word1, value.word2 -&gt; ...</PRE>
 *
 * @version $Id: DASTORE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class DASTORE extends ArrayInstruction {
  public DASTORE() {
	super(DASTORE);
  }  
}