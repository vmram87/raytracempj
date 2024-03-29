/*The MIT License

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
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

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
  private int D_SER_PORT = getPortFromWrapper();
  private boolean loop = true;
  private Selector selector = null;
  private volatile boolean selectorAcceptConnect = true;
  private volatile boolean kill_signal = false;
  private volatile boolean wait = true;
  private volatile boolean waitToStartExecution = true;
  private PrintStream out = null;
  private Semaphore outputHandlerSem = new Semaphore(1,true); 
  static final boolean DEBUG = false ;
  
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
  private String SYSTEM_LIB_DIR = "user-folder/System_Lib";
  private String BLCR_LIB_DIR = "/home/jqchen/local/lib";
  String configFileName = null ;
  private long HEARTBEAT_INTERVAL = 5000;
  private String[] processIds = null;
  
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
  Hashtable<Integer, String> rankProcessIdTable = new Hashtable<Integer, String> ();

  
  private boolean initializing = false;
  private boolean isFinished = false;
  private boolean isRestarting = false;
  private boolean isExit = false;
  private CustomSemaphore initLock = new CustomSemaphore(1); 
  private CustomSemaphore finishLock = new CustomSemaphore(1); 
  private CustomSemaphore heartBeatLock = new CustomSemaphore(1); 
  private CustomSemaphore heartBeatBeginLock = new CustomSemaphore(1); 
  private CustomSemaphore startLock = new CustomSemaphore(1); 
  private CustomSemaphore processStartLock = new CustomSemaphore(1); 
  private Object sendRequestLock = new Object(); 
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
  private final int CHECKPOINT_WAVE_ACK = -32;  
  
  private final int DAEMON_STATUS = -50;
  private final int DAEMON_STATUS_RUNNING = -51;
  private final int DAEMON_STATUS_CHECKPOINTING = -52;
  private final int DAEMON_STATUS_RESTARTING = -53;
  
  private int daemonStatus = DAEMON_STATUS_RUNNING;
  private static String machineName = null;
  
  private int MAX_CHECKPOINT_INVALID_TIME = 12;
  private boolean isRestartFromCheckpoint = false;
  private boolean hasSendRequest = false;
  protected boolean hasAcquireFinishLock = false;  
  private boolean hasReceiveStartCheckpointWave = false;
  private int cpVersionNum = -1;
  private int cpRank = 0;
private boolean initWait = true;

  private static String JAVA_TEMP_FILE_DIRECTORY = "/tmp/hsperfdata_" + System.getProperty("user.name") + "/";
  

  public MPJDaemon(String args[]) throws Exception {
	  
    InetAddress localaddr = InetAddress.getLocalHost();
    String hostName = localaddr.getHostName();
    
    Map<String,String> map = System.getenv() ;
    mpjHomeDir = map.get("MPJ_HOME");
			    
    createLogger(mpjHomeDir, hostName); 

    if(DEBUG && logger.isDebugEnabled()) { 
    	logger.debug("DEBUG "+DEBUG); 
      logger.debug("mpjHomeDir "+mpjHomeDir); 
    }

    if (args.length == 1) {
	    
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug (" args[0] " + args[0]);
        logger.debug ("setting daemon port to" + args[0]);
      }

      D_SER_PORT = new Integer(args[0]).intValue();

    }

    serverSocketInit();
    Thread selectorThreadStarter = new Thread(selectorThread);
    
    if(DEBUG && logger.isDebugEnabled()) { 
      logger.debug ("Starting the selector thread ");
    }

    selectorThreadStarter.start();

    while (loop) {
    	
    	startLock = new CustomSemaphore(1);
    	processStartLock = new CustomSemaphore(1);
    	
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

      
      //configFile.createNewFile();

      try {
    	  File configFile = new File(configFileName) ; 
    	  in = new FileInputStream(configFile);
    	  bufferedReader = new BufferedReader(new InputStreamReader(in));
      }
      catch (Exception e) {
        //e.printStackTrace();
        isExit = true;
      }

     

      OutputHandler [] outputThreads = new OutputHandler[processes] ;  
      p = new Process[processes];  
      processIds = new String[processes];
      pids = new UUID[nprocs];
      
      processStartLock.acquire();
      if(isExit == false && kill_signal == false){
    	  kill_signal = false;
    	  isRestarting = false;
	      try{
	    	  
		      for (int j = 0; j < processes; j++) {
		
		        /* Step 1: Read from the config file - basically need to know
		                   rank of processes */ 
		        String line = null;
		        String rank = null; 
		        
		        if(isRestartFromCheckpoint == false){
		        	if(j == 0){
		        		jvmArgs.add("-Djava.library.path=."+File.pathSeparator + BLCR_LIB_DIR +
		        				File.pathSeparator+ mpjHomeDir + File.separator + SYSTEM_LIB_DIR);
		        	}
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
		        	if(j == 0){
				        boolean now = false;
				        boolean noSwitch = true ;
				
				        for(int e=0 ; e<jArgs.length; e++) {
				
				          if(DEBUG && logger.isDebugEnabled()) { 
				            logger.debug("jArgs["+e+"]="+jArgs[e]);
					  }
				
				          if(now) {
				            String cp = jvmArgs.remove(e);
					      
				            cp = "."+
				            	  File.pathSeparator+mpjHomeDir+"/lib/loader1.jar"+
				            	  File.pathSeparator+mpjHomeDir+"/lib/loader2.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/log4j-1.2.11.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/mpj.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/mpiExp.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/mpi.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/mpjbuf.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/mpjdev.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/xdev.jar"+
				                  File.pathSeparator+mpjHomeDir+"/lib/smpdev.jar"+				                  
				                  File.pathSeparator+mpjHomeDir+"/lib/wrapper.jar"+
				                  File.pathSeparator+applicationClassPathEntry+
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
					  jvmArgs.add("."+
							  File.pathSeparator+mpjHomeDir+"/lib/loader1.jar"+
			            	  File.pathSeparator+mpjHomeDir+"/lib/loader2.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/log4j-1.2.11.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/mpj.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/mpiExp.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/mpi.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/mpjbuf.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/mpjdev.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/xdev.jar"+
			                  File.pathSeparator+mpjHomeDir+"/lib/smpdev.jar"+				                  
			                  File.pathSeparator+mpjHomeDir+"/lib/wrapper.jar"+
			                  File.pathSeparator+applicationClassPathEntry) ; 
				        }
				
				        jArgs = jvmArgs.toArray(new String[0]);
			        
		        	}// end of if j > 0
		        
		        
			       
			 
			        for(int e=0 ; e<jArgs.length; e++) {
			          if(DEBUG && logger.isDebugEnabled()) { 
			            logger.debug("modified: jArgs["+e+"]="+jArgs[e]);
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
		        	rank= pathArg.split("_")[2];
		        	rankProcessIdTable.put(Integer.parseInt(rank), processId);
		        	
		        			        	
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
			
		
		        //avoid the problem that after kill the process, 
		        //it needs time to complete release the resource.
		       // if(j==0)
		        	//Thread.currentThread().sleep(3000);
		        
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
	      
	      try { 
	          bufferedReader.close() ; 
	          in.close() ; 
	        } catch(Exception e) { 
	          e.printStackTrace() ; 
	        } 
	        
	  	  //when init, and worldprocessTable is not init properly, it's should be init to be 0 in selector thread
	        Thread.currentThread().sleep(1000);
	       
      }//end of it isExit == false
      
      processStartLock.signal(); 
      
      
      synchronized (worldProcessTable) {
      	  if(DEBUG && logger.isDebugEnabled()) { 
                logger.debug("worldProcessTable.size(): " +worldProcessTable.size()); 
            }
      	  if(initWait  == true && worldProcessTable.size() != processes){
      		  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug("wait for worldProcessTable, time:" + new Timestamp(System.currentTimeMillis())); 
                }
      		  worldProcessTable.wait(1000 * nprocs * 5);
      		  if(DEBUG && logger.isDebugEnabled()) { 
      			  	logger.debug("Time:" + new Timestamp(System.currentTimeMillis()));
                    logger.debug("After wait or notify worldProcessTable.size(): " +worldProcessTable.size()); 
                }
      		  //processStartLock.acquire();
      		  if(worldProcessTable.size() != processes && kill_signal == false){      			
      			  isFinished = true;
      			  sendRestartReqestToMainHost();
      		  }
      		  //processStartLock.signal();
      	  }
        }
		   
      
      if(DEBUG && logger.isDebugEnabled()) { 
          logger.debug("wait for process to end" +worldProcessTable.size()); 
      }
		
      //Wait for the I/O threads to finish. They finish when 
      // their corresponding JVMs finish. 
      for (int j = 0; j < processes; j++) {
    	  if(outputThreads[j] != null)
    		  outputThreads[j].join();
      }
	      
	      
	      
      
      
      //if process finish before the heartbeat thread, then check the 
      //processFinishmap to see whether they are normal finish or not, 
      if(isExit == false && isRestarting == false && processFinishMap.size() != processes){
    	  isRestarting = true;
    	  //need to be fix latter
    	  sendRestartReqestToMainHost();
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
      		if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("Normal end. Before JVM destroy. Time: " + new Timestamp(System.currentTimeMillis()));
      		}
      		for(int i=0 ; i<processes ; i++) 
      			p[i].destroy();
      	}
      }
      catch(Exception e){
    	  e.printStackTrace();
      }
      kill_signal = true;
      
      processStartLock.signal();
      
      
      if(isRestarting == false){

    	  if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("have before daemon exit to MPJRun Time: " + new Timestamp(System.currentTimeMillis()));
	      }
    	  
	      MPJProcessPrintStream.stop();
	
	      if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("have sent daemon exit to MPJRun Time: " + new Timestamp(System.currentTimeMillis()));
	      }
	      
	
	      try {
	        if(DEBUG && logger.isDebugEnabled()) { 
	          logger.debug ("Checking whether peerChannel is closed or what ?" +
	                    peerChannel.isOpen());
		}
	        
	      //wait for peerChannel is closed, because above have sent exit to MPJRun, The connection is closed by the MPJRun.
	        while(peerChannel.isConnected()){
	        	/*
	        	if(DEBUG && logger.isDebugEnabled()) { 
	                logger.debug ("channel connected");
	        	}
	        	*/
	        }
	        
	        //should be closed, if not close it for safe.
	        if (peerChannel.isOpen()) {
	            if(DEBUG && logger.isDebugEnabled()) { 
	              logger.debug ("Closing it ..."+peerChannel );
	  	  }
	            peerChannel.close();
	          }	        
	        
	        
	
	        if(DEBUG && logger.isDebugEnabled()) { 
	          logger.debug("Was already closed, or i closed it. Time: " + new Timestamp(System.currentTimeMillis()));
		}
	      }
	      catch (Exception e) { 
	        e.printStackTrace() ; 
	        //continue;
	      }
	      
      }// if isRestarting == false
      else{
    	  
      }

      restoreVariables() ;      

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug("\n\n ** .. execution ends .. ** \n\n");
      }
      
      
      processStartLock.acquire();
      kill_signal = false;
      if(hasAcquireFinishLock == true){
    	  finishLock.signal();
    	  hasAcquireFinishLock = false;
    	  if(DEBUG && logger.isDebugEnabled()) { 
              logger.debug ("Release finishLock");
    	  }
      }
      
      if(isExit == true){
    	  isExit = false;
      }
      
      processStartLock.signal();

    } //end while(loop)
  }

  private void sendRestartReqestToMainHost() {
	  
	
	  if(DEBUG && logger.isDebugEnabled()) { 
		  logger.debug("--sendRestartReqestToMainHost--"); 
      }
	  
	  synchronized (peerChannel) {
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
			
			if(DEBUG && logger.isDebugEnabled()) { 
				  logger.debug("Finish send request restart to MPJRun"); 
		    }
		}
	}
	  
	  
	
}

private void restoreVariables() {
	hasSendRequest = false;
	isRestartFromCheckpoint = false;
	initWait = true;
	rankProcessIdTable.clear();
    jvmArgs.clear();
    appArgs.clear(); 
    wdir = null ; 
    applicationClassPathEntry = null;
    deviceName = null;
    className = null ;
    processes = 0;
    worldProcessTable.clear();
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
    	machineName = hostName;
    	MPJProcessPrintStream.setMachineName(machineName);
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

      if (DEBUG && logger.isDebugEnabled()) {
        logger.debug("---doAccept---");
      }

      if(keyChannel.isOpen()) { 
        peerChannel = ( (ServerSocketChannel) keyChannel).accept();
      }
      else { 
        return false; 
      }

      if (DEBUG && logger.isDebugEnabled()) {
        logger.debug("Added channel " + peerChannel);
      }
      channelCollection.add(peerChannel);
      if (DEBUG && logger.isDebugEnabled()) {
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
        if (DEBUG && logger.isDebugEnabled()) {
          logger.debug("notifying and returning true");
        }
        return true;
      }

    } //end sync.

    if (DEBUG && logger.isDebugEnabled()) {
      logger.debug("--doAccept ends--");
    }
    peerChannel = null;
    if (DEBUG && logger.isDebugEnabled()) {
      logger.debug("returning false when the processChannels are still all complete");
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
        	  try{

            key = (SelectionKey) readyItor.next();
            readyItor.remove();
            keyChannel = (SelectableChannel) key.channel();
            if(DEBUG && logger.isDebugEnabled()) { 
              logger.debug ("\n---selector EVENT---");
	    }

            if (key.isAcceptable() && selectorAcceptConnect) {
            	ServerSocketChannel sChannel =(ServerSocketChannel) keyChannel;
            	
            	
            	if (sChannel.socket().getLocalPort() == D_SER_PORT) {
                    if (DEBUG && logger.isDebugEnabled()) {
                      logger.debug("selector calling doAccept (host-channel) ");
                    }
                    doAccept(keyChannel);
                }
                else{
                    if (DEBUG && logger.isDebugEnabled()) {
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
                    
                    doAccept(keyChannel, processChannels);
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
              
              //receive process checkpoint, send checkpoint ack back to the channel
              if(read.equals("che-")){
            	  daemonStatus = DAEMON_STATUS_CHECKPOINTING;
            	  doSendBackCheckpointAck((SocketChannel) keyChannel);
              }
              
              //receive restart from a certain checkpoint command 
              if(read.equals("rst-")){
            	  daemonStatus = DAEMON_STATUS_RESTARTING;
            	  isRestartFromCheckpoint = true;
              }
              
              //receive start checkpoint wave from MPJRun host 
              if(read.equals("scpv")){
            	  int rank = lilBuffer.getInt();
            	  doStartCheckpointWave((SocketChannel) keyChannel,rank);
              }
              
              
              //just a heartheat check
              if(read.equals("cvl-")){
            	  if(DEBUG && logger.isDebugEnabled()) { 
                      logger.debug ("heartbeat check from main host.");
            	  }    
            	  
            	  doResponseCheckValid((SocketChannel) keyChannel);
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
            	  synchronized(worldProcessTable){
            		  initWait = false;
            		  worldProcessTable.notifyAll();
            	  }
            	  if(DEBUG && logger.isDebugEnabled()) { 
                      logger.debug ("before acquire processStartLock");
            	  } 
            	  try{
            		  processStartLock.acquire();
            	  }catch (InterruptedException e1){
            		  e1.printStackTrace();
            	  }            	  
            	  
          	  
            	  lilBuffer.get(lilArray, 0, 4);
                  String object = new String(lilArray);
                  if(object.equals("rest")){             	  
                	  if(DEBUG && logger.isDebugEnabled()) { 
                          logger.debug ("Receive killrest");
                	  } 
                	  
                	  if(logger.isDebugEnabled()) { 
                          logger.debug ("Receive kill restart, time:" + new Timestamp(System.currentTimeMillis()));
                	  }
                	  
                	  if(kill_signal == true){
                		  if(DEBUG && logger.isDebugEnabled()) { 
                              logger.debug ("kill_signal == true, so continue to select");
                    	  }
                		  processStartLock.signal();
                		  continue;
                	  }
                	  
            		  try{                			  
            			  finishLock.acquire();
            			  hasAcquireFinishLock  = true;
            			  if(DEBUG && logger.isDebugEnabled()) { 
                              logger.debug ("Acquire finishLock");
                    	  }
            		  }
            		  catch(InterruptedException e2){
            			  e2.printStackTrace();
            		  }
                	  
                	  isRestarting = true;
                	  isExit = false;
                	  
                  }
                  else{
                	  if(DEBUG && logger.isDebugEnabled()) { 
                          logger.debug ("Receive killexit");
                	  }
                	  isRestarting = false;
                	  isExit = true;
                  }
                  
                  
                  hasReceiveStartCheckpointWave = false;
                  
            	  
            	  checkpointingProcessTable.clear();
            	  
            	  //continue to execute if the startLock is waiting
            	  synchronized (startLock){
            		  startLock.notify();
            	  }
            	  
            	  if(renewThreadStarter != null && (renewThreadStarter.getState() == Thread.State.BLOCKED
            			  || renewThreadStarter.getState() == Thread.State.WAITING))
            		  renewThreadStarter.interrupt();
            	  
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("processing kill event");
		}

                  if(DEBUG && logger.isDebugEnabled()) { 
                    logger.debug ("Killing the process");
		  }
                try {
                    if (kill_signal == false && p != null && p.length > 0) {              	
                        for(int i=0 ; i<processes ; i++) {
                        	if(p[i] != null)
                        		p[i].destroy() ;                         
                        }
                        
                      //wait 3s and then forcely kill 
                       //Thread.currentThread().sleep(3000);
                       Iterator it = rankProcessIdTable.entrySet().iterator();
                       System.out.println("Forecefully Kill: " + rankProcessIdTable);
						while(it.hasNext()){
							Entry entry = (Entry)it.next();
							String pId = (String)entry.getValue();
							Runtime.getRuntime().exec("kill -9 " + pId);
						}
						//Thread.currentThread().sleep(2000);
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
            
            
          	}
            catch(Exception e){
            	e.printStackTrace();
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
  
  
  private void doResponseCheckValid(SocketChannel socketChannel) {
	  	if(machineName == null){
	  		return;
	  	}
		ByteBuffer buf = ByteBuffer.allocate(100);
		buf.putInt(DAEMON_STATUS);
		buf.putInt(machineName.getBytes().length);
		buf.put(machineName.getBytes());
		buf.putInt(daemonStatus);
		
		buf.flip();
		
		synchronized (peerChannel) {
			while(buf.hasRemaining()){
				try{
					if(peerChannel.write(buf) == -1)
						throw new ClosedChannelException();
				}
				catch(Exception e){
					e.printStackTrace();
					return;
				}
			}
		}
		
		
	}
  
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
				
			 synchronized (processChannels) {

		      if (processChannels.size() != processes) {
		        try {
		        	processChannels.wait();
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
			 
		    
			 if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("processChannels renewed, processes:" + processes);
	            }
		        
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
		    
		    if (logger.isDebugEnabled()) {
	              logger.debug("After initialization: time:" + new Timestamp(System.currentTimeMillis()));
	        }
		  
		}
	};// end renew thread
	
	
	private void doStartCheckpointWave(SocketChannel mainHostChannel, int rank) throws Exception {
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("---do start checkpoint wave---");
        }
		
		ByteBuffer verBuffer = ByteBuffer.allocate(4);
		while (verBuffer.hasRemaining()) {
	      try {
	        if (mainHostChannel.read(verBuffer) == -1) {
	          throw new ClosedChannelException();
	        }
	      }
	      catch(Exception e){
	    	  e.printStackTrace();
	    	  return;
	      }
	    }
		
		verBuffer.position(0);
		cpVersionNum = verBuffer.getInt();
		cpRank  = rank;
		synchronized (worldProcessTable) {
			if(worldProcessTable.size() == processes)
			{
				UUID ruid = pids[cpRank];
				SocketChannel socketChannel = worldProcessTable.get(ruid);
				ByteBuffer buf = ByteBuffer.allocate(8);
				buf.putInt(START_CHECKPOINT_WAVE);
				buf.putInt(cpVersionNum);
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
				
				if (DEBUG && logger.isDebugEnabled()) {
		            logger.debug("have sent start checkpoint wave to rank " +  rank);
		        }
			}
			else{
				hasReceiveStartCheckpointWave = true;
			}
			
			
			ByteBuffer ackBuf = ByteBuffer.allocate(100);
			ackBuf.putInt(CHECKPOINT_WAVE_ACK);
			ackBuf.putInt(machineName.getBytes().length);
			ackBuf.put(machineName.getBytes());
			
			ackBuf.flip();
			
			synchronized (peerChannel) {
				while(ackBuf.hasRemaining()){
					try{
						if(peerChannel.write(ackBuf) == -1)
							throw new ClosedChannelException();
					}
					catch(Exception e){
						e.printStackTrace();
						return;
					}
				}
			}
			
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("have sent startcheckpoint ACK to MPJRun");
	        }
					
				
				
			
		}//end of  synchronized (worldProcessTable) 
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
	    int pId = 20000;
	    UUID ruid = null;
	    ByteBuffer barrBuffer = ByteBuffer.allocate(28); //changeallocate

	    if (ignoreFirstFourBytes) {
	      barrBuffer.limit(28);
	    }
	    else {
	      barrBuffer.limit(24);
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
	    pId = barrBuffer.getInt();
	    barrBuffer.clear();
	    ruid = new UUID(msb, lsb);
	    pids[rank] = ruid; //, rank);
	    
   
	    rankProcessIdTable.put(rank, "" + pId);
	    
	    
	    
	    synchronized (table) {
	    	
	    	if(isReconnect == true){
		    	checkpointingProcessTable.remove(ruid);
		    }
	    	
	    	
	      table.put(ruid, socketChannel);	      
	      processValidMap.put(ruid, true);
	      
	      if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("added rand <" + rank + "> to table:" + table);
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
	          
	          if(hasReceiveStartCheckpointWave == true){
	        	  	UUID id = pids[cpRank];
					SocketChannel channel = worldProcessTable.get(id);
					ByteBuffer buf = ByteBuffer.allocate(8);
					buf.putInt(START_CHECKPOINT_WAVE);
					buf.putInt(cpVersionNum);
					buf.flip();
					while(buf.hasRemaining()){
						try{
							if(channel.write(buf) == -1)
								throw new ClosedChannelException();
						}
						catch(IOException e){
							e.printStackTrace();
							return;
						}
					}
					
					if (DEBUG && logger.isDebugEnabled()) {
			            logger.debug("have sent start checkpoint wave to rank " +  cpRank);
			        }
					
					hasReceiveStartCheckpointWave = false;
	          }
	          
	          //reconnect is finish so set the status to running
	          daemonStatus = DAEMON_STATUS_RUNNING;
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
				return;
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
				//note needed here
				//checkpointingProcessTable.remove(ruid);
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
//		  	try {
//				initLock.acquire();
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//				if (DEBUG && logger.isDebugEnabled()) {
//		            logger.debug("Interrupted whne acquire initLock, return");
//		        }
//				return;
//			}
			
			//we should seperated acquire the lock.
//			try{
//				heartBeatLock.acquire();
//			}
//			catch (InterruptedException e2){
//				e2.printStackTrace();
//				if (DEBUG && logger.isDebugEnabled()) {
//		            logger.debug("Interrupted when acqure heartBeatLock, return");
//		        }
//				return;
//			}
//			if (DEBUG && logger.isDebugEnabled()) {
//	            logger.debug("have acquire heartbeatLock");
//	        }
			
			synchronized(worldProcessTable){
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
						//heartBeatLock.signal();
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
						//heartBeatLock.signal();
						return;
					}
				}
				
				checkpointingProcessTable.put(ruid, 0);
				worldProcessTable.remove(ruid);
				processValidMap.put(ruid, false);
				if (DEBUG && logger.isDebugEnabled()) {
		            logger.debug("processValidMap size:" + processValidMap.size());
		        }
				//heartBeatLock.signal();
				
			}//end of syn
			
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
			
			MAX_CHECKPOINT_INVALID_TIME = nprocs ;
			
			while(!isFinished){
				try {
					heartBeatLock.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					//if the lock is interrupted then exit the thread
					return;
				}
				try {
					processStartLock.acquire();
				} catch (InterruptedException e2) {
					heartBeatLock.signal();
					e2.printStackTrace();
					//if the lock is interrupted then exit the thread
					return;
				}
				
				if(kill_signal == false){
				
				
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
								
								synchronized (peerChannel) {
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
										              logger.debug("MPJRun host sockect close, this should not happen!");
										        }
												
												//heartBeatLock.signal();		
												//processStartLock.signal();
												break;
											}
										} // end of while
									} //end of if hasSendRequest == false
								}// end of syn sendRestartRequestLock
								
								checkpointingProcessTable.clear();
								heartBeatLock.signal();
								processStartLock.signal();
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
							              logger.debug("Excced the max time of try, Checkpointing process [" + ruid
							            		  + "]is closed, so notify the main host");
							        }
									
									synchronized (peerChannel) {
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
													
													//heartBeatLock.signal();
													//processStartLock.signal();
													break;
												}
											}// end while
										}//end if hasSendRequest == false
									}// end syn sendRestartRequestLock
									
									checkpointingProcessTable.clear();
									heartBeatLock.signal();
									processStartLock.signal();
									if (DEBUG && logger.isDebugEnabled()) {
							              logger.debug("after notify the MPJRun, exit heartbeat thread!");
							        }
									return;
									
									
								}// end if t == MAX_CHECKPOINT_INVALID_TIME
								else{
									checkpointingProcessTable.put(ruid, t);
								}
									
							}// end if worldProcessTable.get(ruid) == null
						}//end checkpointingProcessTable iterator
						
					}//end syn worldProcessTable
					
					
				}//end of if kill_signal == false
					
	
				heartBeatLock.signal();
				processStartLock.signal();
				try {
					Thread.currentThread().sleep(HEARTBEAT_INTERVAL);
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
            if(line.startsWith("@@@Exit@@@")){
            	System.out.println("Exit True!");
            	break;
            }
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
      //e.printStackTrace();
    } 
    
    //System.out.println("@Rank<" + this.rank + ">: OutputThread Ends Time: " + new Timestamp(System.currentTimeMillis()));
  } //end run.
} 
