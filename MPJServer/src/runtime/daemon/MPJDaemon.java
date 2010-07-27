/*
 The MIT License

 Copyright (c) 2005 - 2008
   1. Distributed Systems Group, University of Portsmouth (2005)
   2. Aamir Shafi (2005 - 2008)
   3. Bryan Carpenter (2005 - 2008)
   4. Mark Baker (2005 - 2008)

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
 * File         : MPJDaemon.java 
 * Author       : Aamir Shafi, Bryan Carpenter
 * Created      : Sun Dec 12 12:22:15 BST 2004
 * Revision     : $Revision: 1.28 $
 * Updated      : $Date: 2006/10/20 17:24:47 $
 */

package runtime.daemon;

import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;

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
import sun.nio.cs.ext.MacHebrew;
import xdev.XDevException;

import java.util.concurrent.Semaphore ; 

public class MPJDaemon {

  private SocketChannel peerChannel; 
  private BufferedReader reader = null;
  private InputStream outp = null;
  private int D_SER_PORT = 10000;
  private boolean loop = true;
  private Selector selector = null;
  private volatile boolean selectorAcceptConnect = true;
  private volatile boolean kill_signal = false;
  private volatile boolean wait = true;
  private volatile boolean waitToStartExecution = true;
  private PrintStream out = null;
  private Semaphore outputHandlerSem = new Semaphore(1,true); 
  static final boolean DEBUG = true ;
  
  private String wdir = null ; 
  private String applicationClassPathEntry = null; 
  private String deviceName = null;
  private String className = null ;
  private String mpjHome = null ;
  private ArrayList<String> jvmArgs = new ArrayList<String>();
  private ArrayList<String> appArgs = new ArrayList<String>();
  private int processes = 0;
  private int nprocs = 0;
  private String cmd = null;
  private Process p[] = null ; 
  static Logger logger = null ; 
  private String mpjHomeDir = null ;  
  private String USER_DIR = "user-folder";
  private String SYSTEM_LIB_DIR = "lib";
  String configFileName = null ;
  
  //Vector<SocketChannel> writableChannels = null;
  //Vector<SocketChannel> tempWritableChannels = new Vector<SocketChannel> ();

  //Vector<SocketChannel> readableChannels = null;
  //Vector<SocketChannel> tempReadableChannels = new Vector<SocketChannel> ();
  Vector<SocketChannel> processChannels = new Vector<SocketChannel> ();
  Vector<SocketChannel> tempProcessChannels = new Vector<SocketChannel> ();
  
  Hashtable<UUID, SocketChannel> worldProcessTable =
		new Hashtable<UUID, SocketChannel> ();
  
  Hashtable<UUID, Integer> checkpointingProcessTable = new Hashtable<UUID, Integer> ();
  
  Hashtable<UUID, Boolean> processValidMap = new Hashtable<UUID, Boolean> ();
  Hashtable<UUID, Boolean> processFinishMap = new Hashtable<UUID, Boolean> ();

  
  private boolean initializing = false;
  private boolean isFinished = false;
  private boolean isRestarting = false;
  private CustomSemaphore initLock = new CustomSemaphore(1); 
  private CustomSemaphore finishLock = new CustomSemaphore(1); 
  private CustomSemaphore heartBeatLock = new CustomSemaphore(1); 
  private CustomSemaphore heartBeatBeginLock = new CustomSemaphore(1); 
  private CustomSemaphore startLock = new CustomSemaphore(1); 
  private CustomSemaphore processStartLock = new CustomSemaphore(1); 
  private Object sendRestartRequestLock = new Object(); 
  private Thread renewThreadStarter = null;
  private Thread heartBeatStarter = null;
  UUID[] pids = null;
  
  public static final int LONG_MESSAGE = -45;
  public static final int DAEMON_EXIT = -46;
  public static final int INT_MESSAGE = -47;
  private final int DAEMON_MARKER_ACK = -35;
  private final int DAEMON_EXIT_ACK = -36;
  private final int REQUEST_RESTART = -70;
  private final int CHECK_VALID = -71;
  private final int START_CHECKPOINT_WAVE = -31;
  
  private final int MAX_CHECKPOINT_INVALID_TIME = 4;
  private boolean isRestartFromCheckpoint = false;
  private boolean hasSendRequest = false;  
  private static String JAVA_TEMP_FILE_DIRECTORY = "/tmp/hsperfdata_" + System.getProperty("user.name") + "/";
  

  public MPJDaemon(String args[]) throws Exception {
	  
    InetAddress localaddr = InetAddress.getLocalHost();
    String hostName = localaddr.getHostName();
    
    Map<String,String> map = System.getenv() ;
    mpjHomeDir = map.get("MPJ_HOME");
			    
    createLogger(mpjHomeDir, hostName); 

    if(DEBUG && logger.isDebugEnabled()) { 
      logger.debug("mpjHomeDir "+mpjHomeDir); 
    }

    if (args.length == 1) {
	    
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug (" args[0] " + args[0]);
        logger.debug ("setting daemon port to" + args[0]);
      }

      D_SER_PORT = new Integer(args[0]).intValue();

    }
    else {
      throw new MPJRuntimeException("Usage: java MPJDaemon daemonServerPort");
    }

    serverSocketInit();
    Thread selectorThreadStarter = new Thread(selectorThread);
    
    if(DEBUG && logger.isDebugEnabled()) { 
      logger.debug ("Starting the selector thread ");
    }

    selectorThreadStarter.start();
    
    int exit = 0;

    while (loop) {
    	
    	isRestarting = false;
    	kill_signal = false;
    	sendRestartRequestLock = new CustomSemaphore(1); 
    	startLock = new CustomSemaphore(1);
    	
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("MPJDaemon is waiting to accept connections ... ");
      }
      
      //wdir = System.getProperty("user.dir");
      //System.out.println("Test user.dir in daemon:" + System.getProperty("user.dir"));

      waitToStartExecution ();


      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("A client has connected");
      }

      MPJProcessPrintStream.start(peerChannel, 
                       new PrintStream(System.out),
                       new PrintStream(System.err));

      BufferedReader bufferedReader = null;
      InputStream in = null;

      File configFile = new File(configFileName) ; 
      configFile.createNewFile();

      try {
        in = new FileInputStream(configFile);
      }
      catch (Exception e) {
        e.printStackTrace();
      }

      bufferedReader = new BufferedReader(new InputStreamReader(in));

      OutputHandler [] outputThreads = new OutputHandler[processes] ;  
      p = new Process[processes];  
      pids = new UUID[nprocs];
      
      processStartLock.acquire();
      if(kill_signal == false){
	      try{
	
	    	  
	    	  
	    	  
		      for (int j = 0; j < processes; j++) {
		
		        /* Step 1: Read from the config file - basically need to know
		                   rank of processes */ 
		        String line = null;
		        String rank = null; 
		        
		        if(isRestartFromCheckpoint == false){
		        	jvmArgs.add("-Djava.library.path=."+File.pathSeparator+"/usr/local/lib"+
		        		File.pathSeparator+ mpjHomeDir + File.separator + SYSTEM_LIB_DIR);
		        }
		
		        while((line = bufferedReader.readLine()) != null) {
		
		          if(DEBUG && logger.isDebugEnabled()) { 
		            //logger.debug ("line ="+line);
		          }
		
		          if(MPJDaemon.matchMe(line) ) {
		            StringTokenizer tokenizer = new StringTokenizer(line, "@");
		            tokenizer.nextToken();
		            tokenizer.nextToken();
		            rank = tokenizer.nextToken();
		            break ;
		          }
		
		        } //end while
		
		        if(DEBUG && logger.isDebugEnabled()) { 
		          logger.debug("out of while loop");
		        }
		
		        /* Step 2: Argument Processing */ 
		
		        String[] jArgs = jvmArgs.toArray(new String[0]); 
		        ProcessBuilder pb = null;
		        
		        if(isRestartFromCheckpoint == false){
		        	
			        boolean now = false;
			        boolean noSwitch = true ;
			
			        for(int e=0 ; e<jArgs.length; e++) {
			
			          if(DEBUG && logger.isDebugEnabled()) { 
			            //logger.debug("jArgs["+e+"]="+jArgs[e]);
				  }
			
			          if(now) {
			            String cp = jvmArgs.remove(e);
				      
			            cp = "."+File.pathSeparator+""+
			                  mpjHomeDir+"/lib/loader1.jar"+
			                  File.pathSeparator+""+mpjHomeDir+"/lib/log4j-1.2.11.jar"+
			                  File.pathSeparator+""+mpjHomeDir+"/lib/wrapper.jar"+
			                  File.pathSeparator+applicationClassPathEntry+
			                  File.pathSeparator+applicationClassPathEntry+"/dom4j-1.6.1.jar"+
			                  File.pathSeparator+applicationClassPathEntry+"/bin"+
			                  File.pathSeparator+cp;
				      
			            jvmArgs.add(e,cp);
			            now = false;
			          }
			
			          if(jArgs[e].equals("-cp")) {
			            now = true;
			            noSwitch = false;
			          }
			        }
			
			        if(noSwitch) {
			          jvmArgs.add("-cp");
				  jvmArgs.add("."+File.pathSeparator+""
			  	        +mpjHomeDir+"/lib/loader1.jar"+
			                File.pathSeparator+""+mpjHomeDir+"/lib/log4j-1.2.11.jar"+
			                File.pathSeparator+""+mpjHomeDir+"/lib/wrapper.jar"+
			                File.pathSeparator+applicationClassPathEntry+
			                File.pathSeparator+applicationClassPathEntry+"/dom4j-1.6.1.jar"+
			                File.pathSeparator+applicationClassPathEntry+"/bin") ; 
			        }
			        
			        
			
			        jArgs = jvmArgs.toArray(new String[0]);
		        
		        
			        jvmArgs.clear();
			 
			        for(int e=0 ; e<jArgs.length; e++) {
			          if(DEBUG && logger.isDebugEnabled()) { 
			            //logger.debug("modified: jArgs["+e+"]="+jArgs[e]);
				  }
			        }
				  
			        String[] aArgs = appArgs.toArray(new String[0]); 
			
			        int N_ARG_COUNT = 7 ; 
				  
			        String[] ex = new String[(N_ARG_COUNT+jArgs.length+aArgs.length)]; 
			        ex[0] = "java";
			
			        //System.arraycopy ... can be used ..here ...
			        for(int i=0 ; i<jArgs.length ; i++) { 
			          ex[i+1] = jArgs[i]; 	
			        }
			
			        int indx = jArgs.length+1; 
				
			        ex[indx] = "runtime.daemon.Wrapper" ; indx++ ;
			        ex[indx] = configFileName; indx++ ; 
			        ex[indx] = Integer.toString(processes); indx++ ; 
			        ex[indx] = deviceName; indx++ ; 
			        ex[indx] = rank; indx++ ; 
			        ex[indx] = className ; 
				  
			        //System.arraycopy ... can be used ..here ...
			        for(int i=0 ; i< aArgs.length ; i++) { 
			          ex[i+N_ARG_COUNT+jArgs.length] = aArgs[i]; 	
			        }
			
			        if(DEBUG && logger.isDebugEnabled()) { 
			          for (int i = 0; i < ex.length; i++) {
			            //logger.debug(i+": "+ ex[i]);
			          }  
			        }
			        
			        
			        /* Step 3: Now start a new JVM */ 
			        pb = new ProcessBuilder(ex);
			        pb.directory(new File(wdir)) ;
			        pb.redirectErrorStream(true); 
		        
		        }//end  of if isRestartFromCheckpoint == false
		        else{
		        	if(DEBUG && logger.isDebugEnabled()) { 
		                logger.debug("process restart args");
		            }
		        	
		        	String contextFilePath = jArgs[j*2];
		        	String tempFilePath = jArgs[j*2 + 1];
		        	
		        	if(DEBUG && logger.isDebugEnabled()) { 
		                logger.debug("contextFilePath:" + contextFilePath);
		                logger.debug("tempFilePath:" + tempFilePath);
		            }
		        	
		        	int pos = tempFilePath.lastIndexOf("/");
		        	String pathArg = tempFilePath.substring(pos + 1);
		        	String processId = pathArg.split("_")[0];
		        	
		        	String dstTempFilePath = JAVA_TEMP_FILE_DIRECTORY + processId;
		        	File srcTempFile = new File(tempFilePath);
		        	File dstTempFile = new File(dstTempFilePath);
		 
		        	
		        	if(DEBUG && logger.isDebugEnabled()) { 
		                logger.debug("copy the temp file");
		            }
		        	copyFile(srcTempFile, dstTempFile);
		        	
		        	String[] ex = new String[2];
		        	ex[0] = "cr_restart";
		        	ex[1] = contextFilePath;
		        	
		        	/* Step 3: Now start a new JVM */ 
			        pb = new ProcessBuilder(ex);
			        pb.directory(new File(wdir)) ;
			        pb.redirectErrorStream(true); 
		        }
			
		       
		        if(DEBUG && logger.isDebugEnabled()) { 
		          logger.debug("starting the process ");
		        }
		
		        p[j] = pb.start();
		
		        /* Step 4: Start a new thread to handle output from this particular
		                   JVM. 
		                   FIXME: Now this seems like a good amount of overhead. If
		                   we start 4 JVMs on a quad-core CPU, we also start 4 
		                   additional threads to handle I/O. Is it possible to 
		                   get rid of this overhead?
		                   */ 
		        outputThreads[j] = new OutputHandler(p[j], rank) ; 
		        outputThreads[j].start();
			  
		        if(DEBUG && logger.isDebugEnabled()) { 
		          logger.debug("started the process "); 
		        }
		      } //end for.
		      
	      }catch(Exception e){
	    	  e.printStackTrace();    	  
	    	  if(DEBUG && logger.isDebugEnabled()) { 
		          logger.debug("Exception in process starting!"); 
		      }
	    	  sendRestartReqestToMainHost();
	      }
      }//end of it kill_signal == false
      processStartLock.signal(); 
		      
		
		
      try { 
        bufferedReader.close() ; 
        in.close() ; 
      } catch(Exception e) { 
        e.printStackTrace() ; 
      } 
      
	  //when init, and worldprocessTable is not init properly
      Thread.currentThread().sleep(1000);
      synchronized (worldProcessTable) {
    	  if(DEBUG && logger.isDebugEnabled()) { 
              logger.debug("worldProcessTable.size(): " +worldProcessTable.size()); 
          }
    	  if(worldProcessTable.size() != processes){
    		  if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug("wait 8s for worldProcessTable"); 
              }
    		  worldProcessTable.wait(12000);
    		  if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug("After wait or notify worldProcessTable.size(): " +worldProcessTable.size()); 
              }
    		  if(worldProcessTable.size() != processes ){
    			  isFinished = true;
    			  sendRestartReqestToMainHost();
    		  }
    	  }
      }
		    	
	       
		
      //Wait for the I/O threads to finish. They finish when 
      // their corresponding JVMs finish. 
      for (int j = 0; j < processes; j++) {
        outputThreads[j].join();
      }
	      
	      
	      
      
      
      //if process finish before the heartbeat thread start, then check the 
      //processFinishmap to see if they are normal finish or not, 
      if(processFinishMap.size() != processes){
    	  isRestarting = true;
    	  //need to be fix latter
    	  //sendRestartReqestToMainHost();
      }
    	 

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("Stopping the output");
        if(renewThreadStarter != null)
        	logger.debug("renewThreadStarter state:" + renewThreadStarter.getState());
      }
      
      if(renewThreadStarter != null && (renewThreadStarter.getState().equals(Thread.State.BLOCKED) || 
    		  renewThreadStarter.getState().equals(Thread.State.WAITING)))
    	  renewThreadStarter.interrupt();
      
      isFinished = true;
      if(DEBUG && logger.isDebugEnabled()) { 
          if(heartBeatStarter != null)
          	logger.debug("heartBeatStarter state:" + heartBeatStarter.getState());
      }
      
      if(heartBeatStarter != null && !heartBeatStarter.getState().equals(Thread.State.TERMINATED))
    	  heartBeatStarter.join();
      
   // Its important to kill all JVMs that we started ... 
      processStartLock.acquire();
      try{
      	if(kill_signal == false){
      		for(int i=0 ; i<processes ; i++) 
      			p[i].destroy();
      	}
      }
      catch(Exception e){
    	  e.printStackTrace();
      }
      processStartLock.signal();
      
      
      
      if(isRestarting == false){

	      MPJProcessPrintStream.stop();
	
	      
	
	      try {
	        if(DEBUG && logger.isDebugEnabled()) { 
	          logger.debug ("Checking whether peerChannel is closed or what ?" +
	                    peerChannel.isOpen());
		}
	        
	        while(peerChannel.isConnected()){
	        	/*
	        	if(DEBUG && logger.isDebugEnabled()) { 
	                logger.debug ("channel connected");
	        	}
	        	*/
	        }
	        
	        if (peerChannel.isOpen()) {
	            if(DEBUG && logger.isDebugEnabled()) { 
	              logger.debug ("Closing it ..."+peerChannel );
	  	  }
	            //peerChannel.close();
	          }
	        	
	        peerChannel.close();
	
	        if(DEBUG && logger.isDebugEnabled()) { 
	          logger.debug("Was already closed, or i closed it");
		}
	      }
	      catch (Exception e) { 
	        e.printStackTrace() ; 
	        //continue;
	      }
	      
	      restoreVariables() ; 
      }// if isRestarting == false
      else{
    	  restoreVariables() ; 
    	  finishLock.signal();
      }

      

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug("\n\n ** .. execution ends .. ** \n\n");
      }

    } //end while(loop)
  }

  private void sendRestartReqestToMainHost() {
	  
	
	  if(DEBUG && logger.isDebugEnabled()) { 
		  logger.debug("--sendRestartReqestToMainHost--"); 
      }
	  
	  synchronized (sendRestartRequestLock) {
		if(hasSendRequest == false){
			if(DEBUG && logger.isDebugEnabled()) { 
				  logger.debug("has not send the restart request, and send it"); 
		    }
			hasSendRequest = true;
			ByteBuffer msgBuffer = ByteBuffer.allocate(4);
			msgBuffer.putInt(REQUEST_RESTART);
			msgBuffer.flip();
			while(msgBuffer.hasRemaining()){
				try{
					if(peerChannel.write(msgBuffer) == -1)
						throw new ClosedChannelException();
				}
				catch(IOException ioe){
					ioe.printStackTrace();
					System.out.println("You should ensure the MPJRun host is running!");
					if (DEBUG && logger.isDebugEnabled()) {
			              logger.debug("MPJRun host sockect close, exit heartbeat thread!");
			        }
					break;
				}
			}
		}
	}
	  
	  
	
}

private void restoreVariables() {
	hasSendRequest = false;
	isRestartFromCheckpoint = false;
    jvmArgs.clear();
    appArgs.clear(); 
    wdir = null ; 
    applicationClassPathEntry = null;
    deviceName = null;
    className = null ;
    processes = 0;
    p = null ; 
  }

   private void waitToStartExecution () {
	   synchronized(startLock){
		   
		   if (waitToStartExecution) {
			   if (DEBUG && logger.isDebugEnabled()) {
		              logger.debug("--go to wait for startLock--");
		       }
		      try {
		        startLock.wait();
		      }
		      catch (Exception e) {
		        e.printStackTrace();
		      }
		    } 

		    waitToStartExecution = true ; 
	   }    

  }

  static boolean matchMe(String line) throws Exception { 

    if (!line.contains("@") || line.startsWith("#")) {
      return false;
    }

    StringTokenizer token = new StringTokenizer(line, "@");
    String hostName = token.nextToken();
    InetAddress host=null, myHost=null;

    try {
      host = InetAddress.getByName(hostName);
      myHost = InetAddress.getLocalHost() ; 
    } catch (Exception e) {
      return false;
    }

    if(host.getHostName().equals(myHost.getHostName()) || 
       host.getHostAddress().equals(myHost.getHostAddress())) {
      return true;
    }

    return false;
  }

  static boolean matchMeOld(String line) throws Exception { 

    if (!line.contains("@") || line.startsWith("#")) {
      return false;
    }

    StringTokenizer token = new StringTokenizer(line, "@");
    String hostName = token.nextToken();
    InetAddress host = null;

    try {
      host = InetAddress.getByName(hostName);
    } catch (Exception e) {
      return false;
    }

    Enumeration<NetworkInterface> cards = 
                               NetworkInterface.getNetworkInterfaces();
               

    while (cards.hasMoreElements()) {
      NetworkInterface card = cards.nextElement();
      Enumeration<InetAddress> addresses = card.getInetAddresses();

      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();
        if(host.getHostAddress().equals(address.getHostAddress())) {
          return true;
        }
      }
    }

    return false;
  }

  private void startExecution () {
	  synchronized (startLock) {
		  waitToStartExecution = false;
		  startLock.notify();
	  }
	  

    
  }
  
  private void createLogger(String homeDir, String hostName) 
                                              throws MPJRuntimeException {
  
    if(logger == null) {

      DailyRollingFileAppender fileAppender = null ;

      try {
        fileAppender = new DailyRollingFileAppender(
                            new PatternLayout(
                            " %-5p %c %x - %m\n" ),
                            homeDir+"/logs/daemon-"+hostName+".log",
                            "yyyy-MM-dd-a" );

        Logger rootLogger = Logger.getRootLogger() ;
        rootLogger.addAppender( fileAppender);
        LoggerRepository rep =  rootLogger.getLoggerRepository() ;
        rootLogger.setLevel ((Level) Level.ALL );
        logger = Logger.getLogger( "mpjdaemon" );
      }
      catch(Exception e) {
        throw new MPJRuntimeException(e) ;
      }
    }
  }



  private void serverSocketInit() {
    ServerSocketChannel serverChannel;
    ServerSocketChannel processServerChannel;
    try {
      selector = Selector.open();
      serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("Binding the serverSocketChannel @" + D_SER_PORT);
      }
      serverChannel.socket().bind(new InetSocketAddress(D_SER_PORT));
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
      
      
      processServerChannel = ServerSocketChannel.open();
      processServerChannel.configureBlocking(false);
      if(DEBUG && logger.isDebugEnabled()) { 
          logger.debug ("Binding the writableServerChannel @" + (D_SER_PORT+1));
      }
      processServerChannel.socket().bind(new InetSocketAddress(D_SER_PORT+1));
      processServerChannel.register(selector, SelectionKey.OP_ACCEPT);
      

    }
    catch (Exception cce) {
      cce.printStackTrace();
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("Exception in serverSocketInit()" + cce.getMessage());
      }
      System.exit(0);
    }
  }

  private void doAccept(SelectableChannel keyChannel) {
    if(DEBUG && logger.isDebugEnabled()) { 
      logger.debug ("---doAccept---");
    }

    try {
      peerChannel = ( (ServerSocketChannel) keyChannel).accept();
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("peerChannel " + peerChannel);
      }
    }
    catch (IOException ioe) {
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("IOException in doAccept");
      }
      System.exit(0);
    }

    try {
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("configuring the channel to be non-blocking");
      }
      peerChannel.configureBlocking(false);
    }
    catch (IOException ioe) {
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("IOException in doAccept");
      }
      System.exit(0);
    }

    try {
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("Registering for OP_READ & OP_WRITE event");
      }
      peerChannel.register(selector,
                           SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
    catch (ClosedChannelException cce) {
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("Closed Channel Exception in doAccept");
      }
      System.exit(0);
    }

    try {
      peerChannel.socket().setTcpNoDelay(true);
    }
    catch (Exception e) {}
  }
  
  /* called from the selector thread, and accept the connections */
  boolean doAccept(SelectableChannel keyChannel,
                   Vector channelCollection) 
	                                             throws Exception {
    SocketChannel peerChannel = null;

    synchronized (channelCollection) {

      if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
        logger.debug("---doAccept---");
      }

      if(keyChannel.isOpen()) { 
        peerChannel = ( (ServerSocketChannel) keyChannel).accept();
      }
      else { 
        return false; 
      }

      if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
        logger.debug("Added channel " + peerChannel);
      }
      channelCollection.add(peerChannel);
      if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
        logger.debug("Now the size is <" + channelCollection.size() + ">");
      }

      peerChannel.configureBlocking(false);
      peerChannel.register(selector,
                          SelectionKey.OP_READ | SelectionKey.OP_WRITE);
  


      peerChannel.socket().setTcpNoDelay(true);

         peerChannel.socket().setSendBufferSize(524288);
         peerChannel.socket().setReceiveBufferSize(524288);

      if (channelCollection.size() == processes) {
        channelCollection.notify();
        if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
          logger.debug(" notifying and returning true");
        }
        return true;
      }

    } //end sync.

    if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
      logger.debug("--doAccept ends--");
    }
    peerChannel = null;
    if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
      logger.debug(" returning false");
    }
    return false;
  }

  Runnable selectorThread = new Runnable() {


	/* This is selector thread */
    public void run() {

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("selector Thread started ");
      }

      Set readyKeys = null;

      Iterator readyItor = null;

      SelectionKey key = null;

      SelectableChannel keyChannel = null;

      /* why are these required here? */
      SocketChannel socketChannel = null;
      ByteBuffer lilBuffer = ByteBuffer.allocateDirect(8);
      ByteBuffer lilBuffer2 = ByteBuffer.allocateDirect(4);
      ByteBuffer buffer = ByteBuffer.allocateDirect(1000);
      byte[] lilArray = new byte[4];

      try {
        while (selector.select() > -1) {

          readyKeys = selector.selectedKeys();
          readyItor = readyKeys.iterator();

          while (readyItor.hasNext()) {

            key = (SelectionKey) readyItor.next();
            readyItor.remove();
            keyChannel = (SelectableChannel) key.channel();
            if(DEBUG && logger.isDebugEnabled()) { 
              logger.debug ("\n---selector EVENT---");
	    }

            if (key.isAcceptable() && selectorAcceptConnect) {
            	ServerSocketChannel sChannel =(ServerSocketChannel) keyChannel;
            	
            	
            	if (sChannel.socket().getLocalPort() == D_SER_PORT) {
                    if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
                      logger.debug("selector calling doAccept (host-channel) ");
                    }
                    doAccept(keyChannel);
                }
                else{
                    if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
                      logger.debug("selector calling doAccept (process-channel) ");
                    }
                    
                    //if it should be initialed 
                	if(initializing == false){
                		
                		finishLock.acquire();
                		finishLock.signal();
                		
                		if (DEBUG && logger.isDebugEnabled()) {
    			                logger.debug("---CLEAR TABLES---");
    			              }
    	            	  
    	            	  for(int i = 0; i < processChannels.size(); i++){
    	            		  if(processChannels.get(i).isOpen())
    	            			  processChannels.get(i).close();
    	            	  }
    	            	  processChannels.clear();     		           		  
    	            	  worldProcessTable.clear();
    	            	  processValidMap.clear();
    	            	  processFinishMap.clear();
    	            	  initializing = true;
              		  
    	            	  heartBeatBeginLock.acquire();
    	            	  renewThreadStarter = new Thread(renewThread);
    	            	  renewThreadStarter.start();
    	            	  
    	            	  if(heartBeatStarter == null || heartBeatStarter.getState().equals(Thread.State.TERMINATED)){
	    	            	  isFinished = false;
	    	            	  heartBeatStarter = new Thread(heartBeatThread);
	    	            	  heartBeatStarter.start();
    	            	  }
                	}
                    
                    doAccept(keyChannel, tempProcessChannels);
                }
                    
              
            }
            else if (key.isConnectable()) {

              /*
               * why would this method be called?
               * At the daemon, this event is not generated ..
               */

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
              //doConnect(socketChannel);
            }

            else if (key.isReadable()) {

              if(DEBUG && logger.isDebugEnabled()) { 
                logger.debug ("READ_EVENT");
	      }
              finishLock.acquire();
              finishLock.signal();
	      
              socketChannel = (SocketChannel) keyChannel;
	      
              int readInt = -1 ; 
              lilBuffer.clear();
              
	      
              if(DEBUG && logger.isDebugEnabled()) { 
                logger.debug("lilBuffer "+ lilBuffer);         
	      }

	        // .. this line is generating exception which kills the 
		//    daemon ... I think we need a try catch here and if
		//    any exception is generated, then we will have to 
		//    goto back to selector.select() method ..
		//
		//    this is Windows 2000 specific error ..i have not 
		//    seen this error on Windows XP ..
	      
	      try { 
		      
                if((readInt = socketChannel.read(lilBuffer)) == -1) {

                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug(" The daemon has received an End of "+
	  	  		  "Stream signal") ; 
	    	    logger.debug(" checking if this channel is still open");
		  }
		  
		  if(socketChannel.isOpen()) {
                    if(DEBUG && logger.isDebugEnabled()) { 
                      logger.debug("closing the channel");
		    }
		    socketChannel.close() ; 			  
		  }

                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug("continuing to select()");
		  }
		  continue ; 
                  //END_OF_STREAM signal .... 

                }
	      }
	      catch(Exception innerException) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Exception in selector thread, message is"+
				  innerException.getMessage() );
                  logger.debug (" continuing to select() method ..."); 
		}
		continue; 
	      }
		      

                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("READ_EVENT (read)" + readInt);
		}

              lilBuffer.flip();
              lilBuffer.get(lilArray, 0, 4);
              String read = new String(lilArray);
              if(DEBUG && logger.isDebugEnabled()) { 
                logger.debug ("READ_EVENT (String)<" + read + ">");
	      }
              
              //receive process info, map the uuid to the worldProcessTable
              if(read.equals("pro-")){
            	  doBarrierRead( ( (SocketChannel) keyChannel),
                          worldProcessTable, false, false);
              }
              
            //receive reconnect process info, map the uuid to the worldProcessTable
              if(read.equals("rcn-")){
            	  doBarrierRead( ( (SocketChannel) keyChannel),
                          worldProcessTable, false, true);
              }
              
            //receive process exit, send exit ack back to the channel
              if(read.equals("exit")){
            	  doSendBackExitAck( (SocketChannel) keyChannel);
              }
              
              //receive process checkpoint, send check cehckpoint ack back to the channel
              if(read.equals("che-")){
            	  doSendBackCheckpointAck((SocketChannel) keyChannel);
              }
              
              //receive restart from a certain checkpoint command 
              if(read.equals("rst-")){
            	  isRestartFromCheckpoint = true;
              }
              
              //receive start checkpoint wave from MPJRun host 
              if(read.equals("scpv")){
            	  int rank = lilBuffer.getInt();
            	  doStartCheckpointWave(rank);
              }
              
              
              //just a heartheat check
              if(read.equals("cvl-")){
            	  if(DEBUG && logger.isDebugEnabled()) { 
                      logger.debug ("heartbeat check from main host.");
            	  }            	  
              }
              
              

              if (read.equals("cpe-")) {
 	        if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("cpe-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("App CP Entry Length -->" + length);
		}
                lilBuffer.clear();
                buffer.limit(length);
                socketChannel.read(buffer);
                byte[] byteArray = new byte[length];
                buffer.flip();
                buffer.get(byteArray, 0, length);
                applicationClassPathEntry = new String(byteArray);
                //change later
                //applicationClassPathEntry = System.getProperty("user.dir");
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("applicationClassPathEntry:<"+ 
                                       applicationClassPathEntry+">");
		}
		
                buffer.clear();
              }
	      
              if (read.equals("cls-")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("cls-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("className length -->" + length);
		}
                lilBuffer.clear();
                buffer.limit(length);
                socketChannel.read(buffer);
                byte[] byteArray = new byte[length];
                buffer.flip();
                buffer.get(byteArray, 0, length);
                className = new String(byteArray);
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("className :<" + className + ">");
		}
                buffer.clear();
              }

              if (read.equals("cfn-")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("cfn-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("className length -->" + length);
		}
                lilBuffer.clear();
                buffer.limit(length);
                socketChannel.read(buffer);
                byte[] byteArray = new byte[length];
                buffer.flip();
                buffer.get(byteArray, 0, length);
                configFileName = new String(byteArray);
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("configFileName :<"+ 
                                           configFileName + ">");
		}
                buffer.clear();
              }

              if (read.equals("app-")) {
		      
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("app-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Application args Length -->" + length);
		}
                lilBuffer.clear();

		for(int j=0 ; j<length ; j++) {
                  lilBuffer2.position(0); lilBuffer2.limit(4); 			
                  socketChannel.read(lilBuffer2);
		  lilBuffer2.flip();
                  int argLen = lilBuffer2.getInt();
                  buffer.limit(argLen);
                  socketChannel.read(buffer);
                  byte[] t = new byte[argLen];
                  buffer.flip();
                  buffer.get(t,0,argLen);
		  appArgs.add(new String(t)); 
		  buffer.clear(); 
		  lilBuffer2.clear();
		}
                 
		//for loop to create a new array ...
                buffer.clear();
		
              }
              else if (read.equals("num-")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("num-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("should be 4, isit ? -->" + length);
		}
                lilBuffer.clear();
                socketChannel.read(lilBuffer2);
                lilBuffer2.flip();
                processes = lilBuffer2.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Num of processes ==>" + processes);
		}
                lilBuffer2.clear();
              }
              
              else if (read.equals("nps-")) {
                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug ("nps-");
  		}
                  nprocs = lilBuffer.getInt();
                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug ("nprocs -->" + nprocs);
  		}
                 
                }


              else if (read.equals("arg-")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("arg-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("argu len -->"+length);
		}
                lilBuffer.clear();

		for(int j=0 ; j<length ; j++) {
                  lilBuffer2.position(0); lilBuffer2.limit(4); 			
                  socketChannel.read(lilBuffer2);
		  lilBuffer2.flip();
                  int argLen = lilBuffer2.getInt();
                  buffer.limit(argLen);
                  socketChannel.read(buffer);
                  byte[] t = new byte[argLen];
                  buffer.flip();
                  buffer.get(t,0,argLen);
		  jvmArgs.add(new String(t)); 
		  buffer.clear(); 
		  lilBuffer2.clear();
		}
                 
		//for loop to create a new array ...
                buffer.clear();
		
              }

              else if (read.equals("dev-")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("dev-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("dev-Length -->" + length);
		}
                lilBuffer.clear();
                buffer.limit(length);
                socketChannel.read(buffer);
                byte[] byteArray = new byte[length];
                buffer.flip();
                buffer.get(byteArray, 0, length);
                deviceName = new String(byteArray);
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Device Name :<" + deviceName + ">");
		}
                buffer.clear();
              }

              else if (read.equals("wdr-")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("wdr-");
		}
                int length = lilBuffer.getInt();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("wdr-Length -->" + length);
		}
                lilBuffer.clear();
                buffer.limit(length);
                socketChannel.read(buffer);
                byte[] byteArray = new byte[length];
                buffer.flip();
                buffer.get(byteArray, 0, length);
                wdir = new String(byteArray);
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("wdir :<"+wdir+">");
		}
                buffer.clear();
              }

              else if (read.equals("*GO*")) {
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("GO");
		}
                lilBuffer.clear();
                startExecution ();

              } 
              else if (read.equals("kill")) {
            	  
            	  processStartLock.acquire();
            	  finishLock.acquire();
            	  isRestarting = true;
            	  synchronized (startLock) {
            		//unnomal terminate 
					startLock.notify();
            	  }
            	  if(renewThreadStarter != null && (renewThreadStarter.getState() == Thread.State.BLOCKED
            			  || renewThreadStarter.getState() == Thread.State.WAITING))
            		  renewThreadStarter.interrupt();
            	  
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("processing kill event");
		}
                //MPJProcessPrintStream.stop();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Stopping the output In kill event");
		}

                try {
                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug ("peerChannel is closed or what ?" +
                                peerChannel.isOpen());
		  }

                  if (peerChannel.isOpen()) {
                    if(DEBUG && logger.isDebugEnabled()) { 
                      logger.debug ("Closing it ...");
		    }
                    //peerChannel.close();
                  }
                }
                catch (Exception e) {}

                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug ("Killling the process");
		  }
                try {
                  synchronized (MPJDaemon.this) {
                    if (p != null) {
                      synchronized (p) {

                        for(int i=0 ; i<processes ; i++) 
                         p[i].destroy() ;                         
                      }
                    }
                  }
                }
                catch (Exception e) {e.printStackTrace(); } 
//no matter what happens, we cant let this thread
//die, coz otherwise, the daemon will die as well..
//maybe you wanne stop the output handler threads as well.

                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Killed it");
		}
                buffer.clear();
                lilBuffer.clear();
                
                kill_signal = true;
                processStartLock.signal();

              }

            } //end if key.isReadable()

            else if (key.isWritable()) {

              if(DEBUG && logger.isDebugEnabled()) { 
                logger.debug(
                    "In, WRITABLE, so changing the interestOps to READ_ONLY");
	      }
              key.interestOps(SelectionKey.OP_READ);

            }

          } //end while iterator
        } //end while
      }
      catch (Exception ioe1) {
        if(DEBUG && logger.isDebugEnabled()) { 
          logger.debug("Exception in selector thread " + ioe1.getMessage());
	}
        ioe1.printStackTrace();
        //System.exit(0);
      }
    } //end run()

	
  }; //end selectorThread which is an inner class 
  
  
  Runnable renewThread = new Runnable() {
		
		@Override
		public void run() {
			//wait all to finish
			try {
				initLock.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				initializing = false;
				heartBeatBeginLock.signal();
				return;
			}
			
			if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("renew thread start");
          }
				
			 synchronized (tempProcessChannels) {

		      if (tempProcessChannels.size() != processes) {
		        try {
		        	tempProcessChannels.wait();
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
		        	initializing = false;
		        	initLock.signal();		
		        	heartBeatBeginLock.signal();
		        	return;
		          
		        }
		      }

		    } //end sync.
			 for(int i=0;i < processChannels.size();i++){
				try {
					processChannels.get(i).close();
				} catch (IOException e) {
					e.printStackTrace();
					initializing = false;
		        	initLock.signal();
		        	heartBeatBeginLock.signal();
		        	return;
				}
			 }
			 
			 processChannels.clear();
			 processChannels = tempProcessChannels;
		    
			 if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("processChannels renewed, processes:" + processes);
	            }

		   
		    
			tempProcessChannels =  new Vector<SocketChannel> ();
		        
		    /*
		     * At this point, all-to-all connectivity has been acheived.
		     * renew the  worldWritableTable and worldReadableTable
		     */
		    
		    /*
		     * non checkpoint server node send the rank, msb, lsb to the checkpoint server
		     *  Do blocking-reads, record the every pair of <uuid, writableChannel>
		     *  the pair of <uuid, readableChannel> left for the selecotr thread to record
		     */
		    
		    /* worldTable is accessed from doBarrierRead or here, so their access
		     * should be synchronized */
		    synchronized (worldProcessTable) {
		      if ( (worldProcessTable.size() != processes) || processValidMap.containsValue(false)) {
		        try {
		        	worldProcessTable.wait();
		        }
		        catch (Exception e) {
		          e.printStackTrace();
		          initializing = false;
		          initLock.signal();	
		          heartBeatBeginLock.signal();
		          return;
		        }
		      }
		    } //end sync
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("worldProcessTable renewed, processes:" + processes);
	            }
		    
		    
		    initializing = false;
		    
		    initLock.signal();
		    heartBeatBeginLock.signal();
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("initLock release");
	            }
		  
		}
	};// end renew thread
	
	private void doStartCheckpointWave(int rank) {
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("---do start checkpoint wave---");
        }
		
		UUID ruid = pids[rank];
		if(ruid != null){
			SocketChannel socketChannel = worldProcessTable.get(ruid);
			if(socketChannel != null){
				ByteBuffer buf = ByteBuffer.allocate(4);
				buf.putInt(START_CHECKPOINT_WAVE);
				buf.flip();
				while(buf.hasRemaining()){
					try{
						if(socketChannel.write(buf) == -1)
							throw new ClosedChannelException();
					}
					catch(IOException e){
						e.printStackTrace();
						return;
					}
				}
			}
		}		
	}
	
	/*
	   * This method is used during initialization.
	   */
	  void doBarrierRead(SocketChannel socketChannel, Hashtable table, boolean
				
          ignoreFirstFourBytes, boolean isReconnect) throws Exception {
		  
		  if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("---do barrier read---");
          }
		  
	    long lsb, msb;
	    int read = 0, tempRead = 0, rank;
	    UUID ruid = null;
	    ByteBuffer barrBuffer = ByteBuffer.allocate(24); //changeallocate

	    if (ignoreFirstFourBytes) {
	      barrBuffer.limit(24);
	    }
	    else {
	      barrBuffer.limit(20);
	    }

	    while (barrBuffer.hasRemaining()) {
	      try {
	        if (socketChannel.read(barrBuffer) == -1) {
	          throw new Exception(new ClosedChannelException());
	        }
	      }
	      catch (ClosedChannelException e) {
	        return;
	      }
	    }

	    barrBuffer.flip();
	    //barrBuffer.position(0);

	    if (ignoreFirstFourBytes) {
	      barrBuffer.getInt();
	    }

	    rank = barrBuffer.getInt();
	    if (DEBUG && logger.isDebugEnabled()) {
          logger.debug("receive rank:" + rank);
        }
	    msb = barrBuffer.getLong();
	    lsb = barrBuffer.getLong();
	    barrBuffer.clear();
	    ruid = new UUID(msb, lsb);
	    pids[rank] = ruid; //, rank);
   
	    
	    
	    
	    synchronized (table) {
	    	
	    	if(isReconnect == true){
		    	checkpointingProcessTable.remove(ruid);
		    }
	    	
	    	
	      table.put(ruid, socketChannel);	      
	      processValidMap.put(ruid, true);
	      
	      if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("added rand" + rank + " to table:" + table);
            logger.debug("table size:" + table.size());
            logger.debug("processValidMap size:" + processValidMap.size());
        }

	      if ( (table.size() == processes ) && (!processValidMap.containsValue(false))) {
	        try {
	        	//notify the renew thread and the main thread for the complete of the table init
	          table.notifyAll();
	          if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("notify table");
	          }
	        }
	        catch (Exception e) {
	          throw new Exception(e);
	        }
	      }

	    }

	  }//end of do barrier read
	  
	  private void doSendBackExitAck(SocketChannel socketChannel) throws IOException {
		  	if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("---do SendBackExitAck---");
	        }
		  	
		  	//while in the channel and table initial period, can't send ack
		  	try {
				initLock.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("acquire initLock");
	        }
			
			long lsb, msb;
			ByteBuffer uuidBuffer = ByteBuffer.allocate(16);
			while(uuidBuffer.hasRemaining()){
				try{
					if(socketChannel.read(uuidBuffer) == -1)
						throw new ClosedChannelException();
				}
				catch(IOException e){
					e.printStackTrace();
					if (DEBUG && logger.isDebugEnabled()) {
			            logger.debug("read channel close, return");
			        }
					initLock.signal();
					return;
				}
				
			}
			
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("finish reading uuidBuffer:");
	        }
			
			uuidBuffer.flip();
			msb = uuidBuffer.getLong();
			lsb = uuidBuffer.getLong();
			UUID ruid = new UUID(msb, lsb);
		  	
			synchronized(worldProcessTable){
				worldProcessTable.remove(ruid);
				processValidMap.put(ruid, false);
				checkpointingProcessTable.remove(ruid);
				processFinishMap.put(ruid, true);
			}
			
			ByteBuffer ackBuffer = ByteBuffer.allocate(4);
			ackBuffer.putInt(DAEMON_EXIT_ACK);
			
			
			ackBuffer.flip();
			while(ackBuffer.hasRemaining()){
				try{
					if(socketChannel.write(ackBuffer) == -1)
						throw new ClosedChannelException();
				}
				catch(IOException e){
					e.printStackTrace();
					if (DEBUG && logger.isDebugEnabled()) {
			            logger.debug("write channel close, return");
			        }
					initLock.signal();
					return;
				}
			}
			
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("finish write back exit ack!");
	        }
			
			initLock.signal();
			
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("---finish do SendBackExitAck---");
	        }
			
			
		}
	  
	  private void doSendBackCheckpointAck(SocketChannel socketChannel) {
		  if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("---do SendBackCheckpointAck---");
	        }
		  	
		//while in the channel and table initial period, can't send ack
		  	try {
				initLock.acquire();
				heartBeatLock.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			
			long lsb, msb, versionNum;
			ByteBuffer uuidBuffer = ByteBuffer.allocate(20);
			while(uuidBuffer.hasRemaining()){
				try{
					if(socketChannel.read(uuidBuffer) == -1)
						throw new ClosedChannelException();
				}
				catch(IOException e){
					e.printStackTrace();
					if (DEBUG && logger.isDebugEnabled()) {
			            logger.debug("read channel close, return");
			        }
					initLock.signal();
					heartBeatLock.signal();
					return;
				}
				
			}
			
			uuidBuffer.flip();
			msb = uuidBuffer.getLong();
			lsb = uuidBuffer.getLong();
			versionNum = uuidBuffer.getInt();
			UUID ruid = new UUID(msb, lsb);
		  	
			
			ByteBuffer ackBuffer = ByteBuffer.allocate(4);
			ackBuffer.putInt(DAEMON_MARKER_ACK);
			
			ackBuffer.flip();
			while(ackBuffer.hasRemaining()){
				try{
					if(socketChannel.write(ackBuffer) == -1)
						throw new ClosedChannelException();
				}
				catch(IOException e){
					e.printStackTrace();
					if (DEBUG && logger.isDebugEnabled()) {
			            logger.debug("write channel close, return");
			        }
					initLock.signal();
					heartBeatLock.signal();
					return;
				}
			}
			
			checkpointingProcessTable.put(ruid, 0);
			worldProcessTable.remove(ruid);
			processValidMap.put(ruid, false);
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("processValidMap size:" + processValidMap.size());
	        }
			initLock.signal();
			heartBeatLock.signal();
			
		}

  public static void main(String args[]) {
    try {
      MPJDaemon dae = new MPJDaemon(args);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  //"-Xloggc:" + hostName + ".gc",
  //"-XX:+PrintGCDetails",
  //"-XX:+PrintGCTimeStamps",
  //"-XX:+PrintGCApplicationConcurrentTime",
  //"-XX:+PrintGCApplicationStoppedTime",
  //"-Xnoclassgc",
  //"-XX:MinHeapFreeRatio=5",
  //"-XX:MaxHeapFreeRatio=5",
  //"-Xms512M", "-Xmx512M",
  //"-DSIZE=1000", "-DITERATIONS=100",
  //"-Xdebug",
  //"-Xrunjdwp:transport=dt_socket,address=11000,server=y,suspend=n",
  
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
	  };
	  
	  Runnable heartBeatThread = new Runnable() {
		
		@Override
		public void run() {
			if (DEBUG && logger.isDebugEnabled()) {
				logger.debug("\n---Heartbeat Thread Start---");
	        }
			
			try {
				heartBeatBeginLock.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			heartBeatBeginLock.signal();
			
			
			
			while(!isFinished){
				try {
					heartBeatLock.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
					//if the lock is interrupted then exit the thread
					return;
				}
				
				ByteBuffer buf = ByteBuffer.allocate(4);
				buf.putInt(CHECK_VALID);
				
				synchronized (worldProcessTable) {
					Iterator it = worldProcessTable.entrySet().iterator();
					SocketChannel socketChannel = null;
					
					if (DEBUG && logger.isDebugEnabled()) {
			              logger.debug("Heartbeat Thread");
			              logger.debug("worldProcessTable size:" + worldProcessTable.size());
			              
			        }
					while(it.hasNext()){
						java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
						UUID ruid = (UUID)entry.getKey();
						if (DEBUG && logger.isDebugEnabled()) {
							logger.debug("processValidMap.get(uuid) " + processValidMap.get(ruid));				              
				        }
						
				    	if(processValidMap.get(ruid) == false)
				    		continue;					    	
				    	
				    	buf.flip();
				    	socketChannel = (SocketChannel)entry.getValue();
				    	
				    	try {
							if(socketChannel.write(buf) == -1){
								throw new ClosedChannelException();
							}
						} catch (IOException e) {
							if (DEBUG && logger.isDebugEnabled()) {
					              logger.debug("Socket Channel:" + socketChannel + " is closed, so notify the main host");
					        }
							
							synchronized (sendRestartRequestLock) {
								if(hasSendRequest == false){
									if(DEBUG && logger.isDebugEnabled()) { 
										  logger.debug("has not send the restart request, and send it"); 
								    }
									hasSendRequest = true;
									ByteBuffer msgBuffer = ByteBuffer.allocate(4);
									msgBuffer.putInt(REQUEST_RESTART);
									msgBuffer.flip();
									while(msgBuffer.hasRemaining()){
										try{
											if(peerChannel.write(msgBuffer) == -1)
												throw new ClosedChannelException();
										}
										catch(IOException ioe){
											ioe.printStackTrace();
											System.out.println("You should ensure the MPJRun host is running!");
											if (DEBUG && logger.isDebugEnabled()) {
									              logger.debug("MPJRun host sockect close, exit heartbeat thread!");
									        }
											
											heartBeatLock.signal();									
											return;
										}
									}
								}
							}
							
							isRestarting = true;
							checkpointingProcessTable.clear();
							heartBeatLock.signal();
							if (DEBUG && logger.isDebugEnabled()) {
					              logger.debug("after notify the MPJRun, exit heartbeat thread!");
					        }
							return;
							
						}
				    	
					}//end worldProcessTable iterator
					
					it = checkpointingProcessTable.entrySet().iterator();
					while(it.hasNext()){
						java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
						UUID ruid = (UUID)entry.getKey();
						if(worldProcessTable.get(ruid) == null){
							int t = (Integer) entry.getValue();
							t++;
							if(t == MAX_CHECKPOINT_INVALID_TIME){
								
								if (DEBUG && logger.isDebugEnabled()) {
						              logger.debug("Socket Channel:" + socketChannel + " is closed, so notify the main host");
						        }
								
								synchronized (sendRestartRequestLock) {
									if(hasSendRequest == false){
										if(DEBUG && logger.isDebugEnabled()) { 
											  logger.debug("has not send the restart request, and send it"); 
									    }
										hasSendRequest = true;
								
										ByteBuffer msgBuffer = ByteBuffer.allocate(4);
										msgBuffer.putInt(REQUEST_RESTART);
										msgBuffer.flip();
										while(msgBuffer.hasRemaining()){
											try{
												if(peerChannel.write(msgBuffer) == -1)
													throw new ClosedChannelException();
											}
											catch(IOException ioe){
												ioe.printStackTrace();
												System.out.println("You should ensure the MPJRun host is running!");
												if (DEBUG && logger.isDebugEnabled()) {
										              logger.debug("MPJRun host sockect close, exit heartbeat thread!");
										        }
												
												heartBeatLock.signal();
												return;
											}
										}
									}
								}
								
								isRestarting = true;
								checkpointingProcessTable.clear();
								heartBeatLock.signal();
								if (DEBUG && logger.isDebugEnabled()) {
						              logger.debug("after notify the MPJRun, exit heartbeat thread!");
						        }
								return;
								
								
							}
							else{
								checkpointingProcessTable.put(ruid, t);
							}
								
						}
					}//end checkpointingProcessTable iterator
					
				}//end syn
				
				
				
				

				heartBeatLock.signal();
				try {
					Thread.currentThread().sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			
			}//end while isFinished
			
			
			if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("exit heartbeat thread");
	        }
		}//end run
	};
	
	public static void copyFile(File sourceFile,File targetFile) 
	throws IOException{
        // buffer the input stream
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff=new BufferedInputStream(input);
 
        //buffer the output stream
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff=new BufferedOutputStream(output);
        
        //buffer array
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len =inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        
        outBuff.flush();
        
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    } 
}

class OutputHandler extends Thread { 
	String rank;
	Process p = null ; 

  public OutputHandler(Process p, String rank) { 
    this.p = p; 
    this.rank = rank;
  } 

  public void run() {

    InputStream outp = p.getInputStream() ;
    String line = "";
    BufferedReader reader = new BufferedReader(new InputStreamReader(outp));
       
    if(MPJDaemon.DEBUG && MPJDaemon.logger.isDebugEnabled()) { 
      MPJDaemon.logger.debug( "outputting ...");
    }

    try {
      do {
        if (!line.equals("")) {
          line.trim(); 
 
          synchronized (this) {
            System.out.println("@Rank<" + this.rank + ">: " + line);
            //if(DEBUG && logger.isDebugEnabled()) { 
            //  logger.debug(line);
	    //}
          } 
        }
      }  while ( (line = reader.readLine()) != null); 
        // && !kill_signal); 
    }
    catch (Exception e) {
      if(MPJDaemon.DEBUG && MPJDaemon.logger.isDebugEnabled()) { 
        MPJDaemon.logger.debug ("outputHandler =>" + e.getMessage());
      }
      e.printStackTrace();
    } 
  } //end run.
} 
