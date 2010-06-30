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

import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.InstructionFactory;
import de.fub.bytecode.generic.ArrayInstruction;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.InstructionList;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.ALOAD;
import de.fub.bytecode.generic.ASTORE;
import de.fub.bytecode.generic.TABLESWITCH;
import de.fub.bytecode.generic.ICONST;
import de.fub.bytecode.generic.IFEQ;
import de.fub.bytecode.generic.GOTO;
import de.fub.bytecode.generic.GETSTATIC;
import de.fub.bytecode.generic.GETFIELD;
import de.fub.bytecode.generic.INVOKESTATIC;
import de.fub.bytecode.generic.PUSH;
import de.fub.bytecode.generic.CHECKCAST;
import de.fub.bytecode.generic.InvokeInstruction;
import de.fub.bytecode.generic.POP;
import de.fub.bytecode.generic.POP2;

import de.fub.bytecode.classfile.Code;
import de.fub.bytecode.classfile.JavaClass;
import de.fub.bytecode.classfile.Method;

import de.fub.bytecode.Constants;

import java.util.Vector;
import java.util.Enumeration;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Stack;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.VirtualType;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Frame;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Registry;
import be.ac.kuleuven.cs.ttm.transformer.Util;

public class InvokeRewriter implements Rewriter {
  private MethodGen mGen;
  
  private Registry registry;
  
  private InstructionFactory insFactory;
  
  public InvokeRewriter() {
  }  

  private boolean isValid(MethodGen m) {
    if (m.getMethodName().equals("<init>")) return false;
    if (m.getMethodName().equals("<clinit>")) return false;
    if (m.isNative() || m.isAbstract()) return false;
    return true;
  }

  private Stack removeReturnType(Stack s, InstructionHandle ins) {
    InvokeInstruction inv = (InvokeInstruction) ins.getInstruction();
    Type t = Util.getReturnType(mGen.getConstantPool().getConstantPool(), inv.getIndex());
    if (!t.equals(Type.VOID)) {
      s = new Stack(s);
      s.pop();
      if (t.getSize() == 2) {
	s.pop();
      }
    }
    return s;
  }

  private InstructionList restoreContext(InstructionHandle ins) {
    RewriteFactory f = RewriteFactory.getInstance();
    InstructionList insList = new InstructionList();
    StackRestorer ss = new StackRestorer(insFactory);
    FrameRestorer fs = new FrameRestorer(insFactory);
    removeReturnType(registry.getStack(ins.getNext()),ins).accept(ss);
    registry.getFrame(ins.getNext()).accept(fs);
    insList.append(fs.getInstructionList());
    insList.append(ss.getInstructionList());
    InvokeInstruction inv = (InvokeInstruction) ins.getInstruction();
    if (!(inv instanceof INVOKESTATIC)) {
      insList.append(insFactory.createInvoke(f.getContextClass(), f.getPopMethod() + "This", Type.OBJECT, Type.NO_ARGS, InstructionFactory.INVOKESTATIC));
      insList.append(insFactory.createCast(Type.OBJECT, Util.getObjectType(mGen.getConstantPool().getConstantPool(), inv.getIndex())));
    }
    Type[] pTypes = Util.getParamTypes(mGen.getConstantPool().getConstantPool(), inv.getIndex());
    for (int j = 0; j < pTypes.length; j++) {
      insList.append(new NULLCONST(pTypes[j]));
    }
    insList.append(new GOTO(ins));
    return insList;
  }
  
  public void rewrite(MethodGen m, Registry reg) {
    RewriteFactory f = RewriteFactory.getInstance();
    if (!isValid(m)) return;
    insFactory = new InstructionFactory(m.getConstantPool());
    Vector invokeIns = new Vector();
    int count = 0;
    mGen = m;
    registry = reg;
    InstructionList insList = m.getInstructionList();
    InstructionHandle firstIns = insList.getStart();
    InstructionHandle ins = firstIns;

    InstructionList rList = null;
    while (ins != null) {
      InstructionHandle next = ins.getNext();
      if (rewriteable(ins)) {
	rList = restoreContext(ins);
	insList.append(ins, saveContext(ins, count++));
	invokeIns.addElement(rList.getStart());
	insList.insert(firstIns, rList);
      }
      ins = next;
    }
    if (count > 0) {
      InstructionHandle[] tableTargets = new InstructionHandle[count];
      int[] match = new int[count];
      for (int i = 0; i < count; i++)
	match[i] = i;
      invokeIns.copyInto(tableTargets);
      insList.insert(new TABLESWITCH(match, tableTargets, firstIns));
      insList.insert(insFactory.createInvoke(f.getContextClass(), f.getPopMethod() + "Int", Type.INT, Type.NO_ARGS, InstructionFactory.INVOKESTATIC));
      insList.insert(new IFEQ(firstIns));
      insList.insert(insFactory.createInvoke(f.getComputationClass(), f.getRestoring(), Type.BOOLEAN, Type.NO_ARGS, InstructionFactory.INVOKESTATIC));
      m.setMaxLocals(m.getMaxLocals() + 10);
    }
  }
  
  private boolean rewriteable(InstructionHandle ins) {
    int t = ins.getInstruction().getTag();
    boolean invokeSpecialSuper = false;
    if (t == InstructionList.INVOKESPECIAL) {
      InvokeInstruction ivs = (InvokeInstruction) ins.getInstruction();
      String mName = ivs.getMethodName(mGen.getConstantPool());
      invokeSpecialSuper = !mName.equals("<init>");
    }
    
    if ((t == InstructionList.INVOKEVIRTUAL) || (t == InstructionList.INVOKESTATIC) || (t == InstructionList.INVOKEINTERFACE) || invokeSpecialSuper ) {
      int index = ((InvokeInstruction) ins.getInstruction()).getIndex();
      String cName = be.ac.kuleuven.cs.ttm.transformer.Util.getObjectType(mGen.getConstantPool().getConstantPool(), index).getClassName();
      return !cName.startsWith("java.");
    }
    return false;
  }
  
  private InstructionList saveContext(InstructionHandle ins, int pc) {
    RewriteFactory f = RewriteFactory.getInstance();
    InstructionList insList;
    StackSaver ss = new StackSaver(insFactory);
    FrameSaver fs = new FrameSaver(insFactory);
    removeReturnType(registry.getStack(ins.getNext()), ins).accept(ss);
    registry.getFrame(ins.getNext()).accept(fs);
    // save stack
    insList = ss.getInstructionList();
    
    //Indien het een niet-void methode betreft, moet de return-waarde eruit gegooid worden.
    InvokeInstruction ivs = (InvokeInstruction) ins.getInstruction();
    Type t = Util.getReturnType(mGen.getConstantPool().getConstantPool(), ivs.getIndex());
    if (!t.equals(Type.VOID)) {
      if (t.getSize() == 1) insList.insert(new POP());
      else insList.insert(new POP2());
    }
    // add isSwitching test
    insList.insert(new IFEQ(ins.getNext()));
    insList.insert(insFactory.createInvoke(f.getComputationClass(), f.getSwitching(), Type.BOOLEAN, Type.NO_ARGS, InstructionFactory.INVOKESTATIC));
    // save local variables
    insList.append(fs.getInstructionList());
    // save programcounter
    insList.append(new PUSH(mGen.getConstantPool(), pc));
    insList.append(insFactory.createInvoke(f.getContextClass(), f.getPushMethod() + "Int", Type.VOID, new Type[] {Type.INT}, InstructionFactory.INVOKESTATIC));
    // return NULL result
    insList.append(new NULLCONST(mGen.getReturnType()));
    insList.append(insFactory.createReturn(mGen.getReturnType()));
    return insList;
  }
}
