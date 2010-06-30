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
import de.fub.bytecode.generic.LDC;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.CPInstruction;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.Constant;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;

class Ldc extends Noop {
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	CPInstruction lins = (CPInstruction) ins.getInstruction();
	ConstantPool cpool = mGen.getConstantPool().getConstantPool();
	switch (cpool.getConstant(lins.getIndex()).getTag()) {
		case ConstantPool.CONSTANT_Integer :
			s.push(VirtualType.create(Type.INT));
			break;
		case ConstantPool.CONSTANT_String :
			s.push(VirtualType.create(Type.STRING));
			break;
		case ConstantPool.CONSTANT_Float :
			s.push(VirtualType.create(Type.FLOAT));
			break;
		case ConstantPool.CONSTANT_Double :
			s.push(VirtualType.create(Type.DOUBLE));
			s.push(VirtualType.create(Type.VOID));
			break;
		case ConstantPool.CONSTANT_Long :
			s.push(VirtualType.create(Type.LONG));
			s.push(VirtualType.create(Type.VOID));
			break;
		default :
			throw new VerifyError(lins.toString());
	}
	return super.execute(mGen,ins,s,f);
}
}
