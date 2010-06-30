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
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualType;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualReferenceType;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Registry;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.InvokeInstruction;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.INVOKESTATIC;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.ConstantCP;
import de.fub.bytecode.classfile.ConstantNameAndType;
import de.fub.bytecode.classfile.ConstantUtf8;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;

class Invoke extends Noop {
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	ConstantPool cp = mGen.getConstantPool().getConstantPool();
	ConstantPoolGen cpg = new ConstantPoolGen(cp);	
	InvokeInstruction invIns = (InvokeInstruction) ins.getInstruction();
	String className = Util.getObjectType(cp, invIns.getIndex()).getClassName();
	Type[] pTypes = Util.getParamTypes(cp, invIns.getIndex());
	for (int i = pTypes.length - 1; i >= 0; i--) {
		s.pop(); // remove parameter
		if (pTypes[i].getSize() > 1)
			s.pop();
	}
	if (!(invIns instanceof INVOKESTATIC)) {
		VirtualType vrt = (VirtualType) s.pop(); // remove reference to invokee
		String methodName = invIns.getMethodName(cpg);
		if (methodName.equals("<init>")) {
			VirtualReferenceType initializedVrt = new VirtualReferenceType((ObjectType)vrt.getRealType());
			s.replace(vrt, initializedVrt);
		}
	}
	Type t = Util.getReturnType(cp, invIns.getIndex());
	if (!t.equals(Type.VOID)) {
		s.push(VirtualType.create(t));
		if (t.getSize() > 1)
			s.push(VirtualType.create(Type.VOID));
	}
	return super.execute(mGen, ins, s, f);
}
}
