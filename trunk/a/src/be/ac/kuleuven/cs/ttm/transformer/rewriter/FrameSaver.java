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

import be.ac.kuleuven.cs.ttm.transformer.dataflow.FrameVisitor;
import be.ac.kuleuven.cs.ttm.transformer.Transformer;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ReferenceType;
import de.fub.bytecode.generic.BasicType;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.InstructionFactory;
import de.fub.bytecode.generic.InstructionHandle;

class FrameSaver implements FrameVisitor, Constants {
  private InstructionList insList;
  private InstructionFactory insFactory;
  
  /**
   * StackSaver constructor comment.
   */
  public FrameSaver(InstructionFactory f) {
	super();
	insFactory = f;
	insList = new InstructionList();
  }

  InstructionList getInstructionList() {
	return insList;
  }
  
  public void visitBasicType(BasicType t, int pos) {
	RewriteFactory f = RewriteFactory.getInstance();
	insList.append(insFactory.createLoad(t, pos));
	if ((t.getSize() < 2) && (!t.equals(Type.FLOAT)))
		t = (BasicType) Type.INT;
	String mName = f.getPushMethod() + TYPE_NAMES[t.getType()];
	insList.append(insFactory.createInvoke(f.getContextClass(), mName, Type.VOID, new Type[] { t }, InstructionFactory.INVOKESTATIC));
  }
  
  public void visitNull(int pos) {
	// no need to save null
  }
  
  public void visitReferenceType(ReferenceType t, int pos) {
	RewriteFactory f = RewriteFactory.getInstance();
	if (pos == 0 && !Transformer.currentMethodStatic) {
	  insList.append(insFactory.createLoad(t, pos));
	  insList.append(insFactory.createInvoke(f.getContextClass(), f.getPushMethod() + "This", Type.VOID, new Type[] { Type.OBJECT }, InstructionFactory.INVOKESTATIC));
	}
	insList.append(insFactory.createLoad(t, pos));
	insList.append(insFactory.createInvoke(f.getContextClass(), f.getPushMethod() + "Object", Type.VOID, new Type[] { Type.OBJECT }, InstructionFactory.INVOKESTATIC));
  }
  
  public void visitUninitializedType(ObjectType t, InstructionHandle ins, int pos) {
  }
}
