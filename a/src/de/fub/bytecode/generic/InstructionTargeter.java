package de.fub.bytecode.generic;

/**
 * Denote that a class targets InstructionHandles within an InstructionList. Namely
 * the following implementers:
 *
 * @see BranchHandle
 * @see LocalVariableGen
 * @see CodeExceptionGen
 * @version $Id: InstructionTargeter.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public interface InstructionTargeter {
  public boolean containsTarget(InstructionHandle ih);  
  public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih);  
}