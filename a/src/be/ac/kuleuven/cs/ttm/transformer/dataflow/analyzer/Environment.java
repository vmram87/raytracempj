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
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualUninitializedType;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Registry;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.Constants;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.classfile.Utility;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.InvokeInstruction;
import de.fub.bytecode.generic.CodeExceptionGen;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

class Environment {

	Vector changedInstructions;
  // the registry contains the frame and stack BEFORE the execution of each instruction
  Registry registry;
  MethodGen methodGen;  
public Environment(MethodGen m) {
	methodGen = m;
	InstructionList insList = m.getInstructionList();
	registry = new Registry();
	changedInstructions = new Vector();
	changedInstructions.addElement(insList.getStart());
	Frame curFrame = new Frame();
	Stack curStack = new Stack();
	Type[] pTypes = m.getArgTypes();
	int i = 0;
	if (!m.isStatic()) {
		if (m.getMethodName().equals("<init>")) // constructor
			curFrame.put(new VirtualUninitializedType(new ObjectType(m.getClassName()), null), i++); // this pointer
		else
			curFrame.put(VirtualType.create(new ObjectType(m.getClassName())), i++); // this pointer
	}
	for (int j = 0; j < pTypes.length; j++) {
		curFrame.put(VirtualType.create(pTypes[j]), i++);
		if (pTypes[j].getSize() > 1) i++;
	}
	registry.register(insList.getStart(), curFrame);
	registry.register(insList.getStart(), curStack);
}
public void addChanged(InstructionHandle ins) {
	if (!changedInstructions.contains(ins))
		changedInstructions.addElement(ins);
}
public InstructionHandle getChangedInstruction() {
	if (changedInstructions.isEmpty() )
		return null;
	InstructionHandle ins = (InstructionHandle) changedInstructions.firstElement();
	changedInstructions.removeElementAt(0);
	return ins;
}
public Frame getFrame(InstructionHandle ins) {
	return registry.getFrame(ins);
}
public MethodGen getMethodGen() {
	return methodGen;
}
public Registry getRegistry() {
	return registry;
}
public Stack getStack(InstructionHandle ins) {
	return registry.getStack(ins);
}
public boolean merge(InstructionHandle ins, Frame f) {
	Frame curFrame = getFrame(ins);
	if (curFrame == null) {
		registry.register(ins, f);
		return true;
	}
	return curFrame.merge(f);
}
public boolean merge(InstructionHandle ins, Stack s) {
	Stack curStack = getStack(ins);
	if (curStack == null) {
		registry.register(ins, s);
		return true;
	}
	return curStack.merge(s);
}
}
