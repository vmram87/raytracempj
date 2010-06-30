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
import de.fub.bytecode.generic.BranchInstruction;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;

class Goto extends Opcode {

Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	Vector v = new Vector();
	BranchInstruction bins = (BranchInstruction) ins.getInstruction();
	v.addElement(bins.getTarget());
	return v;
}
}
