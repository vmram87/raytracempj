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

import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.MethodGen;
import java.util.Vector;


class Dup_x2 extends Noop {

Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
	VirtualType tp1 = s.pop();
	VirtualType tp2 = s.pop();
	VirtualType tp3 = s.pop();
	s.push(tp1);
	s.push(tp3);
	s.push(tp2);
	s.push(tp1);
	return super.execute(mGen, ins, s, f);
}
}
