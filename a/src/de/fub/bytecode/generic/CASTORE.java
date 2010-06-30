package de.fub.bytecode.generic;

/** 
 * CASTORE -  Store into char array
 * <PRE>Stack: ..., arrayref, index, value -&gt; ...</PRE>
 *
 * @version $Id: CASTORE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class CASTORE extends ArrayInstruction {
  public CASTORE() {
	super(CASTORE);
  }  
}