package de.fub.bytecode.classfile;

import  de.fub.bytecode.Constants;
import  java.io.*;

/** 
 * This class is derived from the abstract 
 * <A HREF="de.fub.bytecode.classfile.Constant.html">Constant</A> class 
 * and represents a reference to a (external) class.
 *
 * @version $Id: ConstantClass.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see     Constant
 */
public final class ConstantClass extends Constant {
  private int name_index; // Identical to ConstantString except for the name

  /**
   * @param name_index Name index in constant pool
   */
  public ConstantClass(int name_index) {
	super(CONSTANT_Class);
	this.name_index = name_index;
  }  
  /**
   * Initialize from another object.
   */
  public ConstantClass(ConstantClass c) {
	this(c.getNameIndex());
  }  
  /**
   * Initialize instance from file data.
   *
   * @param file Input stream
   * @throw IOException
   */
  ConstantClass(DataInputStream file) throws IOException
  {    
	this(file.readUnsignedShort());
  }  
  /**
   * Called by objects that are traversing the nodes of the tree implicitely
   * defined by the contents of a Java class. I.e., the hierarchy of methods,
   * fields, attributes, etc. spawns a tree of objects.
   *
   * @param v Visitor object
   */
  public void accept(Visitor v) {
	v.visitConstantClass(this);
  }  
  /** 
   * Dump constant class to file stream in binary format.
   *
   * @param file Output file stream
   * @throw IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
	file.writeByte(tag);
	file.writeShort(name_index);
  }  
  /**
   * @return Name index in constant pool of class name.
   */  
  public final int getNameIndex() { return name_index; }  
  /**
   * @param name_index.
   */
  public final void setNameIndex(int name_index) {
	this.name_index = name_index;
  }  
  /**
   * @return String representation.
   */
  public final String toString() {
	return super.toString() + "(name_index = " + name_index + ")";
  }  
}