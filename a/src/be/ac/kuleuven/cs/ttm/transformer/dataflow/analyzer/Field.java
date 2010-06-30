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
import java.util.Vector;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualType;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.FieldInstruction;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ReferenceType;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.ConstantString;
import de.fub.bytecode.classfile.ConstantFieldref;
import de.fub.bytecode.classfile.ConstantNameAndType;
import de.fub.bytecode.classfile.ConstantUtf8;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;


class Field extends Noop {
	private boolean isPut;
	private boolean isStatic;
	Field(boolean isP, boolean isS) {
		isPut = isP;
		isStatic = isS;
	}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	ConstantPool cp = mGen.getConstantPool().getConstantPool();
	FieldInstruction fIns = (FieldInstruction) ins.getInstruction();
	Type t = Util.getFieldType(cp, fIns.getIndex());
	if (!isStatic) {
		s.pop(); // remove object from the stack
	}
	if (isPut) {
		s.pop();
		if (t.getSize() > 1)
			s.pop();
	} else {
		s.push(VirtualType.create(t));
		if (t.getSize() > 1)
			s.push(VirtualType.create(Type.VOID));
	}
	return super.execute(mGen,ins,s,f);
}
}
