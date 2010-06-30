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

import be.ac.kuleuven.cs.ttm.transformer.Util;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Stack;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Frame;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualReferenceType;
import java.util.Vector;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.CPInstruction;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ReferenceType;
import de.fub.bytecode.generic.ArrayType;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.ConstantString;
import de.fub.bytecode.generic.MethodGen;

class Multianewarray extends Noop {
Multianewarray() {
}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	ConstantPool cp = mGen.getConstantPool().getConstantPool();
	CPInstruction cpIns = (CPInstruction) ins.getInstruction();
	ReferenceType rType = Util.getClassReference(cp, cpIns.getIndex());
	for (int i = 0; i < cpIns.consumeStack(null); i++)
		s.pop(); // pop array size
	s.push(new VirtualReferenceType(rType));
	return super.execute(mGen, ins, s, f);
}
}
