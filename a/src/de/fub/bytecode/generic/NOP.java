package de.fub.bytecode.generic;

/**
 * NOP - Do nothing
 *
 * @version $Id: NOP.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class NOP extends Instruction {
  public NOP() {
	super(NOP, (short)1);
  }  
}