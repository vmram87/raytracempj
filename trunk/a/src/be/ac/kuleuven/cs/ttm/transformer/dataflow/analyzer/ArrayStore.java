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

class ArrayStore extends Noop {
	private Type componentType;

ArrayStore(Type cType) {
	componentType = cType;
}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	// a real byte code verifier now checks the contents of the stack with the type of ins
	if (componentType.getSize() > 1)
		s.pop();
	VirtualType element = s.pop(); // pop element to store
	s.pop(); // pop array index of stack
	VirtualReferenceType array = (VirtualReferenceType) s.pop(); // pop array
	array.assignComponent(element);
	return super.execute(mGen,ins,s,f);
}
}
