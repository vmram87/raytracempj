package de.fub.bytecode.classfile;

import  de.fub.bytecode.Constants;
import  java.io.*;
import  java.util.zip.*;

/**
 * Wrapper class that parses a given Java .class file. The method
 * <A href ="#parse">parse</A> returns a
 * <A href ="de.fub.bytecode.classfile.JavaClass.html">
 * JavaClass</A> object on success. When an I/O error or an
 * inconsistency occurs an appropiate exception is propagated back
 * to the caller.
 *
 * The structure and the names comply, except for a few conveniences,
 * exactly with the <A href="ftp://java.sun.com/docs/specs/vmspec.ps">
 * JVM specification 1.0</a>. See this paper for
 * further details about the structure of a bytecode file.
 *
 * @version $Id: ClassParser.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public final class ClassParser implements Constants {
  private DataInputStream file;
  private ZipFile         zip;
  private String          file_name;
  private int             class_name_index, superclass_name_index;
  private int             major, minor; // Compiler version
  private int             access_flags; // Access rights of parsed class
  private int[]           interfaces; // Names of implemented interfaces
  private ConstantPool    constant_pool; // collection of constants
  private Field[]         fields; // class fields, i.e. its variables
  private Method[]        methods; // methods defined in the class
  private Attribute[]     attributes; // attributes defined in the class

  private static final int BUFSIZE = 8192;

  /**
   * Parse class from the given stream.
   *
   * @param file Input stream
   * @param file_name File name
   */
  public ClassParser(InputStream file, String file_name) {
	this.file_name = file_name;

	if(file instanceof DataInputStream) // Is already a data stream
	  this.file = (DataInputStream)file;
	else
	  this.file = new DataInputStream(new BufferedInputStream(file, BUFSIZE));
  }  
  /** Parse class from given .class file.
   *
   * @param file_name file name
   * @throw IOException
   */
  public ClassParser(String file_name) throws IOException
  {    
	this.file_name = file_name;
	file = new DataInputStream(new BufferedInputStream
			       (new FileInputStream(file_name), BUFSIZE));
  }  
  /** Parse class from given .class file in a ZIP-archive
   *
   * @param file_name file name
   * @throw IOException
   */
  public ClassParser(String zip_file, String file_name) throws IOException
  {    
	zip = new ZipFile(zip_file);
	ZipEntry entry = zip.getEntry(file_name);
  		   
	this.file_name = file_name;

	file = new DataInputStream(new BufferedInputStream(zip.getInputStream(entry),
						       BUFSIZE));
  }  
  /**
   * Parse the given Java class file and return an object that represents
   * the contained data, i.e. constants, methods, fields and commands.
   * A <em>ClassFormatError</em> is raised, if the file is not a valid
   * .class file. (This does not include verification of the byte code as it
   * is performed by the java interpreter).
   *
   * @return Class object representing the parsed class file
   * @throw  IOException
   * @throw  ClassFormatError
   */  
  public JavaClass parse() throws IOException, ClassFormatError
  {
	/****************** Read headers ********************************/
	// Check magic tag of class file
	readID();

	// Get compiler version
	readVersion();

	/****************** Read constant pool and related **************/
	// Read constant pool entries
	readConstantPool();
	
	// Get class information
	readClassInfo();

	// Get interface information, i.e. implemented interfaces
	readInterfaces();

	/****************** Read class fields and methods ***************/ 
	// Read class fields, i.e. the variables of the class
	readFields();

	// Read class methods, i.e. the functions in the class
	readMethods();

	// Read class attributes
	readAttributes();

	// Check for unknown variables
	Unknown[] u = Unknown.getUnknownAttributes();
	
	for(int i=0; i < u.length; i++)
	  System.err.println("WARNING: " + u[i]);

	// Everything should have been read now
	if(file.available() > 0) {
	  int bytes = file.available();
	  byte[] buf = new byte[bytes];
	  file.read(buf);

	  System.err.println("WARNING: Trailing garbage at end of " + file_name);
	  System.err.println(bytes + " extra bytes: " + Utility.toHexString(buf));
	}

	// Read everything of interest, so close the file
	file.close();
	if(zip != null)
	  zip.close();

	// Return the information we have gathered in a new object
	return new JavaClass(class_name_index, superclass_name_index, 
			 file_name, major, minor, access_flags,
			 constant_pool, interfaces, fields,
			 methods, attributes);
  }  
  /**
   * Read information about the attributes of the attributes of the class.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readAttributes() throws IOException, ClassFormatError
  {
	int attributes_count;

	attributes_count = file.readUnsignedShort();
	attributes       = new Attribute[attributes_count];

	for(int i=0; i < attributes_count; i++)
	  attributes[i] = Attribute.readAttribute(file, constant_pool);
  }  
  /**
   * Read information about the class and its super class.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readClassInfo() throws IOException, ClassFormatError
  {
	access_flags = file.readUnsignedShort();

	/* Interfaces are implicitely abstract, the flag should be set
	 * according to the JVM specification.
	 */
	if((access_flags & ACC_INTERFACE) != 0)
	  access_flags |= ACC_ABSTRACT;

	if(((access_flags & ACC_ABSTRACT) != 0) && 
	   ((access_flags & ACC_FINAL)    != 0 ))
	  throw new ClassFormatError("Class can't be both final and abstract");

	class_name_index      = file.readUnsignedShort();
	superclass_name_index = file.readUnsignedShort();
  }  
  /**
   * Read constant pool entries.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readConstantPool() throws IOException, ClassFormatError
  {
	constant_pool = new ConstantPool(file);
  }  
  /**
   * Read information about the fields of the class, i.e. its variables.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readFields() throws IOException, ClassFormatError
  {
	int fields_count;

	fields_count = file.readUnsignedShort();
	fields       = new Field[fields_count];

	for(int i=0; i < fields_count; i++)
	  fields[i] = new Field(file, constant_pool);
  }  
  // No getXXX/setXXX methods, wouldn't make too much sense

  /******************** Private utility methods **********************/

  /**
   * Check whether the header of the file is ok.
   * Of course, this has to be the first action on successive file reads.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readID() throws IOException, ClassFormatError
  {
	int magic = 0xCAFEBABE;

	if(file.readInt() != magic)
	  throw new ClassFormatError(file_name + " is not a Java .class file");
  }  
  /**
   * Read information about the interfaces implemented by this class.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readInterfaces() throws IOException, ClassFormatError
  {
	int interfaces_count;

	interfaces_count = file.readUnsignedShort();
	interfaces       = new int[interfaces_count];

	for(int i=0; i < interfaces_count; i++)
	  interfaces[i] = file.readUnsignedShort();
  }  
  /**
   * Read information about the methods of the class.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readMethods() throws IOException, ClassFormatError
  {
	int methods_count;

	methods_count = file.readUnsignedShort();
	methods       = new Method[methods_count];

	for(int i=0; i < methods_count; i++)
	  methods[i] = new Method(file, constant_pool);
  }  
  /**
   * Read major and minor version of compiler which created the file.
   * @throw  IOException
   * @throw  ClassFormatError
   */
  private final void readVersion() throws IOException, ClassFormatError
  {
	minor = file.readUnsignedShort();
	major = file.readUnsignedShort();

	if(!((major == MAJOR_1_1) && (minor == MINOR_1_1)) &&
	   !((major == MAJOR_1_2) && (minor >= MINOR_1_2)))
	  System.err.println("Possibly incompatible version: " + major + "." + minor);
  }  
}