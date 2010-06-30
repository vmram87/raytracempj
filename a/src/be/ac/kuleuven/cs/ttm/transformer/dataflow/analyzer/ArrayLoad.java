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

package be.ac.kuleuven.cs.ttm.transformer.dataflow.analyzer;

import be.ac.kuleuven.cs.ttm.transformer.dataflow.Stack;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Frame;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualType;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualReferenceType;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.ArrayInstruction;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;

class ArrayLoad extends Noop {

Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	// a real byte code verifier now checks the contents of the stack with the type of ins
	s.pop(); // pop array index of stack
	VirtualReferenceType arrayType = (VirtualReferenceType) s.pop();
	VirtualType compType = arrayType.getComponentType();
	s.push(compType);
	if (compType.getSize() > 1)
		s.push(VirtualType.create(Type.VOID));
	return super.execute(mGen,ins,s,f);
}
}
