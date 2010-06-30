package de.fub.bytecode.generic;

/**
 * Super class for the x2y family of instructions.
 *
 * @version $Id: ConversionInstruction.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public abstract class ConversionInstruction extends Instruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  ConversionInstruction() {}  
  /**
   * @param tag opcode of instruction
   */
  protected ConversionInstruction(short tag) {
	super(tag, (short)1);
  }  
}