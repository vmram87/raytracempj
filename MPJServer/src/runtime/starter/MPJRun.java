/*
 The MIT License

 Copyright (c) 2005 - 2010
   1. Distributed Systems Group, University of Portsmouth (2005)
   2. Aamir Shafi (2005 - 2010)
   3. Bryan Carpenter (2005 - 2010)
   4. Mark Baker (2005 - 2010)

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
/*
 * File         : MPJRun.java 
 * Author       : Aamir Shafi, Bryan Carpenter
 * Created      : Sun Dec 12 12:22:15 BST 2004
 * Revision     : $Revision: 1.35 $
 * Updated      : $Date: Wed Mar 31 15:33:18 PKT 2010$
 */

package runtime.starter;

import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger ;
import org.apache.log4j.PropertyConfigurator ;
import org.apache.log4j.PatternLayout ;
import org.apache.log4j.FileAppender ;
import org.apache.log4j.Level ;
import org.apache.log4j.DailyRollingFileAppender ;
import org.apache.log4j.spi.LoggerRepository ;

import java.util.jar.Attributes ;
import java.util.jar.JarFile ;

import runtime.MPJRuntimeException ;

public class MPJRun {

  public static String CONF_FILE_NAME = "mpjdev.conf" ;
  public static String MPJ_DIR_NAME = ".mpj" ;

  String configFileName = null ; 

  private static int MPJ_SERVER_PORT = 20000 ; 
  private static int mxBoardNum = 0 ; 
  private static int D_SER_PORT = getPortFromWrapper() ;
  private static int endPointID = 0 ;

  int S_PORT = 15000; 
  String machinesFile = "machines" ; 
  ArrayList<String> jvmArgs = new ArrayList<String>() ; 
  ArrayList<String> appArgs = new ArrayList<String>() ; 
  String[] jArgs = null ;  
  String[] aArgs = null ;
  private int psl = 128*1024 ;  //128K 
  static Logger logger = null ; 
  FileOutputStream cfos = null;
  File CONF_FILE = null;
  private volatile boolean wait = true;
  private Vector<SocketChannel> peerChannels;
  private InetAddress localaddr = null;
  private Selector selector = null;
  private volatile boolean selectorFlag = true;
  private String LOG_FILE = null;
  private String hostName = null;
  private String hostIP = null;
  private Thread selectorThreadStarter = null;
  private Vector machineVector = new Vector();
  private Map machineConnectedMap=new HashMap();
  private HashMap<String, SocketChannel> machineChannelMap = new HashMap<String, SocketChannel>();
  int nprocs = Runtime.getRuntime().availableProcessors() ; 
  String spmdClass = null;
  String deviceName = "multicore";
  String applicationArgs = "default_app_arg" ;
  String mpjHomeDir = null;
  byte[] urlArray = null;
  Hashtable procsPerMachineTable = new Hashtable();
  int endCount = 0; 
  int streamEndedCount = 0 ;
  String wdir;
  String className = null ; 
  String applicationClassPathEntry = null ; 
  String codeBase = null;
  String mpjCodeBase = null ; 
  ByteBuffer buffer = ByteBuffer.allocate(1000);

  static final boolean DEBUG = true ; 
  static final String VERSION = "0.36" ; 
  private static int RUNNING_JAR_FILE = 2 ; 
  private static int RUNNING_CLASS_FILE = 1 ; 
  private SocketChannel checkpiontChannel = null;
  private String checkpointHost = getCheckpointHost();
  private int checkpointPort = getCheckpointPort();
  
  private final int NUM_OF_PROCCESSES = -42;
  public static final int LONG_MESSAGE = -45;
  public static final int DAEMON_EXIT = -46;
  public static final int END_OF_STREAM = -14;
  public static final int INT_MESSAGE = -47;
  public static final int END_APP = -48;
  private final int REQUEST_RESTART = -70;
  private final int CHECK_VALID = -71;
  
  
  private boolean isFinished = false;
  private CustomSemaphore initLock = new CustomSemaphore(1); 
  private CustomSemaphore heartBeatLock = new CustomSemaphore(1); 

  /**
   * Every thing is being inside this constructor :-)
   */
  public MPJRun(String args[]) throws Exception {

    java.util.logging.Logger logger1 = 
    java.util.logging.Logger.getLogger("");


    //remove all existing log handlers: remove the ERR handler
    for (java.util.logging.Handler h : logger1.getHandlers()) {
      logger1.removeHandler(h);
    }
		  
    Map<String,String> map = System.getenv() ;
    mpjHomeDir = map.get("MPJ_HOME");

    createLogger(args) ; 

    if(DEBUG && logger.isDebugEnabled()) {
      logger.info(" --MPJRun invoked--"); 
      logger.info(" adding shutdown hook thread"); 
    }

	    
    if(DEBUG && logger.isDebugEnabled()) {
      logger.info("processInput called ..."); 
    }

    processInput(args);

    if(deviceName.equals("multicore")) {
       
      System.out.println("MPJ Express ("+VERSION+") is started in the "+
                                              "multicore configuration"); 
      if(DEBUG && logger.isDebugEnabled()) {
        logger.info("className "+className) ; 
      }

//applicationClassPathEntry 
//className 

      int jarOrClass = (applicationClassPathEntry.endsWith(".jar")?
                                  RUNNING_JAR_FILE:RUNNING_CLASS_FILE);
       
      //System.out.println("codeBase"+codeBase) ; 
      MulticoreDaemon multicoreDaemon =
          new MulticoreDaemon(className, applicationClassPathEntry, jarOrClass, 
	                           nprocs, wdir, jvmArgs, appArgs) ;
      return ;

    }
    else { 
      System.out.println("MPJ Express ("+VERSION+") is started in the "+
                                              "cluster configuration"); 
    }

    //System.exit(0) ; 
    readMachineFile();
    machinesSanityCheck() ;
	    
    File mpjDirectory = new File ( System.getProperty("user.home")
                                               + File.separator
                                               + MPJ_DIR_NAME ) ;

    if(!mpjDirectory.isDirectory() && !mpjDirectory.exists()) {
      mpjDirectory.mkdir();
    }

    configFileName = mpjHomeDir 
                                     + File.separator
                                     + MPJ_DIR_NAME
                                     + File.separator
                                     + CONF_FILE_NAME  ;

    CONF_FILE = new File(configFileName) ; 

    CONF_FILE.createNewFile() ; 

    CONF_FILE.deleteOnExit() ;

    if(DEBUG && logger.isDebugEnabled()) { 
      logger.debug("CONF_FILE_PATH <"+CONF_FILE.getAbsolutePath()+">");
    }

    assignTasks();

    try {

      localaddr = InetAddress.getLocalHost();
      hostName = localaddr.getHostName();

      if(hostIP == null)
        hostIP = localaddr.getHostAddress(); 

      if(DEBUG && logger.isDebugEnabled()) {
	logger.debug("Address: " + localaddr);
	logger.debug("Name   : " + hostName );
      }

    }
    catch (UnknownHostException unkhe) {
      throw new MPJRuntimeException(unkhe);  
    }

    urlArray = applicationClassPathEntry.getBytes();

    peerChannels = new Vector<SocketChannel>();

    selector = Selector.open();

    clientSocketInit();

    //System.out.println("going to sleep") ; 
    //try { Thread.currentThread().sleep(10000) ; } catch(Exception e) {}
    //System.out.println("sleep over") ; 
    //System.exit(0) ; 

    //startHttpServer();

    selectorThreadStarter = new Thread(selectorThread);

    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("Starting the selector thread ");
    }

    selectorThreadStarter.start();

    /* 
     * wait till this client has connected to all daemons
     */
    Wait();

    buffer.clear();

    for (int j = 0; j < peerChannels.size(); j++) {

      SocketChannel socketChannel = peerChannels.get(j);
      
      if(DEBUG && logger.isDebugEnabled()) { 
	logger.debug("procsPerMachineTable " + procsPerMachineTable);
      }

      /* FIXME: should we not be checking all IP addresses of remote 
                machine? Does it make sense? */

      String hAddress = 
                     socketChannel.socket().getInetAddress().getHostAddress();
      String hName = socketChannel.socket().getInetAddress().getHostName();

      Integer nProcessesInt = ((Integer) procsPerMachineTable.get(hName)) ; 

      if(nProcessesInt == null) { 
        nProcessesInt = ((Integer) procsPerMachineTable.get(hAddress)) ;     
      } 

      int nProcesses = nProcessesInt.intValue();

      pack(nProcesses); 

      if(DEBUG && logger.isDebugEnabled()) { 
	logger.debug("Sending to " + socketChannel);
	byte[] tempArray = new byte[buffer.limit()];
	buffer.get(tempArray,0,buffer.limit());
	// String line = new String(tempArray);
	 logger.debug("Sending Content Buffer");
	 //System.out.println(line);
	 buffer.flip();
      }
      
      
      
      int w = 0 ; 
      while(buffer.hasRemaining()) {
	if((w += socketChannel.write(buffer)) == -1) {
	  //throw an exception ...
	} 
      }
      if(DEBUG && logger.isDebugEnabled()) { 
	logger.debug("Wrote bytes-->"+w+"to process"+j);
      }

      buffer.clear();

    }

    if(DEBUG && logger.isDebugEnabled()) { 
      logger.debug("procsPerMachineTable " + procsPerMachineTable);
    }

    addShutdownHook();

    /* 
     * waiting to get the answer from the daemons that the job has finished.
     */ 
    Wait();
if(DEBUG && logger.isDebugEnabled())
    logger.debug("Calling the finish method now");

    this.finish();

  }
	  
  /* 
   * 1. Application Classpath Entry (-cpe). This is a String classpath entry 
        which will be appended by the MPJ Express daemon before starting
        a user process (JVM). In the case of JAR file, it's the absolute
        path and name. In the case of a class file, its the name of the 
        working directory where mpjrun command was launched. 
   * 2. num- [# of processes] to be started by a particular MPJ Express
        daemon.
   * 3. arg- args to JVM
   * 4. wdr- Working Directory 
   * 5. cls- Classname to be executed. In the case of JAR file, this 
        name is taken from the manifest file. In the case of class file, 
        the class name is specified on the command line by the user.
   * 6. cfn- Configuration File name. This points to "System.getProperty
        ("user.home")+/+.mpj+/+mpjdev.conf
   * 7. dev-: what device to use?
   * 8. app-: Application arguments ..
   * 9. GO_FOR_IT_SIGNAL
   */ 
  private void pack(int nProcesses) {
    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer (initial)" + buffer);
    }
    buffer.put("cpe-".getBytes());

    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer (after putting url-) " + buffer);
    }
    buffer.putInt(urlArray.length);

    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer urlArray.length)" + buffer);
    }

    buffer.put(urlArray, 0, urlArray.length);

    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer urlArray itself " + buffer);
    }

    buffer.put("num-".getBytes());
	 
    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer " + buffer);
    }

    buffer.putInt(4);

    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer(after writing 4) " + buffer);
      logger.debug("nProcesses " + nProcesses);
    }

    buffer.putInt(nProcesses);

    if(DEBUG && logger.isDebugEnabled()) {
      logger.debug("buffer(after nProcesses) " + buffer);
    }

    buffer.put("arg-".getBytes());
    buffer.putInt(jArgs.length); 
    for(int j=0 ; j<jArgs.length ; j++) {
      buffer.putInt(jArgs[j].getBytes().length);
      buffer.put(jArgs[j].getBytes(), 0, jArgs[j].getBytes().length);
    }

    if(wdir == null) { 
      wdir = System.getProperty("user.dir") ;
    }

    buffer.put("wdr-".getBytes());
    buffer.putInt(wdir.getBytes().length);
    buffer.put(wdir.getBytes(), 0, wdir.getBytes().length); 
    
    buffer.put("cls-".getBytes());
    buffer.putInt(className.getBytes().length);
    buffer.put(className.getBytes(), 0, className.getBytes().length); 

    //configFileName 
    buffer.put("cfn-".getBytes());
    buffer.putInt(configFileName.getBytes().length);
    buffer.put(configFileName.getBytes(), 0, configFileName.getBytes().length); 

    buffer.put("dev-".getBytes());
    buffer.putInt(deviceName.getBytes().length);
    buffer.put(deviceName.getBytes(), 0, deviceName.getBytes().length); 
	    
    buffer.put("app-".getBytes());
    buffer.putInt(aArgs.length); 

    for(int j=0 ; j<aArgs.length ; j++) {
      buffer.putInt(aArgs[j].getBytes().length);
      buffer.put(aArgs[j].getBytes(), 0, aArgs[j].getBytes().length);
    }

    buffer.put("*GO*".getBytes(), 0, "*GO*".getBytes().length);    

    buffer.flip();
  }

  private void createLogger(String[] args) throws MPJRuntimeException {
  
    if(DEBUG && logger == null) {

      DailyRollingFileAppender fileAppender = null ;

      try {
	fileAppender = new DailyRollingFileAppender(
			    new PatternLayout(
			    " %-5p %c %x - %m\n" ),
			    mpjHomeDir+"/logs/mpjrun.log",
			    "yyyy-MM-dd-a" );

	Logger rootLogger = Logger.getRootLogger() ;
	rootLogger.addAppender( fileAppender);
	LoggerRepository rep =  rootLogger.getLoggerRepository() ;
	rootLogger.setLevel ((Level) Level.ALL );
	//rep.setThreshold((Level) Level.OFF ) ;
	logger = Logger.getLogger( "runtime" );
      }
      catch(Exception e) {
	throw new MPJRuntimeException(e) ;
      }
    }  
  }

  private void printUsage() { 
    System.out.println(   
      "mpjrun.[bat/sh] [options] class [args...]"+
      "\n                (to execute a class)"+
      "\nmpjrun.[bat/sh] [options] -jar jarfile [args...]"+
      "\n                (to execute a jar file)"+
      "\n\nwhere options include:"+
      "\n   -np val            -- <# of cores>"+ 
      "\n   -dev val           -- multicore"+
      "\n   -dport val         -- <read from wrapper.conf>"+ 
      "\n   -wdir val          -- $MPJ_HOME/bin"+ 
      "\n   -mpjport val       -- 20000"+  
      "\n   -mxboardnum val    -- 0"+  
      "\n   -headnodeip val    -- ..."+
      "\n   -psl val           -- 128Kbytes"+ 
      "\n   -machinesfile val  -- machines"+ 
      "\n   -h                 -- print this usage information"+ 
      "\n   ...any JVM arguments..."+
 "\n Note: Value on the right in front of each option is the default value"+ 
 "\n Note: 'MPJ_HOME' variable must be set");

  }
  

  /**
   * Parses the input ...
   */
  private void processInput(String args[]) {

    if (args.length < 1) {
      printUsage() ;
      System.exit(0);  
    }
 
    boolean append = false;
    boolean parallelProgramNotYetEncountered = true ; 
    
    for (int i = 0; i < args.length; i++) {

      if(args[i].equals("-np")) {

        try {  
          nprocs = new Integer(args[i+1]).intValue();
	} 
	catch(NumberFormatException e) {
	  nprocs = Runtime.getRuntime().availableProcessors();
	}

        i++;
      }

      else if(args[i].equals("-h")) {
        printUsage();
        System.exit(0); 
      }
      
      else if (args[i].equals("-dport")) {
        D_SER_PORT = new Integer(args[i+1]).intValue();
        i++;
      }

      else if (args[i].equals("-headnodeip")) {
	hostIP = args[i+1] ;
	i++;
      }
      
      else if (args[i].equals("-dev")) {
        deviceName = args[i+1];
        i++;
	if(!(deviceName.equals("niodev") || deviceName.equals("mxdev") ||
	                    deviceName.equals("multicore"))){
	  System.out.println("MPJ Express currently does not support the <"+
	                                   deviceName+"> device.");
          System.out.println("Possible options are niodev, mxdev, and "+
	                               "multicore devices.");
	  System.out.println("exiting ...");
	  System.exit(0); 
	}
      } 

      else if (args[i].equals("-machinesfile")) {
        machinesFile = args[i+1];
        i++;
      }

      else if (args[i].equals("-wdir")) {
        wdir = args[i+1];
        i++;
      }

      else if(args[i].equals("-psl")) {
        psl = new Integer(args[i+1]).intValue();
        i++;
      }
      
      else if (args[i].equals("-mpjport")) {
        MPJ_SERVER_PORT = new Integer(args[i+1]).intValue();
        i++;
      }
      
      else if (args[i].equals("-mxboardnum")) {
        mxBoardNum = new Integer(args[i+1]).intValue();
        i++;
      }
      
      else if (args[i].equals("-cp") | args[i].equals("-classpath")) {
        jvmArgs.add("-cp");
	jvmArgs.add(args[i+1]);
        i++;
      }
      
      else if (args[i].equals("-sport")) {
        S_PORT = new Integer(args[i+1]).intValue();
        i++;
      }
      
      else if(args[i].equals("-jar")) {
        File tFile = new File(args[i+1]);
	String absJarPath = tFile.getAbsolutePath();
	
	if(tFile.exists()) {
          applicationClassPathEntry = new String(absJarPath) ; 

          try { 
            JarFile jarFile = new JarFile(absJarPath) ;
            Attributes attr = jarFile.getManifest().getMainAttributes();
            className = attr.getValue(Attributes.Name.MAIN_CLASS);
          } catch(IOException ioe) { 
            ioe.printStackTrace() ; 
          } 
	  parallelProgramNotYetEncountered = false ; 
	  i++;
	}
	else {
          throw new MPJRuntimeException("mpjrun cannot find the jar file <"+
			  args[i+1]+">. Make sure this is the right path.");	
	}
	
      }

      else {
	      
        //these are JVM options .. 
        if(parallelProgramNotYetEncountered) {
          if(args[i].startsWith("-")) { 		
	    jvmArgs.add(args[i]); 
	  }
          else {
            //This code takes care of executing class files directly ....
            //although does not look like it ....
            applicationClassPathEntry = System.getProperty("user.dir");	      
 	    className = args[i];
	    parallelProgramNotYetEncountered = false ; 
          }
	}
	
        //these have to be app arguments ...		
	else {
          appArgs.add(args[i]);		
	}

      }

    }

    jArgs = jvmArgs.toArray(new String[0]);
    aArgs = appArgs.toArray(new String[0]);

    if(DEBUG && logger.isDebugEnabled()) {

      logger.debug("###########################"); 	    
      logger.debug("-appargs: <"+applicationArgs+">");
      logger.debug("-dport: <"+D_SER_PORT+">");
      logger.debug("-mpjport: <"+MPJ_SERVER_PORT+">");
      logger.debug("-sport: <"+S_PORT+">");
      logger.debug("-np: <"+nprocs+">");
      logger.debug("$MPJ_HOME: <"+mpjHomeDir+">");
      logger.debug("-dir: <"+codeBase+">"); 
      logger.debug("-dev: <"+deviceName+">");
      logger.debug("-psl: <"+psl+">");
      logger.debug("jvmArgs.length: <"+jArgs.length+">");
      logger.debug("className : <"+className+">");
      logger.debug("applicationClassPathEntry : <"+applicationClassPathEntry+">");
      

      for(int i=0; i<jArgs.length ; i++) {
	  if(DEBUG && logger.isDebugEnabled())
        logger.debug(" jvmArgs["+i+"]: <"+jArgs[i]+">");	      
      }
      if(DEBUG && logger.isDebugEnabled())
      logger.debug("appArgs.length: <"+aArgs.length+">");

      for(int i=0; i<aArgs.length ; i++) {
	  if(DEBUG && logger.isDebugEnabled())
        logger.debug(" appArgs["+i+"]: <"+aArgs[i]+">");	      
      }
      
      if(DEBUG && logger.isDebugEnabled())
      logger.debug("###########################"); 	    
    }

  }

  private synchronized void Wait() throws Exception {
    if (wait) {
	if(DEBUG && logger.isDebugEnabled())
      logger.debug("Waiting ...");
      this.wait();
	  if(DEBUG && logger.isDebugEnabled())
      logger.debug("Unwaiting ...");
    }

    wait = true;
  }

  private synchronized void Notify() {
  if(DEBUG && logger.isDebugEnabled())
    logger.debug("Notifying ..."); 				///////////////////////////////
    this.notify();
    wait = false;
  }

  private void assignTasks() throws Exception {
	  
    PrintStream cout = null;
    int rank = 0;
    String name = null;
    int port = MPJ_SERVER_PORT;

    try {
      cfos = new FileOutputStream(CONF_FILE);
    }
    catch (FileNotFoundException fnfe) {}

    cout = new PrintStream(cfos);
    int noOfMachines = machineVector.size();
    cout.println("# Number of Processes");
    cout.println(nprocs);
    cout.println("# Protocol Switch Limit");
    cout.println(psl);
    cout.println("# Entry, HOST_NAME/IP@SERVERPORT@RANK");
    
    //have to modify later!!!!!
    cout.println(checkpointHost + "$" + checkpointPort + "$0");

    if (nprocs < noOfMachines) {

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug("Processes Requested " + nprocs +
                  " are less than than machines " + noOfMachines);
        logger.debug("Adding 1 processes to the first " + nprocs +
                  " items");
      }

      for (int i = 0; i < nprocs; i++) {
        //name=(String)machineVector.get(i);
        //name=InetAddress.getByName(name).getHostName();
        //name=InetAddress.getByAddress( name.getBytes() ).getHostName();
        procsPerMachineTable.put( (String) machineVector.get(i),
                                 new Integer(1));
	 
	if(deviceName.equals("niodev")) { 
          cout.println(name + "@" + port +
                       "@" + (rank++));
          port += 2;
	} else if(deviceName.equals("mxdev")) { 
          cout.println(name + "@" + mxBoardNum+
                       "@" + (rank++));
	} 
	
	
        if(DEBUG && logger.isDebugEnabled()) { 
          logger.debug("procPerMachineTable==>" + procsPerMachineTable);
	}

      }

    }
    else if (nprocs > noOfMachines) {
	//System.out.println(nprocs);
	//System.out.println();
     if(DEBUG && logger.isDebugEnabled())
	 {
      logger.debug("Processes Requested " + nprocs +
                  " are greater than than machines " + noOfMachines);
      }
	  int divisor = nprocs / noOfMachines;
      if(DEBUG && logger.isDebugEnabled())
	 {
	  logger.debug("divisor " + divisor);
      }
	  int remainder = nprocs % noOfMachines;
      if(DEBUG && logger.isDebugEnabled())
	 {
	  logger.debug("remainder " + remainder);
     }
      for (int i = 0; i < noOfMachines; i++) {
	      
        if (i < remainder) {
		
          procsPerMachineTable.put( (String) machineVector.get(i),
                                   new Integer(divisor + 1));
          if(DEBUG && logger.isDebugEnabled()) { 
            logger.debug("procPerMachineTable==>" + procsPerMachineTable);
	  }
	  
          //name=(String)machineVector.get(i);
          //name=InetAddress.getByAddress( name.getBytes() ).getHostName();
          //name=InetAddress.getByName(name).getHostName();

          for (int j = 0; j < (divisor + 1); j++) {
            if(deviceName.equals("niodev")) { 		  
              cout.println( (String) machineVector.get(i) + "@" +
                           port + "@" + (rank++));
              port += 2;
	    } else if(deviceName.equals("mxdev")) { 
              cout.println( (String) machineVector.get(i) + "@" +
                           (mxBoardNum+j) + "@" + (rank++));
	    }
          }
	  
        }
	
        else if (divisor > 0) {
          procsPerMachineTable.put( (String) machineVector.get(i),
                                   new Integer(divisor));
	  
          if(DEBUG && logger.isDebugEnabled()) { 
            logger.debug("procPerMachineTable==>" + procsPerMachineTable);
	  }

          //name=(String)machineVector.get(i);
          //name=InetAddress.getByAddress( name.getBytes() ).getHostName();
          for (int j = 0; j < divisor; j++) {
            if(deviceName.equals("niodev")) { 		  
              cout.println( (String) machineVector.get(i) + "@" +
                           port + "@" + (rank++));
              port += 2;
	    } else if(deviceName.equals("mxdev")) { 
              cout.println( (String) machineVector.get(i) + "@" +
                           (mxBoardNum+j) + "@" + (rank++));
	    }
          }
        }
      }

    }
    else if (nprocs == noOfMachines) {

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug("Processes Requested " + nprocs +
                  " are equal to machines " + noOfMachines);
        logger.debug("Adding a process each into the hashtable");
      }
      
      for (int i = 0; i < nprocs; i++) {
        procsPerMachineTable.put( (String) machineVector.get(i), 
                                  new Integer(1));
	if(deviceName.equals("niodev")) { 
          cout.println( (String) machineVector.get(i) + "@" + port+
                       "@" + (rank++));
          port += 2;
	} else if(deviceName.equals("mxdev")) { 
          cout.println( (String) machineVector.get(i) + "@" +
                       (mxBoardNum) + "@" + (rank++));
	}
	
        if(DEBUG && logger.isDebugEnabled()) { 
          logger.debug("procPerMachineTable==>" + procsPerMachineTable);
	}
      }

    }

    cout.close(); 
    cfos.close(); 

  }
  
  
  //assign tasks after restart
  private void restartTasks() throws Exception {
		boolean finished = false;
		while(!finished){
			ByteBuffer killMsg = ByteBuffer.allocate(4);
			killMsg.put("kill".getBytes());
				
		    SocketChannel socketChannel = null;
		    machineVector.clear();
		    machineConnectedMap.clear();
		    readMachineFile();
		    ArrayList<String> validMachines = new ArrayList<String>();
		    
		    for(int i = 0; i < machineVector.size(); i++){
		    	String daemon = (String) machineVector.get(i);
		    	socketChannel = machineChannelMap.get(daemon);
		    	if(socketChannel == null || !(socketChannel.isOpen() && socketChannel.isConnected())){
		    		socketChannel = SocketChannel.open();
		    		socketChannel.configureBlocking(true);
		    		
		    		if(true == socketChannel.connect(new InetSocketAddress(daemon, D_SER_PORT))){
		    			doConnect(socketChannel, true);
		    			machineConnectedMap.put(daemon, true);
		    			machineChannelMap.put(daemon, socketChannel);
		    			validMachines.add(daemon);
		    		}else if(machineChannelMap.get(daemon) != null){
		    			machineChannelMap.remove(daemon);
		    		}
		    	}
		    	else{
		    		killMsg.flip();
		    		int s = 0;
		    		int w = 0;
		    		while(killMsg.hasRemaining()){
		    			try{
		    				if((w = socketChannel.write(killMsg)) == -1)
		    					throw new ClosedChannelException();
		    			}
		    			catch(IOException e){
		    				machineChannelMap.remove(daemon);
		    				break;
		    			}	    
		    			s += w;
		    		}
		    		if(s == 4){
		    			machineConnectedMap.put(daemon, true);
		    			validMachines.add(daemon);	
		    		}
		    		
		    	}
		    }
		    
		    
		    finished  = assignRestartTasks(validMachines);
		    
		    
		}
		
  }
  

  private boolean assignRestartTasks(ArrayList<String> validMachines) {
	    
		//access database
	  
	  return true;
		
	}

private void machinesSanityCheck() throws Exception {
	  
    for(int i=0 ; i<machineVector.size() ; i++) {
	    
      String host = (String) machineVector.get(i) ;

      try {
        InetAddress add = InetAddress.getByName(host);
      } catch( Exception e) {
        throw new MPJRuntimeException (e);	      
      }
      
    }

  }

  /* assume 'machines'is in the current directory */
  public void readMachineFile() throws Exception {

    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new FileReader( machinesFile ));
    }
    catch (FileNotFoundException fnfe) {
      throw new MPJRuntimeException ( "<"+ machinesFile + "> file cannot "+
                            " be found." +
                            " The starter module assumes "+
                            "it to be in the current directory.");
    }

    boolean loop = true;
    String line = null;
    int machineCount = 0 ; 

    while (machineCount < nprocs) {

      line = reader.readLine();

      if(DEBUG && logger.isDebugEnabled()) {
        logger.debug("line <" + line + ">");
      }

      if(line == null) { 
        break ; 
      }

      if (line.startsWith("#") || line.equals("") ||
          (machineVector.size() == nprocs)) {
        //loop = false;
        continue ;
      }


      machineCount ++ ;

      line = line.trim();

      InetAddress address = InetAddress.getByName(line);
      String addressT = address.getHostAddress();
      String nameT = address.getHostName();

      if(DEBUG && logger.isDebugEnabled()) {
        logger.debug("nameT " + nameT);
        logger.debug("addressT " + addressT);
      } 
     
      boolean alreadyPresent = false;
      
      for(int i=0 ; i<machineVector.size() ; i++) {

        String machine = (String) machineVector.get(i); 

        if(machine.equals(nameT) || machine.equals(addressT)) {  
           alreadyPresent = true;
           break ;
        }

      }

      if(!alreadyPresent) { 

        //if( addressT or nameT already present, then you are buggered ) {
        //}
      
        /* What is the solution for this? */
        //machineVector.add(addressT);
        machineVector.add(nameT);
        machineConnectedMap.put(nameT, false);

        if(DEBUG && logger.isDebugEnabled()) {
          logger.debug("Line " + line.trim() +
                    " added to vector " + machineVector);
        }

      }

    }//end while.
  
  }

  private static int getPortFromWrapper() {

    int port = 0;
    FileInputStream in = null;
    DataInputStream din = null;
    BufferedReader reader = null;
    String line = "";

    try {

      String path = System.getenv("MPJ_HOME")+"/conf/wrapper.conf";
      in = new FileInputStream(path);
      din = new DataInputStream(in);
      reader = new BufferedReader(new InputStreamReader(din));

      while ((line = reader.readLine()) != null)   {
        if(line.startsWith("wrapper.app.parameter.2")) {
          String trimmedLine=line.replaceAll("\\s+", "");
          port = Integer.parseInt(trimmedLine.substring(24));
          break;
        }
      }

      in.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return port;

  }
  
  private static int getCheckpointPort() {

	    int port = 0;
	    FileInputStream in = null;
	    DataInputStream din = null;
	    BufferedReader reader = null;
	    String line = "";

	    try {

	      String path = System.getenv("MPJ_HOME")+"/conf/wrapper.conf";
	      in = new FileInputStream(path);
	      din = new DataInputStream(in);
	      reader = new BufferedReader(new InputStreamReader(din));

	      while ((line = reader.readLine()) != null)   {
	        if(line.startsWith("wrapper.checkpointServer.port")) {
	          String trimmedLine=line.replaceAll("\\s+", "");
	          port = Integer.parseInt(trimmedLine.substring(30));
	          break;
	        }
	      }

	      in.close();

	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    return port;

	  }
  
  private static String getCheckpointHost() {

	    String host = null;
	    FileInputStream in = null;
	    DataInputStream din = null;
	    BufferedReader reader = null;
	    String line = "";

	    try {

	      String path = System.getenv("MPJ_HOME")+"/conf/wrapper.conf";
	      in = new FileInputStream(path);
	      din = new DataInputStream(in);
	      reader = new BufferedReader(new InputStreamReader(din));

	      while ((line = reader.readLine()) != null)   {
	        if(line.startsWith("wrapper.checkpointServer.host")) {
	          String trimmedLine=line.replaceAll("\\s+", "");
	          host = trimmedLine.substring(30);
	          break;
	        }
	      }

	      in.close();

	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    return host;

	  }

  private void clientSocketInit() throws Exception {
      	  
    SocketChannel[] clientChannels = new SocketChannel[machineVector.size()];
    for (int i = 0; i < machineVector.size(); i++) {
      boolean connected = false ; 	    
      String daemon = (String) machineVector.get(i);
      try {
        clientChannels[i] = SocketChannel.open();
        clientChannels[i].configureBlocking(true);
		if(DEBUG && logger.isDebugEnabled())
		{
        logger.debug("Connecting to " + daemon + "@" + D_SER_PORT);
		}      
	  connected = clientChannels[i].connect(
			new InetSocketAddress(daemon, D_SER_PORT));

	if(!connected) {
	  System.out.println(" home-made ...");

          if(System.getProperty("os.name").startsWith("Windows")) {   
            CONF_FILE.delete() ;
          }

          throw new MPJRuntimeException("Cannot connect to the daemon "+
			  "at machine <"+daemon+"> and port <"+
			  D_SER_PORT+">."+
			  "Please make sure that the machine is reachable "+
			  "and running the daemon in 'sane' state"); 
	}

	doConnect(clientChannels[i], true); 
	machineConnectedMap.put(daemon, true);
	machineChannelMap.put(daemon, clientChannels[i]);
	
      }
      catch(IOException ioe) {
        if(System.getProperty("os.name").startsWith("Windows")) {   
          CONF_FILE.delete() ;
        }

	System.out.println(" IOException in doConnect");
        throw new MPJRuntimeException("Cannot connect to the daemon "+
			"at machine <"+daemon+"> and port <"+
			D_SER_PORT+">."+
			"Please make sure that the machine is reachable "+
			"and running the daemon in 'sane' state"); 
      }
      catch (Exception ccn1) {
	  System.out.println(" rest of the exceptions ");
        throw ccn1;
      }
    }
    
    //connect to the checkpoint server
    //in checkpoint server  original port for writable,original port+1 for readable, original port+2 for control
    int port  = checkpointPort + 2;
    try{
    	checkpiontChannel = SocketChannel.open();
    	checkpiontChannel.configureBlocking(true);
    	boolean connected = checkpiontChannel.connect(new InetSocketAddress(checkpointHost, port ));
    	if(connected == false){
    		
    		if(System.getProperty("os.name").startsWith("Windows")) {   
                CONF_FILE.delete() ;
            }
    		
    		throw new MPJRuntimeException("Cannot connect to the checpont server "+
        			"at machine <"+checkpointHost+"> and port <"+
        			port+">."+
        			"Please make sure that the machine is reachable.");     		
    	}
    	
    	//have connected so add to the selector
    	doConnect(checkpiontChannel, false);
    	ByteBuffer numBuffer = ByteBuffer.allocate(8);
    	numBuffer.putInt(NUM_OF_PROCCESSES);
    	numBuffer.putInt(nprocs);
    	
    	numBuffer.flip();
    	
    	while(numBuffer.hasRemaining()){
    		try{
    			if(checkpiontChannel.write(numBuffer) == -1)
    				throw new ClosedChannelException();
    		}
    		catch(Exception e){
    			throw e;
    		}
    	}
    	
    	numBuffer.clear();
    	
    }
    catch(IOException ie){
    	if(System.getProperty("os.name").startsWith("Windows")) {   
            CONF_FILE.delete() ;
        }
    	throw new MPJRuntimeException("Cannot connect to the checpont server "+
    			"at machine <"+checkpointHost+"> and port <"+
    			port+">."+
    			"Please make sure that the machine is reachable."); 
    	
    }
    catch(Exception e){
    	if(System.getProperty("os.name").startsWith("Windows")) {   
            CONF_FILE.delete() ;
        }
    	e.printStackTrace();
    	throw e;
    }
    
  }

  /**
   * This method cleans up the device environments, closes the selectors, serverSocket, and all the other socketChannels
   */
  public void finish() {
   if(DEBUG && logger.isDebugEnabled())
	{
    logger.debug("\n---finish---");
	}
    try {
      if(DEBUG && logger.isDebugEnabled())
	 {
      logger.debug("Waking up the selector");
      
	 }
	 selector.wakeup();
      selectorFlag = false;
	if(DEBUG && logger.isDebugEnabled())
	 {
      logger.debug("Closing the selector");
      }
	  selector.close();

      SocketChannel peerChannel = null;

      for (int i = 0; i < peerChannels.size(); i++) {
        peerChannel = peerChannels.get(i);
	if(DEBUG && logger.isDebugEnabled())
	 {       
	   logger.debug("Closing the channel " + peerChannel);
	 }
        if (peerChannel.isOpen()) {
          peerChannel.close();
        }


      }

      peerChannel = null;
    }
    catch (Exception e) {
      //e.printStackTrace();
    }
  }

  private void doConnect(SocketChannel peerChannel, boolean isDeamonChannel) {
  if(DEBUG && logger.isDebugEnabled())
    logger.debug("---doConnect---");
    try {
	if(DEBUG && logger.isDebugEnabled())
      logger.debug("Configuring it to be non-blocking");
      peerChannel.configureBlocking(false);
    }
    catch (IOException ioe) {
	if(DEBUG && logger.isDebugEnabled())
      logger.debug("Closed Channel Exception in doConnect");
      System.exit(0);
    }

    try {
	if(DEBUG && logger.isDebugEnabled())
      logger.debug("Registering for OP_READ & OP_WRITE event");
      peerChannel.register(selector,
                           SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
    catch (ClosedChannelException cce) {
	if(DEBUG && logger.isDebugEnabled())
      logger.debug("Closed Channel Exception in doConnect");
      System.exit(0);
    }

    try {
      peerChannel.socket().setTcpNoDelay(true);
    }
    catch (Exception e) {}
    
    if(isDeamonChannel){
    	peerChannels.add(peerChannel);
    	if(DEBUG && logger.isDebugEnabled())
    	{
        logger.debug("Adding the channel " + peerChannel + " to " + peerChannels);
        logger.debug("Size of Peer Channels vector " + peerChannels.size());
        }
    	peerChannel = null;
        if (peerChannels.size() == machineVector.size()) {
          Notify();
        }
    }
    
  }
  
  /**
   * Entry point to the class 
   */
  public static void main(String args[]) throws Exception {

    try {
      MPJRun client = new MPJRun(args);
    }
    catch (Exception e) {
      throw e;
    }

  }

  private void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          for (int j = 0; j < peerChannels.size(); j++) {
            SocketChannel socketChannel = null;
            socketChannel = peerChannels.get(j);
            buffer.clear();
            buffer.put( (new String("kill")).getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
          }

          cfos.close();
        }
        catch(Exception e){
        }
      }
    });
  }

  Runnable selectorThread = new Runnable() {

    /* This is selector thread */
    public void run() {
if(DEBUG && logger.isDebugEnabled())     
	 logger.debug("selector Thread started ");
      Set readyKeys = null;
      Iterator readyItor = null;
      SelectionKey key = null;
      SelectableChannel keyChannel = null;
      SocketChannel socketChannel = null;
      ByteBuffer lilBuffer = ByteBuffer.allocateDirect(4);
      ByteBuffer bigBuffer = ByteBuffer.allocateDirect(10000);

      try {
        while (endCount < machineVector.size() && selector.select() > -1 && selectorFlag == true) {

          readyKeys = selector.selectedKeys();
          readyItor = readyKeys.iterator();
          int read = 0;

          while (readyItor.hasNext()) {

            key = (SelectionKey) readyItor.next();
            readyItor.remove();
            keyChannel = (SelectableChannel) key.channel();
			if(DEBUG && logger.isDebugEnabled())
            logger.debug("\n---selector EVENT---");

            if (key.isAcceptable()) {
              //doAccept(keyChannel);
			  if(DEBUG && logger.isDebugEnabled())
              logger.debug("ACCEPT_EVENT");
            }

            else if (key.isConnectable()) {
				if(DEBUG && logger.isDebugEnabled())
              logger.debug("CONNECT_EVENT");
              try {
                socketChannel = (SocketChannel) keyChannel;
              }
              catch (NoConnectionPendingException e) {
                continue;
              }

              if (socketChannel.isConnectionPending()) {
                try {
                  socketChannel.finishConnect();
                }
                catch (IOException e) {
                  continue;
                }
              }

              doConnect(socketChannel, true);
            }

            else if (key.isReadable()) { 
              //if(DEBUG && logger.isDebugEnabled())
              //logger.debug("READ_EVENT");
              socketChannel = (SocketChannel) keyChannel;
              
              lilBuffer.clear();
              read = 0;
              while(lilBuffer.hasRemaining()){
            	  if((read = socketChannel.read(lilBuffer)) == -1 ){
            		  break;
            	  }
              }
              
              if (read != -1) {
                  lilBuffer.flip();
                  read = lilBuffer.getInt();
                  lilBuffer.clear();
              }
              else {
            	  read = END_OF_STREAM;
              }

              if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
            	  logger.debug("---READ_EVENT---" + read );
              }

              /* 
               * It would be ideal if this piece of code is called ...
               * but it appears ..its never callled ..maybe the behaviour
               * of closing down that we saw was Linux dependant ????
               */ 
              InetAddress ias=socketChannel.socket().getInetAddress();
              if(DEBUG && logger.isDebugEnabled())
  				logger.debug("HostName: " + ias.getHostName() + " IP Adress: " + ias.getHostAddress()) ; 

              switch(read){
              	case END_OF_STREAM:
              		if((Boolean)(machineConnectedMap.get(ias.getHostName()))==true){
                		if(DEBUG && logger.isDebugEnabled())
            				logger.debug("END_OF_STREAM signal at starter from "+
                                         "channel "+socketChannel) ;  
                           
                        streamEndedCount ++ ; 
                        machineConnectedMap.put(ias.getHostName(),false);
                        socketChannel.close();
                    	logger.debug("streamEndedCount = "+streamEndedCount);  

                    	/*
                        if (streamEndedCount == machineVector.size()) {
            				if(DEBUG && logger.isDebugEnabled())
            				{
            					logger.debug("The starter has received "+ 
            	                               machineVector.size() +"signals"); 
            	                logger.debug("This means its time to exit"); 
            				}                 
            				Notify();
                        }
                        */
                	}
              		
              		break;
              		
              	case LONG_MESSAGE:
              		lilBuffer.clear();
              		while(lilBuffer.hasRemaining()){
              			try{
	              			if(socketChannel.read(lilBuffer) == -1){
	              				throw new ClosedChannelException();
	              			}
              			}
              			catch(IOException e){
              				e.printStackTrace();
              				break;
              			}
              		}
              			
              		
              		lilBuffer.flip();
              		int len = lilBuffer.getInt();
              		ByteBuffer tempBuffer = ByteBuffer.allocate(len);
              		
              		while(tempBuffer.hasRemaining()){
              			try{
	              			if(socketChannel.read(tempBuffer) == -1){
	              				throw new ClosedChannelException();
	              			}
              			}
              			catch(IOException e){
              				e.printStackTrace();
              				break;
              			}
              		}
              		
              	 	byte[] tempArray = new byte[len];
              	 	//logger.debug("bigBuffer " + bigBuffer + "From :" + socketChannel);
              	 	tempBuffer.flip();
              	 	tempBuffer.get(tempArray, 0, len);
              	 	String line = new String(tempArray);
              	 	tempBuffer.clear();
              	 	System.out.print(line);
              	 	//RECEIVED
  					  if(DEBUG && logger.isDebugEnabled()){
  			      logger.debug("Buffer content , Size:" + line.length() + "From :" + socketChannel);
  					 }
              		
              		break;
              		
              	case INT_MESSAGE:
              		lilBuffer.clear();
              		while(lilBuffer.hasRemaining())
              			socketChannel.read(lilBuffer);
              		
              		int num = lilBuffer.getInt();
              		
              		break;
              		
              	case DAEMON_EXIT:
              		endCount++;
			        if(DEBUG && logger.isDebugEnabled())
						{
						logger.debug("endCount " + endCount);
			        logger.debug("machineVector.size() " + machineVector.size());
						}
			        if (endCount == machineVector.size()) {
						if(DEBUG && logger.isDebugEnabled())
						 logger.debug("Notify and exit"); 
						
						ByteBuffer endMsg = ByteBuffer.allocate(4);
						endMsg.putInt(END_APP);
						endMsg.flip();
						while(endMsg.hasRemaining()){
							try{
								if(checkpiontChannel.write(endMsg) == -1)
									throw new ClosedChannelException();
							}
							catch(IOException e){
								e.printStackTrace();
								if(DEBUG && logger.isDebugEnabled())
								{
									logger.debug("the checkpiont channel is close, this should not happen!");
								}								
							}
						}
						if(DEBUG && logger.isDebugEnabled())
						{
							logger.debug("finish send END_APP to the checkpiont server");
						}	
						Notify();
			        }
			        
              		break;
              		
              	case REQUEST_RESTART:
              		//retrive the database and assign job and version number
              		restartTasks();
              		//send "kill" to the daemon
              		
              		//pack and send the command like before
              		
              		
              		break;
              		
            	
              	default:
              		System.out.println("Impossible");
              }    

            } //end if key.isReadable()

            else if (key.isWritable()) {
			if(DEBUG && logger.isDebugEnabled())
              logger.debug(
                  "In, WRITABLE, so changing the interestOps to READ_ONLY");
              key.interestOps(SelectionKey.OP_READ);
            }
          }
        }
      }
      catch (Exception ioe1) {
	  if(DEBUG && logger.isDebugEnabled())
        logger.debug("Exception in selector thread ");
        ioe1.printStackTrace();
        System.exit(0);
      }
	  if(DEBUG && logger.isDebugEnabled())
      logger.debug("Thread getting out");
    }

  };
  
  
  Runnable heartBeatThread = new Runnable() {
		
		@Override
		public void run() {
			if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("start heart beat thread");
	        }
			
			while(!isFinished){
				try {
					heartBeatLock.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					//if the lock is interrupted then exit the thread
					break;
				}
				
				ByteBuffer buf = ByteBuffer.allocate(4);
				buf.putInt(CHECK_VALID);
				
				for(int i = 0; i < peerChannels.size(); i++){
					buf.flip();
					while(buf.hasRemaining()){
						try{
							if(peerChannels.get(i).write(buf) == -1)
								throw new ClosedChannelException();
						}
						catch(IOException ioe){
							ioe.printStackTrace();
							System.out.println("Some daemon has been down! So Restart");
							if (DEBUG && logger.isDebugEnabled()) {
					              logger.debug("Some daemon has been down! So Restart");
					        }
							
							heartBeatLock.signal();
							
							doRestart();
							return;
						}
					}
				}

					
				

			
				try {
					Thread.currentThread().sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				heartBeatLock.signal();
			
			}//end while isFinished
			
			
			if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("exit heart beat thread");
	        }
		}//end run

		
	};
	
	private void doRestart() {
		// TODO Auto-generated method stub
		
	}
	
	  class CustomSemaphore {

		    private int s ;
		    
		    public CustomSemaphore(int s) {
		      this.s = s ;
		    }
		    
		    public synchronized void acquire() throws InterruptedException {
		      while (s == 0) wait(0) ;
		      s-- ;
		    }
		    
		    public synchronized void signal() {
		      s++ ;
		      notify() ;
		    }
		    
		  }
}
