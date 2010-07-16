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
  private String cmd = null;
  private Process p[] = null ; 
  static Logger logger = null ; 
  private String mpjHomeDir = null ;  
  String configFileName = null ;
  
  //Vector<SocketChannel> writableChannels = null;
  //Vector<SocketChannel> tempWritableChannels = new Vector<SocketChannel> ();

  //Vector<SocketChannel> readableChannels = null;
  //Vector<SocketChannel> tempReadableChannels = new Vector<SocketChannel> ();
  Vector<SocketChannel> processChannels = new Vector<SocketChannel> ();
  Vector<SocketChannel> tempProcessChannels = new Vector<SocketChannel> ();
  
  Hashtable<UUID, SocketChannel> worldProcessTable =
		new Hashtable<UUID, SocketChannel> ();
  
  Hashtable<UUID, Boolean> processValidMap = new Hashtable<UUID, Boolean> ();

  
  private boolean initializing = false;
  private boolean isFinished = false;
  private CustomSemaphore initLock = new CustomSemaphore(1); 
  private CustomSemaphore heartBeatLock = new CustomSemaphore(1); 
  private Thread renewThreadStarter = null;
  UUID[] pids = null;
  
  public static final int LONG_MESSAGE = -45;
  public static final int DAEMON_EXIT = -46;
  public static final int INT_MESSAGE = -47;
  private final int DAEMON_MARKER_ACK = -35;
  private final int DAEMON_EXIT_ACK = -36;
  private final int REQUEST_RESTART = -70;
  private final int CHECK_VALID = -71;
  

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
    (new Thread(heartBeatThread)).start();
    
    int exit = 0;

    while (loop) {

    	if(DEBUG && logger.isDebugEnabled()) { 
    	      logger.debug ("CLEAR TABLE");
    	}
    	worldProcessTable.clear();
    	processValidMap.clear();
    	
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("MPJDaemon is waiting to accept connections ... ");
      }
      
      wdir = System.getProperty("user.dir");
      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug("wdir "+wdir);
      }
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
      pids = new UUID[processes];

      for (int j = 0; j < processes; j++) {

        /* Step 1: Read from the config file - basically need to know
                   rank of processes */ 
        String line = null;
        String rank = null; 
        
        jvmArgs.add("-Djava.library.path=."+File.pathSeparator+"/usr/local/lib"+
        		File.pathSeparator+"/root/C++Workspace/blcr/Debug");

        while((line = bufferedReader.readLine()) != null) {

          if(DEBUG && logger.isDebugEnabled()) { 
            logger.debug ("line ="+line);
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
        boolean now = false;
        boolean noSwitch = true ;

        for(int e=0 ; e<jArgs.length; e++) {

          if(DEBUG && logger.isDebugEnabled()) { 
            logger.debug("jArgs["+e+"]="+jArgs[e]);
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
            logger.debug(i+": "+ ex[i]);
          }  
        }
	
        /* Step 3: Now start a new JVM */ 
        ProcessBuilder pb = new ProcessBuilder(ex);
        pb.directory(new File(wdir)) ;
        pb.redirectErrorStream(true); 

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

      try { 
        bufferedReader.close() ; 
        in.close() ; 
      } catch(Exception e) { 
        e.printStackTrace() ; 
      } 
      
	  //wait for the init of the writable and readable channels
      //renewThreadStarter.join();
      

      //Wait for the I/O threads to finish. They finish when 
      // their corresponding JVMs finish. 
      for (int j = 0; j < processes; j++) {
        outputThreads[j].join();
      }

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug ("Stopping the output");
      }

      MPJProcessPrintStream.stop();

      // Its important to kill all JVMs that we started ... 
      synchronized (p) {
        for(int i=0 ; i<processes ; i++) 
          p[i].destroy();
        kill_signal = false;
      }

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

      if(DEBUG && logger.isDebugEnabled()) { 
        logger.debug("\n\n ** .. execution ends .. ** \n\n");
      }

    } //end while(loop)
  }

  private void restoreVariables() {
    jvmArgs.clear();
    appArgs.clear(); 
    wdir = null ; 
    applicationClassPathEntry = null;
    deviceName = null;
    className = null ;
    processes = 0;
    p = null ; 
  }

   private synchronized void waitToStartExecution () {

    while (waitToStartExecution) {
      try {
        this.wait();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    } 

    waitToStartExecution = true ; 

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

  private synchronized void startExecution () {
    waitToStartExecution = false;
    this.notify();
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
            	
            	//if it should be initialed 
            	if(initializing == false){
            		if (DEBUG && logger.isDebugEnabled()) {
			                logger.debug("---CLEAR TABLES---");
			              }
	            	  
	            	  for(int i = 0; i < processChannels.size(); i++){
	            		  if(processChannels.get(i).isOpen())
	            			  processChannels.get(i).close();
	            	  }
	            	  processChannels.clear();
          		  
          		  
	            	  worldProcessTable.clear();
	            	  initializing = true;
          		  
	            	  (new Thread(renewThread)).start();
            	}
            	
            	if (sChannel.socket().getLocalPort() == D_SER_PORT) {
                    if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
                      logger.debug("selector calling doAccept (data-channel) ");
                    }
                    doAccept(keyChannel);
                }
                else{
                    if (mpi.MPI.DEBUG && logger.isDebugEnabled()) {
                      logger.debug("selector calling doAccept (ctrl-channel) ");
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
                          worldProcessTable, false);
              }
              
            //receive process exit, send exit ack back to the channel
              if(read.equals("exit")){
            	  doSendBackExitAck( (SocketChannel) keyChannel);
              }
              
              //receive process checkpoint, send check cehckpoint ack back to the channel
              if(read.equals("che-")){
            	  doSendBackCheckpointAck((SocketChannel) keyChannel);
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
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("processing kill event");
		}
                MPJProcessPrintStream.stop();
                if(DEBUG && logger.isDebugEnabled()) { 
                  logger.debug ("Stopping the output");
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
                    peerChannel.close();
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

                        kill_signal = true;
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
		        }
		      }

		    } //end sync.
			 for(int i=0;i < processChannels.size();i++){
				try {
					processChannels.get(i).close();
				} catch (IOException e) {
					e.printStackTrace();
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
		        }
		      }
		    } //end sync
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("worldProcessTable renewed, processes:" + processes);
	            }
		    
		    
		    initializing = false;
		    
		    initLock.signal();
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("initLock release");
	            }
		  
		}
	};// end renew thread
	
	/*
	   * This method is used during initialization.
	   */
	  void doBarrierRead(SocketChannel socketChannel, Hashtable table, boolean
				
          ignoreFirstFourBytes) throws Exception {
		  
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
	      table.put(ruid, socketChannel);	      
	      processValidMap.put(ruid, true);
	      
	      if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("added rand" + rank + " to table:" + table);
            logger.debug("table size:" + table.size());
            logger.debug("processValidMap size:" + processValidMap.size());
        }

	      if ( (table.size() == processes ) && (!processValidMap.containsValue(false))) {
	        try {
	          table.notify();
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
			
			processValidMap.put(ruid, false);
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
			} catch (InterruptedException e1) {
				e1.printStackTrace();
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
					return;
				}
			}
			
			processValidMap.put(ruid, false);
			if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("processValidMap size:" + processValidMap.size());
	        }
			initLock.signal();
			
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
				
				
				Iterator it = worldProcessTable.entrySet().iterator();
				SocketChannel socketChannel = null;
				while(it.hasNext()){
					java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
					UUID ruid = (UUID)entry.getKey();
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
				              logger.debug("Socket Channel:" + socketChannel + " is closed, so notify the main host, exit heartbeat thread!");
				        }
						
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
						
						heartBeatLock.signal();
						if (DEBUG && logger.isDebugEnabled()) {
				              logger.debug("after notify the MPJRun, exit heartbeat thread!");
				        }
						return;
						
					}
			    	
				}//end iterator

			
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
