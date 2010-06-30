package de.fub.bytecode.generic;

/** 
 * Thrown on internal errors. Extends RuntimeException so it hasn't to be declared
 * in the throws clause every time.
 *
 * @version $Id: ClassGenException.java,v 1.1.1.1 2000/08/10 16:05:07 cvs Exp $
 * @author  <A HREF="http://www.inf.fu-berlin.de/~dahm">M. Dahm</A>
 */
public class ClassGenException extends RuntimeException {
  public ClassGenException() { super(); }  
  public ClassGenException(String s) { super(s); }  
}