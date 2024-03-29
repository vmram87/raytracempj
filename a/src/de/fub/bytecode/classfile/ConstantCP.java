package de.fub.bytecode.classfile;

import java.io.*;

/** 
 * Abstract super class for Fieldref and Methodref constants.
 *
 * @version $Id: ConstantCP.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see     ConstantFieldref
 * @see     ConstantMethodref
 * @see     ConstantInterfaceMethodref
 */
public abstract class ConstantCP extends Constant {
  /** References to the constants containing the class and the field signature
   */
  protected int class_index, name_and_type_index; 

  /**
   * @param class_index Reference to the class containing the field
   * @param name_and_type_index and the field signature
   */
  protected ConstantCP(byte tag, int class_index, 
		       int name_and_type_index) {
	super(tag);
	this.class_index         = class_index;
	this.name_and_type_index = name_and_type_index;
  }  
  /**
   * Initialize instance from file data.
   *
   * @param tag  Constant type tag
   * @param file Input stream
   * @throw IOException
   */
  ConstantCP(byte tag, DataInputStream file) throws IOException
  {
	this(tag, file.readUnsignedShort(), file.readUnsignedShort());
  }  
  /**
   * Initialize from another object.
   */
  public ConstantCP(ConstantCP c) {
	this(c.getTag(), c.getClassIndex(), c.getNameAndTypeIndex());
  }  
  /** 
   * Dump constant field reference to file stream in binary format.
   *
   * @param file Output file stream
   * @throw IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
	file.writeByte(tag);
	file.writeShort(class_index);
	file.writeShort(name_and_type_index);
  }  
  /**
   * @return Reference (index) to class this field belongs to.
   */  
  public final int getClassIndex()       { return class_index; }  
  /**
   * @return Reference (index) to signature of the field.
   */  
  public final int getNameAndTypeIndex() { return name_and_type_index; }  
  /**
   * @param class_index points to Constant_class 
   */
  public final void setClassIndex(int class_index) {
	this.class_index = class_index;
  }  
  /**
   * @param name_and_type_index points to Constant_NameAndType
   */
  public final void setNameAndTypeIndex(int name_and_type_index) {
	this.name_and_type_index = name_and_type_index;
  }  
  /**
   * @return String representation.
   */
  public final String toString() {
	return super.toString() + "(class_index = " + class_index +
	  ", name_and_type_index = " + name_and_type_index + ")";
  }  
}