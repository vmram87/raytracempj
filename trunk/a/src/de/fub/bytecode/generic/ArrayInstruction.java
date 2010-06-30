package de.fub.bytecode.generic;

/**
 * Super class for the xRETURN family of instructions.
 *
 * @version $Id: ArrayInstruction.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public abstract class ArrayInstruction extends Instruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ArrayInstruction() {}  
  /**
   * @param tag opcode of instruction
   */
  protected ArrayInstruction(short tag) {
	super(tag, (short)1);
  }  
}