package de.fub.bytecode.generic;

/**
 * SASTORE - Store into short array
 * <PRE>Stack: ..., arrayref, index, value -&gt; ...</PRE>
 *
 * @version $Id: SASTORE.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class SASTORE extends ArrayInstruction {
  public SASTORE() {
	super(SASTORE);
  }  
}