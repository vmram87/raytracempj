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
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;
import java.util.Enumeration;

class SimpleOpcode extends Noop {
	private Type[] pushes;
	private Type[] pops;
SimpleOpcode(Type[] pp, Type[] psh) {
	pushes = psh;
	pops = pp;
}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	for (int i = pops.length - 1; i >= 0; i--) {
		s.pop();
	}
	for (int i = 0; i < pushes.length; i++) {
		s.push(VirtualType.create(pushes[i]));
	}
	return super.execute(mGen,ins,s,f);
}
}
