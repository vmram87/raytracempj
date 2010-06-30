/** This file is part of the BRAKES framework v0.3
  * Developed by: 
  *   Distributed Systems and Computer Networks Group (DistriNet)
  *   Katholieke Universiteit Leuven  
  *   Department of Computer Science
  *   Celestijnenlaan 200A
  *   3001 Leuven (Heverlee)
  *   Belgium
  * Project Manager and Principal Investigator: 
  *                        Pierre Verbaeten(pv@cs.kuleuven.ac.be)
  * Licensed under the Academic Free License version 1.1 (see COPYRIGHT)
  */

package be.ac.kuleuven.cs.ttm.transformer.dataflow;

import de.fub.bytecode.generic.BasicType;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.ReferenceType;
import de.fub.bytecode.generic.ArrayType;
import de.fub.bytecode.generic.InstructionHandle;

public interface FrameVisitor {

public void visitBasicType(BasicType t, int pos);

void visitNull(int pos);

public void visitReferenceType(ReferenceType t, int pos);

public void visitUninitializedType(ObjectType t, InstructionHandle ins, int pos);
}
