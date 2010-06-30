package de.fub.bytecode.generic;

/**
 * Denotes an instruction to be a variable length instruction, such as
 * GOTO, JSR, LOOKUPSWITCH and TABLESWITCH.
 *
 * @version $Id: VariableLengthInstruction.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>

 * @see GOTO
 * @see JSR
 * @see LOOKUPSWITCH
 * @see TABLESWITCH
 */
public interface VariableLengthInstruction {}