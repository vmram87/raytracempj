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
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.NEWARRAY;
import de.fub.bytecode.generic.BasicType;
import de.fub.bytecode.generic.ArrayType;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.ConstantString;
import de.fub.bytecode.generic.MethodGen;

class Newarray extends Noop {
Newarray() {
}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	ConstantPool cp = mGen.getConstantPool().getConstantPool();
	NEWARRAY naIns = (NEWARRAY) ins.getInstruction();
	for (int i = 0; i < naIns.consumeStack(); i++)
		s.pop(); // pop array size
	s.push(new VirtualReferenceType(new ArrayType(new BasicType(naIns.getTypeTag()), naIns.consumeStack())));
	return super.execute(mGen, ins, s, f);
}
}
