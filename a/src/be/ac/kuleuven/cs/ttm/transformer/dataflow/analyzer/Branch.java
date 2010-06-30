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
import java.util.Vector;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.BranchInstruction;

class Branch extends SimpleOpcode {
Branch(Type[] pops) {
	super(pops, new Type[] {});
}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	Vector v = super.execute(mGen,ins,s,f);
	BranchInstruction bins = (BranchInstruction) ins.getInstruction();
	v.addElement(bins.getTarget());
	return v;
}
}
