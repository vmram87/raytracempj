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

package be.ac.kuleuven.cs.ttm.transformer.rewriter;

import java.util.Properties;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class RewriteFactory {

  private static RewriteFactory instance;

  private Properties props;
  
  private RewriteFactory() {
    try {
      props = new Properties();
      props.load(new BufferedInputStream(new FileInputStream("rewriteinfo.prop")));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static RewriteFactory getInstance() {
    if (instance == null) {
      instance = new RewriteFactory();
    }
    return instance;
  }

  public String getComputationClass() {
    return props.getProperty("COMPUTATIONCLASS");
  }

  public String getContextClass() {
    return props.getProperty("CONTEXTCLASS");
  }

  public String getPopMethod() {
    return props.getProperty("POPMETHOD");
  }

  public String getPushMethod() {
    return props.getProperty("PUSHMETHOD");
  }

  public String getRestoring() {
    return props.getProperty("RESTORING");
  }

  public String getSwitching() {
    return props.getProperty("SWITCHING");
  }
}
