package de.fub.bytecode.classfile;

import  de.fub.bytecode.Constants;
import  java.io.*;

/**
 * This class is derived from <em>Attribute</em> and represents a reference
 * to the source file of this class.
 * It is instantiated from the <em>Attribute.readAttribute()</em> method.
 *
 * @version $Id: SourceFile.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see     Attribute
 */
public final class SourceFile extends Attribute {
  private int sourcefile_index;

  /**
   * @param name_index Index in constant pool to CONSTANT_Utf8
   * @param length Content length in bytes
   * @param constant_pool Array of constants
   * @param sourcefile_index Index in constant pool to CONSTANT_Utf8
   */
  public SourceFile(int name_index, int length, int sourcefile_index,
		    ConstantPool constant_pool)
  {
	super(ATTR_SOURCE_FILE, name_index, length, constant_pool);
	this.sourcefile_index = sourcefile_index;
  }  
  /**
   * Construct object from file stream.
   * @param name_index Index in constant pool to CONSTANT_Utf8
   * @param length Content length in bytes
   * @param file Input stream
   * @param constant_pool Array of constants
   * @throw IOException
   */
  SourceFile(int name_index, int length, DataInputStream file,
	     ConstantPool constant_pool) throws IOException
  {
	this(name_index, length, file.readUnsignedShort(), constant_pool);
  }  
  /**
   * Initialize from another object. Note that both objects use the same
   * references (shallow copy). Use clone() for a physical copy.
   */
  public SourceFile(SourceFile c) {
	this(c.getNameIndex(), c.getLength(), c.getSourceFileIndex(),
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
	v.visitSourceFile(this);
  }  
  /**
   * @return deep copy of this attribute
   */
  public Attribute copy(ConstantPool constant_pool) {
	return (SourceFile)clone();
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
	file.writeShort(sourcefile_index);
  }  
  /**
   * @return Index in constant pool of source file name.
   */  
  public final int getSourceFileIndex() { return sourcefile_index; }  
  /**
   * @return Source file name.
   */ 
  public final String getSourceFileName() {
	ConstantUtf8 c = (ConstantUtf8)constant_pool.getConstant(sourcefile_index, 
							     CONSTANT_Utf8);
	return c.getBytes();
  }  
  /**
   * @param sourcefile_index.
   */
  public final void setSourceFileIndex(int sourcefile_index) {
	this.sourcefile_index = sourcefile_index;
  }  
  /**
   * @return String representation
   */ 
  public final String toString() {
	return "SourceFile(" + getSourceFileName() + ")";
  }  
}