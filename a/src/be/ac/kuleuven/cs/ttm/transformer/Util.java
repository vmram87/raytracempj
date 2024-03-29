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

import de.fub.bytecode.generic.InstructionHandle;
import de.fub.bytecode.generic.CPInstruction;
import de.fub.bytecode.generic.Type;
import de.fub.bytecode.generic.ObjectType;
import de.fub.bytecode.generic.ReferenceType;
import de.fub.bytecode.classfile.ConstantPool;
import de.fub.bytecode.classfile.ConstantCP;
import de.fub.bytecode.classfile.ConstantString;
import de.fub.bytecode.classfile.ConstantFieldref;
import de.fub.bytecode.classfile.ConstantNameAndType;
import de.fub.bytecode.classfile.ConstantUtf8;
import de.fub.bytecode.classfile.JavaClass;

public class Util {
public static ReferenceType getClassReference(ConstantPool cp, int index) {
	String name = cp.getConstantString(index, ConstantPool.CONSTANT_Class);
	if (!name.startsWith("["))
		name = "L" + name + ";";
	return (ReferenceType) Type.getType(name);
}
public static Type getFieldType(ConstantPool cp, int index) {
	ConstantFieldref cfr = (ConstantFieldref) cp.getConstant(index);
	ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cfr.getNameAndTypeIndex());
	String sig = ((ConstantUtf8) cp.getConstant(cnat.getSignatureIndex())).getBytes();
	// if (!sig.startsWith("["))
	//	sig = "L" + sig + ";";
	return Type.getType(sig);
}
public static ObjectType getObjectType(ConstantPool cp, int index) {
	ConstantCP cmr = (ConstantCP) cp.getConstant(index);
	String sig = cp.getConstantString(cmr.getClassIndex(), ConstantPool.CONSTANT_Class);
	return new ObjectType(sig.replace('/','.'));
}
public static Type[] getParamTypes(ConstantPool cp, int index) {
	ConstantCP cmr = (ConstantCP) cp.getConstant(index);
	ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cmr.getNameAndTypeIndex());
	String sig = ((ConstantUtf8) cp.getConstant(cnat.getSignatureIndex())).getBytes();
	return Type.getArgumentTypes(sig);
}
public static Type getReturnType(ConstantPool cp, int index) {
	ConstantCP cmr = (ConstantCP) cp.getConstant(index);
	ConstantNameAndType cnat = (ConstantNameAndType) cp.getConstant(cmr.getNameAndTypeIndex());
	String sig = ((ConstantUtf8) cp.getConstant(cnat.getSignatureIndex())).getBytes();
	return Type.getReturnType(sig);
}
}
