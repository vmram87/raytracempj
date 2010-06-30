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

package be.ac.kuleuven.cs.ttm.transformer;

import be.ac.kuleuven.cs.ttm.transformer.dataflow.analyzer.Analyzer;
import be.ac.kuleuven.cs.ttm.transformer.dataflow.Registry;
import be.ac.kuleuven.cs.ttm.transformer.rewriter.Rewriter;
import be.ac.kuleuven.cs.ttm.transformer.rewriter.InvokespecialRewriter;
import be.ac.kuleuven.cs.ttm.transformer.rewriter.StripDebugRewriter;
import be.ac.kuleuven.cs.ttm.transformer.rewriter.InvokeRewriter;
import java.io.File;
import de.fub.bytecode.classfile.JavaClass;
import de.fub.bytecode.classfile.Method;
import de.fub.bytecode.generic.MethodGen;
import de.fub.bytecode.generic.ConstantPoolGen;
import de.fub.bytecode.classfile.ClassParser;
import de.fub.bytecode.classfile.Visitor;

public class Transformer {
	
  public static boolean currentMethodStatic;

  public static void main(String arg[]) {
	 arg[0]="bin/test2/Agent.class";
    for (int i = 0; i < arg.length; i++) {
	try {
	  JavaClass jClass = new ClassParser(arg[i]).parse();
	  ConstantPoolGen cpGen = new ConstantPoolGen(jClass.getConstantPool());
	  Rewriter r1 = new StripDebugRewriter();
	  Rewriter r2 = new InvokespecialRewriter();
	  Rewriter r3 = new InvokeRewriter();
	  Analyzer a = new Analyzer();
	  Method[] method = jClass.getMethods();
	  for (int j = 0; j < method.length; j++) {
	    currentMethodStatic = method[j].isStatic();
	    MethodGen mGen = new MethodGen(method[j], jClass.getClassName(), cpGen);
	    if (!mGen.isAbstract()) {
		Registry reg = a.analyze(mGen);
		r1.rewrite(mGen, reg);
		r2.rewrite(mGen, reg);
		r3.rewrite(mGen, reg);
		mGen.setMaxStack();
		mGen.setMaxLocals();
		method[j] = mGen.getMethod();
	    }
	  }
			
	  /*//Reanalyze each method
	  for (int j = 0; j < method.length; j++) {
		currentMethodStatic = method[j].isStatic();
		MethodGen mGen = new MethodGen(method[j], jClass.getClassName(), cpGen);
		if (!mGen.isAbstract()) {
			Registry reg = a.analyze(mGen);
			r3.rewrite(mGen, reg);
			mGen.setMaxStack();
			mGen.setMaxLocals();
			method[j] = mGen.getMethod();
		}
	  }*/
			
			
			
	  jClass.setConstantPool(cpGen.getFinalConstantPool());
	  jClass.dump(arg[i] + ".rewritten");
        } catch (java.io.IOException e) {
	  e.printStackTrace();
	}
    }
  }
  public void register(MethodVisitor m) {
  }

  public void translate(JavaClass jc) {
  }
}
