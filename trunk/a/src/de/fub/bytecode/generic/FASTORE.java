package de.fub.bytecode.generic;

/** 
 * FASTORE -  Store into float array
 * <PRE>Stack: ..., arrayref, index, value -&gt; ...</PRE>
 *
 * @version $Id: FASTORE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class FASTORE extends ArrayInstruction {
  public FASTORE() {
	super(FASTORE);
  }  
}