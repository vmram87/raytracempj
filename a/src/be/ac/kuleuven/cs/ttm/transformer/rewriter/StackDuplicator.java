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

package be.ac.kuleuven.cs.ttm.transformer.rewriter;

import be.ac.kuleuven.cs.ttm.transformer.dataflow.StackVisitor;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Frame;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ReferenceType;
import de.fub.bytecode.generic.BasicType;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.generic.InstructionFactory;
import de.fub.bytecode.generic.InstructionConstants;
import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.NEW;

class StackDuplicator implements StackVisitor, Constants {
  private InstructionList insList;
  private InstructionFactory insFactory;
  private int frameSize;
  private InstructionHandle newIns;
  private InstructionHandle myNewIns;
  
  /**
   * StackSaver constructor comment.
   */
  StackDuplicator(InstructionFactory f, int fSize) {
	super();
	insFactory = f;
	insList = new InstructionList();
	insList.insert(new NEW(0));
	myNewIns = insList.getStart();
	frameSize = fSize;
  }
  
  InstructionList getInstructionList() {
	return insList;
  }
  
  InstructionHandle getNewInstruction() {
	return newIns;
  }
  
  public void visitBasicType(BasicType t) {
	if (newIns == null) {
		insList.insert(myNewIns,insFactory.createStore(t, frameSize));
		insList.append(myNewIns, insFactory.createLoad(t, frameSize));
		frameSize += t.getSize();
	}
  }
  
  public void visitNull() {
	if (newIns == null) {
		insList.insert(myNewIns, InstructionConstants.POP);
		insList.append(myNewIns, InstructionConstants.ACONST_NULL);
	}
  }
  
  public void visitReferenceType(ReferenceType t) {
	if (newIns == null) {
		insList.insert(myNewIns, insFactory.createStore(t, frameSize));
		insList.append(myNewIns, insFactory.createLoad(t, frameSize));
		frameSize++;
	}
  }
  
  public void visitUninitializedType(ObjectType t, InstructionHandle ins) {
	if (newIns == null) {
		newIns = ins;
		myNewIns.setInstruction(ins.getInstruction());
	} else {
		if (ins == newIns) {
			insList.append(myNewIns, InstructionConstants.DUP);
		} /*else {
			throw new VerifyError();
		}*/
	}
  }
}
