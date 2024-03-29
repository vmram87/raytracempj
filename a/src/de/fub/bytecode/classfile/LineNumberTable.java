package de.fub.bytecode.classfile;

import  de.fub.bytecode.Constants;
import  java.io.*;

/**
 * This class is derived from <em>Attribute</em> and represents a table of 
 * line numbers for debugging purposes. This attribute is used by the 
 * <em>Code</em> attribute. It contains pairs of PCs and line numbers.
 *
 * @version $Id: LineNumberTable.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 * @see     Code
 * @see     LineNumber
 */
public final class LineNumberTable extends Attribute {
  private int          line_number_table_length;
  private LineNumber[] line_number_table; // Table of line/numbers pairs

  /*
   * @param name_index Index of name
   * @param length Content length in bytes
   * @param line_number_table Table of line/numbers pairs
   * @param constant_pool Array of constants
   */
  public LineNumberTable(int name_index, int length,
			 LineNumber[] line_number_table,
			 ConstantPool constant_pool)
  {
	super(ATTR_LINE_NUMBER_TABLE, name_index, length, constant_pool);
	setLineNumberTable(line_number_table);
  }  
  /**
   * Construct object from file stream.
   * @param name_index Index of name
   * @param length Content length in bytes
   * @param file Input stream
   * @throw IOException
   * @param constant_pool Array of constants
   */
  LineNumberTable(int name_index, int length, DataInputStream file,
		  ConstantPool constant_pool) throws IOException
  {
	this(name_index, length, (LineNumber[])null, constant_pool);
	line_number_table_length = (file.readUnsignedShort());
	line_number_table = new LineNumber[line_number_table_length];

	for(int i=0; i < line_number_table_length; i++)
	  line_number_table[i] = new LineNumber(file);
  }  
  /*
   * Initialize from another object. Note that both objects use the same
   * references (shallow copy). Use clone() for a physical copy.
   */
  public LineNumberTable(LineNumberTable c) {
	this(c.getNameIndex(), c.getLength(), c.getLineNumberTable(),
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
	v.visitLineNumberTable(this);
  }  
  /**
   * @return deep copy of this attribute
   */
  public Attribute copy(ConstantPool constant_pool) {
	LineNumberTable c = (LineNumberTable)clone();

	c.line_number_table = new LineNumber[line_number_table_length];
	for(int i=0; i < line_number_table_length; i++)
	  c.line_number_table[i] = line_number_table[i].copy();

	c.constant_pool = constant_pool;
	return c;
  }  
  /**
   * Dump line number table attribute to file stream in binary format.
   *
   * @param file Output file stream
   * @throw IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
	super.dump(file);
	file.writeShort(line_number_table_length);
	for(int i=0; i < line_number_table_length; i++)
	  line_number_table[i].dump(file);
  }  
  /**
   * @return Array of (pc offset, line number) pairs.
   */  
  public final LineNumber[] getLineNumberTable() { return line_number_table; }  
  /**
   * Map byte code positions to source code lines.
   *
   * @param pos byte code offset
   * @return corresponding line in source code
   */
  public int getSourceLine(int pos) {
	int l = 0, r = line_number_table_length-1;

	if(r < 0) // array is empty
	  return -1;

	int min_index = -1, min=-1;
	
	/* Do a binary search since the array is ordered.
	 */
	do {
	  int i = (l + r) / 2;
	  int j = line_number_table[i].getStartPC();

	  if(j == pos)
	return line_number_table[i].getLineNumber();
	  else if(pos < j) // else constrain search area
	r = i - 1;
	  else // pos > j
	l = i + 1;

	  /* If exact match can't be found (which is the most common case)
	   * return the line number that corresponds to the greatest index less
	   * than pos.
	   */
	  if(j < pos && j > min) {
		min       = j;
		min_index = i;
	  }
	} while(l <= r);

	return line_number_table[min_index].getLineNumber();
  }  
  public final int getTableLength() { return line_number_table_length; }  
  /**
   * @param line_number_table.
   */
  public final void setLineNumberTable(LineNumber[] line_number_table) {
	this.line_number_table = line_number_table;

	line_number_table_length = (line_number_table == null)? 0 :
	  line_number_table.length;
  }  
  /**
   * @return String representation.
   */ 
  public final String toString() {
	StringBuffer buf  = new StringBuffer();
	StringBuffer line = new StringBuffer();

	for(int i=0; i < line_number_table_length; i++) {
	  line.append(line_number_table[i].toString());

	  if(i < line_number_table_length - 1)
	line.append(", ");

	  if(line.length() > 72) {
	line.append('\n');
	buf.append(line);
	line.setLength(0);
	  }
	}

	buf.append(line);

	return buf.toString();    
  }  
}