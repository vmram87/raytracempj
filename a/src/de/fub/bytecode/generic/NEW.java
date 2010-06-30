package de.fub.bytecode.generic;

import java.io.*;

/** 
 * NEW - Create new object
 * <PRE>Stack: ... -&gt; ..., objectref</PRE>
 *
 * @version $Id: NEW.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class NEW extends CPInstruction implements LoadClass, AllocationInstruction {
  /**
   * Empty constructor needed for the Class.newInstance() statement in
   * Instruction.readInstruction(). Not to be used otherwise.
   */
  NEW() {}  
  public NEW(int index) {
	super(NEW, index);
  }  
}