package de.fub.bytecode.generic;

import de.fub.bytecode.Constants;

/**
 * Super class for the xRETURN family of instructions.
 *
 * @version $Id: ReturnInstruction.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public abstract class ReturnInstruction extends Instruction implements Constants {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ReturnInstruction() {}  
  /**
   * @param tag opcode of instruction
   */
  protected ReturnInstruction(short tag) {
	super(tag, (short)1);
  }  
  public Type getType() {
	switch(tag) {
	  case IRETURN : return Type.INT;
	  case LRETURN : return Type.LONG;
	  case FRETURN : return Type.FLOAT;
	  case DRETURN : return Type.DOUBLE;
	  case ARETURN : return Type.NULL;
 
	default: // Never reached
	  throw new RuntimeException("Unknown type " + tag);
	}
  }  
}