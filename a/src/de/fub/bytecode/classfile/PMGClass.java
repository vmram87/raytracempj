package de.fub.bytecode.classfile;

import  de.fub.bytecode.Constants;
import  java.io.*;

/**
 * This class is derived from <em>Attribute</em> and represents a reference
 * to a <a href="http://www.inf.fu-berlin.de/~bokowski/pmgjava/index.html">PMG</a>
 * attribute.
 *
 * @version $Id: PMGClass.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see     Attribute
 */
public final class PMGClass extends Attribute {
  private int pmg_class_index, pmg_index;

  /**
   * @param name_index Index in constant pool to CONSTANT_Utf8
   * @param length Content length in bytes
   * @param constant_pool Array of constants
   * @param PMGClass_index Index in constant pool to CONSTANT_Utf8
   */
  public PMGClass(int name_index, int length, int pmg_index, int pmg_class_index,
		  ConstantPool constant_pool)
  {
	super(ATTR_PMG, name_index, length, constant_pool);
	this.pmg_index       = pmg_index;
	this.pmg_class_index = pmg_class_index;
  }  
  /**
   * Construct object from file stream.
   * @param name_index Index in constant pool to CONSTANT_Utf8
   * @param length Content length in bytes
   * @param file Input stream
   * @param constant_pool Array of constants
   * @throw IOException
   */
  PMGClass(int name_index, int length, DataInputStream file,
	   ConstantPool constant_pool) throws IOException
  {
	this(name_index, length, file.readUnsignedShort(), file.readUnsignedShort(),
	 constant_pool);
  }  
  /**
   * Initialize from another object. Note that both objects use the same
   * references (shallow copy). Use clone() for a physical copy.
   */
  public PMGClass(PMGClass c) {
	this(c.getNameIndex(), c.getLength(), c.getPMGIndex(), c.getPMGClassIndex(),
	 c.getConstantPool());
  }  
  /**
   * Called by objects that are traversing the nodes of the tree implicitely
   * defined by the contents of a Java class. I.e., the hierarchy of methods,
   * fields, attributes, etc. spawns a tree of objects.
   *
   * @param v Visitor object
   */
   public void accept(Visitor v) {
	 System.err.println("Visiting non-standard PMGClass object");
   }   
  /**
   * @return deep copy of this attribute
   */
  public Attribute copy(ConstantPool constant_pool) {
	return (PMGClass)clone();
  }  
  /**
   * Dump source file attribute to file stream in binary format.
   *
   * @param file Output file stream
   * @throw IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
	super.dump(file);
	file.writeShort(pmg_index);
	file.writeShort(pmg_class_index);
  }  
  /**
   * @return Index in constant pool of source file name.
   */  
  public final int getPMGClassIndex() { return pmg_class_index; }  
  /**
   * @return PMG class name.
   */ 
  public final String getPMGClassName() {
	ConstantUtf8 c = (ConstantUtf8)constant_pool.getConstant(pmg_class_index, 
							     CONSTANT_Utf8);
	return c.getBytes();
  }  
  /**
   * @return Index in constant pool of source file name.
   */  
  public final int getPMGIndex() { return pmg_index; }  
  /**
   * @return PMG name.
   */ 
  public final String getPMGName() {
	ConstantUtf8 c = (ConstantUtf8)constant_pool.getConstant(pmg_index, 
							     CONSTANT_Utf8);
	return c.getBytes();
  }  
  /**
   * @param PMGClass_index.
   */
  public final void setPMGClassIndex(int pmg_class_index) {
	this.pmg_class_index = pmg_class_index;
  }  
  /**
   * @param PMGClass_index.
   */
  public final void setPMGIndex(int pmg_index) {
	this.pmg_index = pmg_index;
  }  
  /**
   * @return String representation
   */ 
  public final String toString() {
	return "PMGClass(" + getPMGName() + ", " + getPMGClassName() + ")";
  }  
}