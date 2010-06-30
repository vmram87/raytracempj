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
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.Select;
import de.fub.bytecode.generic.MethodGen;

import java.util.Vector;

class Switch extends Branch {
	Switch() {
		super(new Type[] { Type.INT } );
	}
Vector execute(MethodGen mGen, InstructionHandle ins, Stack s, Frame f) {
        System.out.println("&&&&&&&& "  + ins);
        System.out.println(s);
        System.out.println(f);
	Vector v = super.execute(mGen,ins,s,f);
	Select swIns = (Select) ins.getInstruction();
	InstructionHandle[] targets = swIns.getTargets();
	for (int i = 0; i < targets.length; i++)
		v.addElement(targets[i]);
	return v;
}
}





