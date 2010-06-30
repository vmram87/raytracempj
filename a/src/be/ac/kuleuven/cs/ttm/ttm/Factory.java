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

package be.ac.kuleuven.cs.ttm.ttm;

import java.lang.reflect.Constructor;
import java.util.Properties;
import java.io.*;

/**
  * This is the Computation Factory. It will create a Computation according
  * to the Config File.
  * @version $id=0.1
  * @Author <A HREF=http://www.cs.kuleuven.ac.be/~tim>T.Coninx</A>
  */
public class Factory {

  private static Factory instance = null;

  private Properties props;

  private Factory() {
    try {
      props = new Properties();
      props.load(new BufferedInputStream(new FileInputStream("rewriteinfo.prop")));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static Factory getInstance() {
    if (instance == null) {
      instance = new Factory();
    }
    return instance;
  }

  public Computation createComputation(Runnable r) {
    Computation result = null;
    try {
      Class compclass = Class.forName(props.getProperty("COMPUTATIONCLASS"));
      Constructor compconst = compclass.getConstructor(new Class[]{Class.forName("java.lang.Runnable")});
      result = (Computation)compconst.newInstance(new Object[]{r});
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return result;
  }

  public Class getScheduler() {
    Class result = null;
    String compclass = props.getProperty("COMPUTATIONCLASS");
    String scheduler = compclass.substring(0,compclass.lastIndexOf("Computation")).concat("Scheduler");
    try {
      result = Class.forName(scheduler);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return result;
  }
}
