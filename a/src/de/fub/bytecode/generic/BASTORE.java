package de.fub.bytecode.generic;

/** 
 * BASTORE -  Store into byte or boolean array
 * <PRE>Stack: ..., arrayref, index, value -&gt; ...</PRE>
 *
 * @version $Id: BASTORE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class BASTORE extends ArrayInstruction {
  public BASTORE() {
	super(BASTORE);
  }  
}