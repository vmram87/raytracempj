package de.fub.bytecode.util;

import java.util.Stack;
import de.fub.bytecode.classfile.JavaClass;

/** 
 * Utility class implementing a (typesafe) stack of JavaClass objects.
 *
 * @version $Id: ClassStack.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A> 
 * @see Stack
*/
public class ClassStack {
  private Stack stack = new Stack();

  public boolean   empty()               { return stack.empty(); }  
  public JavaClass pop()                 { return (JavaClass)stack.pop(); }  
  public void      push(JavaClass clazz) { stack.push(clazz); }  
  public JavaClass top()                 { return (JavaClass)stack.peek(); }  
}