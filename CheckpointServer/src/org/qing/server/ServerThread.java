package org.qing.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerRepository;
import org.qing.dao.ContextDao;
import org.qing.factory.ContextFactory;
import org.qing.object.Context;








public class ServerThread {
	private Thread serverThread = null;
	private String mpjHomeDir = null;
	private Selector selector = null;
	private int psl = 0, nprocs = 0, server_rank = 0, size = 0, my_server_port = 0;
	private static final int SHUTDOWN_SIGNAL = -13;
	private static final int END_OF_STREAM = -14;
	private static String server_host = null;
	private boolean isCheckpointing = false;
	private int versionNum = 0;
	private boolean initializing = false;
	private CustomSemaphore initLock = new CustomSemaphore(0); 
	
	String localHostName = null;
	InetAddress localaddr = null;
	private String[] args = null;
	UUID[] pids = null;
	
	 /* Server Socket Channel */
	  ServerSocketChannel writableServerChannel = null;
	  ServerSocketChannel readableServerChannel = null;
	  ServerSocketChannel controlServerChannel = null;
	  
	  
	  
	/*
	   * This integer is used as the header to send initial control messages
	   */
	  private final int INIT_MSG_HEADER_DATA_CHANNEL = -21;
	
	  private final int INIT_MSG_HEADER_CTRL_CHANNEL = -20;
	  
	  private final int CHECKPOINT_RECONNECT = -40;
	  
	  private final int MARKER_ACK = -41;
	  
	  public static final int END_APP = -48;
	  
	  private final int NUM_OF_PROCCESSES = -42;
	
	  private final int RENDEZ_CTRL_MSG_LENGTH = 4;
	
	  private final int ACK_LENGTH = 17;
	
	  private final int CTRL_MSG_LENGTH = 45;
	  
	  int SEND_OVERHEAD = CTRL_MSG_LENGTH + 4 ;
	
	  int RECV_OVERHEAD = 0; 
	
	  private final int STD_COMM_MODE = 3;
	
	  private final int SYNC_COMM_MODE = 2;
	
	  private final boolean NO_ACK_RECEIVED = false;
	
	  private final boolean REQ_NOT_COMPLETED = false;
	
	  private final boolean RECV_POSTED = true;
	
	  private final int READY_TO_SEND = -24;
	
	  private static final int ACK_HEADER = -23;
	
	  private final int RENDEZ_HEADER = -22;
	  
	  private final int START_CHECKPOINT = -32;
	  
	  private final int FINISH_CHECKPOINT = -33;
	  
	  private final int EXIT_PROCESS = -34;
	  
	  private final int CPSERVER_EXIT_ACK = -37;
	
	  private final int SEND_ACK_TO_SENDER = -80;
	
	  private final int RECV_IN_USER_MEMORY = -81;
	
	  private final int RECV_IN_DEV_MEMORY = -82;
	
	  private final int MORE_TO_WRITE = -83;
	
	  private final int MORE_TO_READ = -84;
	  
	  private final int REQUEST_RESTART = -70;
	  
	
	Vector<SocketChannel> writableChannels = new Vector<SocketChannel> ();
	Vector<SocketChannel> tempWritableChannels = new Vector<SocketChannel> ();

	Vector<SocketChannel> readableChannels = new Vector<SocketChannel> ();
	Vector<SocketChannel> tempReadableChannels = new Vector<SocketChannel> ();


	Hashtable<UUID, SocketChannel> worldWritableTable =
		new Hashtable<UUID, SocketChannel> ();

	Hashtable<UUID, SocketChannel> worldReadableTable =
		new Hashtable<UUID, SocketChannel> ();

	Hashtable<UUID, CustomSemaphore> writeLockTable =
		new Hashtable<UUID, CustomSemaphore> ();
	
	SocketChannel controlChannel = null;
	
	
	static Logger logger = null ;
	public static final boolean DEBUG = true; 
	public static String CONTEXT_DIR_NAME = ".context" ;
	
	public ServerThread(){
		
	}
	
	public boolean init(String[] args) throws Exception{
		/*
		if (args.length != 2) {

	      throw new Exception("Usage: " +
	        "java ServerApp rank <conf_file> <device_name>"+
	           "conf_file can be, ../conf/xdev.conf <Local>"+
	           "OR http://holly.dsg.port.ac.uk:15000/xdev.conf <Remote>");
	    }	
*/		
		
		  DailyRollingFileAppender fileAppender = null ;  	  
	      Map<String,String> map = System.getenv() ;
	      mpjHomeDir = map.get("MPJ_HOME");
	      
	      if(logger == null && DEBUG ) {
	        try {
	          fileAppender = new DailyRollingFileAppender( 
				  new PatternLayout(
					  " %-5p %c %x - %m\n" ),
				  mpjHomeDir+"/logs/cpServer.log", 
				  "yyyy-MM-dd-HH" );
		  
		  Logger rootLogger = Logger.getRootLogger() ;
		  //rootLogger.addAppender( fileAppender);
		 // LoggerRepository rep =  rootLogger.getLoggerRepository() ;
		  //rootLogger.setLevel ((Level) Level.ALL );
		  //rep.setThreshold((Level) Level.OFF ) ;
		  logger = Logger.getLogger( "cpServer" );  
		  logger.addAppender(fileAppender);
		  logger.setLevel(Level.ALL);
	        }
	        catch(Exception e) {
	          throw new Exception(e) ;
	        }
	      }

	    
	    try {

	        localaddr = InetAddress.getLocalHost();
	        localHostName = localaddr.getHostName();

	      }
	      catch (UnknownHostException unkhe) {
	        throw new Exception(unkhe);
	      }

	      String[] argv={"0","mpj.conf","niodev"};
	      this.args = argv;
	      
	      if(DEBUG && logger.isDebugEnabled())  {
	          logger.debug("socket Init") ;
	        }
	      socketInit();
		
		return true;
	}
	
	
	/*
	   * reand the configure file
	   * init the socket channel 
	   * reinit the socket after checkpoint
	   * 
	   */
	  public void socketInit() throws Exception{
		    
		    my_server_port = getCheckpointPort();

		    /* Open the selector */
		    try {
		      selector = Selector.open();
		    }
		    catch (IOException ioe) {
		      throw new Exception(ioe);
		    }
		    
		    
		    /* Checking for the java.net.BindException. This
		     * Exception is thrown when the port on which
		     * we want to bind is already in use */
		    boolean isOK = false; 
		    boolean isError = false ;
		    
		    

		    while(isOK != true) { 

		      isOK = false ; 
		      isError = false;

		      try {
		    	  	
		    	  	writableServerChannel = ServerSocketChannel.open();
			        writableServerChannel.configureBlocking(false);
			        writableServerChannel.socket().bind(new InetSocketAddress(my_server_port));
			        writableServerChannel.register(selector, SelectionKey.OP_ACCEPT);
			        
			        if(DEBUG && logger.isDebugEnabled())  {
				          logger.debug("Init writableServerChannel at port " + my_server_port) ;
				    }

			        readableServerChannel = ServerSocketChannel.open();
			        readableServerChannel.configureBlocking(false);
			        readableServerChannel.socket().bind(
			            new InetSocketAddress( (my_server_port + 1)));
			        readableServerChannel.register(selector, SelectionKey.OP_ACCEPT);
			        
			        if(DEBUG && logger.isDebugEnabled())  {
				          logger.debug("Init readableServerChannel at port " + (my_server_port+1)) ;
				    }
			        
			        controlServerChannel = ServerSocketChannel.open();
			        controlServerChannel.configureBlocking(false);
			        controlServerChannel.socket().bind(
			            new InetSocketAddress( (my_server_port + 2)));
			        controlServerChannel.register(selector, SelectionKey.OP_ACCEPT);
			        
			        if(DEBUG && logger.isDebugEnabled())  {
				          logger.debug("Init controlServerChannel at port " + (my_server_port+2)) ;
				    }
			        

		      }
		      catch (IOException ioe) {
		        isError = true;
		        try { Thread.sleep(500); } catch(Exception e){ e.printStackTrace();}
		      }
		      finally {
		        if(isError == true)
		          isOK = false;
		        else if(isError == false)
		          isOK = true;
		      }
		    }

		    serverThread = new Thread(selectorThread);

		    serverThread.start();

		    //(new Thread(renewThread)).start();
		    
		    
	  }//end of socket init

	public void waitToEnd() {
		// TODO Auto-generated method stub
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	  /*
	   * Static anonymous inner class that is
	   * basically the selector thread
	   */
	  Runnable selectorThread = new Runnable() {
		  
		

		/* This is selector thread */
	    public void run() {
	    	if(DEBUG && logger.isDebugEnabled())  {
		          logger.debug("selector thread started");
			 }
	    	
	      Set readyKeys = null;
	      long stop_ready_sendrecv =0l,  start_ready_sendrecv =0l ; 
	      Iterator<SelectionKey> readyItor = null;
	      SelectionKey key = null;
	      ByteBuffer lilBuffer = ByteBuffer.allocate(4);
	      SelectableChannel keyChannel = null;
	      SocketChannel socketChannel = null;
	      int tempRead = 0, read = 0, shutdownCounter = 0;
	      int header = 0;
	      Thread renewThreadStarter = null;
	      //long strt = 0L, stop = 0L, intv = 0L ;

	      try {
	        while (selector.select() > -1) {

	          //strt = System.nanoTime() ;

	          readyKeys = selector.selectedKeys();
	          readyItor = readyKeys.iterator();

	          while (readyItor.hasNext()) {

	            key = readyItor.next();
	            readyItor.remove();
	            keyChannel = (SelectableChannel) key.channel();

	            
	            if (DEBUG && logger.isDebugEnabled()) {
	                logger.debug("---selector EVENT---");
	              }
	            
	            if (key.isValid() && key.isAcceptable()) {

	              ServerSocketChannel sChannel =
	                  (ServerSocketChannel) keyChannel;
	              	             
	              if(initializing == false){
	            	  if (DEBUG && logger.isDebugEnabled()) {
			                logger.debug("---CLEAR TABLES---");
			              }
	            	  
	            	  for(int i = 0; i < readableChannels.size(); i++){
	            		  if(readableChannels.get(i).isOpen())
	            			  readableChannels.get(i).close();
	            	  }
            		  readableChannels.clear();
            		  
            		  for(int i = 0; i < writableChannels.size(); i++){
	            		  if(writableChannels.get(i).isOpen())
	            			  writableChannels.get(i).close();
	            	  }
            		  writableChannels.clear();
            		  
            		  worldReadableTable.clear();
            		  worldWritableTable.clear();
            		  initializing = true;
            		  
            		  renewThreadStarter=new Thread(renewThread);
            		  renewThreadStarter.start();
            	  }
	              
	              if (sChannel.socket().getLocalPort() == my_server_port) {
	            		  doAccept(keyChannel, writableChannels, true);
	              }
	              else if(sChannel.socket().getLocalPort() == my_server_port + 1){
	            		  doAccept(keyChannel, readableChannels, false);
	              }
	              else{
	            	  doAccept(keyChannel);
	              }

	            }
	            else if (key.isValid() && key.isReadable()) {
	            	
	            	

	              socketChannel = (SocketChannel) keyChannel;

	                /* Read the first 4 bytes */
	                lilBuffer.clear();
	                header = 0;

	                while (lilBuffer.hasRemaining()) {
	                  if ( (header = socketChannel.read(lilBuffer)) == -1) {
	                    //throw new ClosedChannelException();
	                    break;
	                  }
	                }

	                if (header != -1) {
	                  lilBuffer.flip();
	                  header = lilBuffer.getInt();
	                  lilBuffer.clear();
	                }
	                else {
	                  header = END_OF_STREAM;
	                }
	                
	                if (DEBUG && logger.isDebugEnabled()) {
		                logger.debug("---READ EVENT---" + header);
		              }

	                /**
	                 * 
	                 * 
	                 */
	                switch (header) {
	                  case NUM_OF_PROCCESSES:
	                	doReceiveNumOfProc((SocketChannel) keyChannel);
	                	break;
	                
	                  case INIT_MSG_HEADER_DATA_CHANNEL:
	                    doBarrierRead(  (SocketChannel) keyChannel,
	                                  worldReadableTable, false, false);
	                    break;
	                    
	                  case INIT_MSG_HEADER_CTRL_CHANNEL:	                	  
	                      doBarrierRead(  (SocketChannel) keyChannel,
	                                    worldReadableTable, false, false);
	                      break;

	                  case CHECKPOINT_RECONNECT:
	                	  if (DEBUG && logger.isDebugEnabled()) {
	  		                logger.debug("receive chepoint reconnect quest");
	  		              }
	                    doBarrierRead(  (SocketChannel) keyChannel,
	                                  worldReadableTable, false, true);
	                    break;
	               
	                    
	                  case START_CHECKPOINT:
	                	  initLock.acquire();
	                	  doCheckpoint( (SocketChannel) keyChannel,
	                              worldWritableTable);
	                	  
	                	  System.out.println("out checkpoint");
	                	  initLock.signal();
	                	  break;
	                	  
	                  case EXIT_PROCESS:
	                	  doSendBackAck((SocketChannel) keyChannel);
	                	  
	                	  break;
	                	  
	                  case END_OF_STREAM:

	                      realFinish((SocketChannel) keyChannel);

	                      break;
	                      
	                  case REQUEST_RESTART:
	                	  if(renewThreadStarter.getState().equals(Thread.State.BLOCKED));
	                	 		renewThreadStarter.interrupt();
	                	 		
	                	  initializing = false;
	                	  break;
	                    
	                  case END_APP:
	                	  clearContextFileAndDatabase();
	                	  break;

	                  default:

	                    System.out.println(" impossible ");
	                    break;

	                } //end switch-case
           
	            }
	            else if (key.isValid() && key.isWritable()) {

	              key.interestOps(SelectionKey.OP_READ);

	            } //end else writable.

	          } //end while iterator
	        } //end while
	      }
	      catch (Exception ioe1) {
	        ioe1.printStackTrace() ;
	      } //end catch(Exception e) ...

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
				return;
			}
			
			if (DEBUG && logger.isDebugEnabled()) {
              logger.debug("renew thread start");
            }
				
			 synchronized (writableChannels) {

		      if (writableChannels.size() != nprocs) {
		        try {
		        	writableChannels.wait();
		        }
		        catch (Exception e) {
		          e.printStackTrace();
		          initLock.signal();
		          initializing = false;
		          return;
		        }
		      }

		    } //end sync.
		    
			 if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("writableChannels renewed");
	            }

		    /* This is for control-channels. */
		    synchronized (readableChannels) {

		      if (readableChannels.size() != nprocs) {
		        try {
		        	readableChannels.wait();
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
		        	initLock.signal();
		        	initializing = false;
			          return;
		        }
		      }

		    } //end sync.
		    
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("readableChannels renewed");
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
		    synchronized (worldReadableTable) {
		      if ( (worldReadableTable.size() != nprocs)) {
		        try {
		          worldReadableTable.wait();
		        }
		        catch (Exception e) {
		          e.printStackTrace();
		          initLock.signal();
		          initializing = false;
		          return;
		        }
		      }
		    } //end sync
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("worldReadableTable renewed");
	            }
		    
		    for (int i = 0; i < writableChannels.size(); i++) {
		      SocketChannel socketChannel = writableChannels.get(i);
		      try {
		        doBarrierRead(socketChannel, worldWritableTable, true, false);
		      }
		      catch (Exception xde) {
		    	  xde.printStackTrace();
		    	  initLock.signal();
		    	  initializing = false;
		          return;
		      }
		    }
		    
		    
		    synchronized (worldWritableTable) {
		      if ( (worldWritableTable.size() != nprocs)) {
		        try {
		          worldWritableTable.wait();
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
		        	initLock.signal();
		        	initializing = false;
			          return;
		        }
		      }
		    } //end sync
		    
		    if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("worldWritableTable renewed");
	            }
		    
		    initializing = false;
		    
		    initLock.signal();
		  
		}
	};// end renew thread
	
	private void doReceiveNumOfProc(SocketChannel socketChannel) throws Exception {
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("--doReceiveNumOfProc--");
          }
		
		 ByteBuffer numBuffer = ByteBuffer.allocate(4);
		 
		 while(numBuffer.hasRemaining()){
			 try{
				 if(socketChannel.read(numBuffer) == -1)
					 throw new ClosedChannelException();
			 }
			 catch(Exception e){
				 throw e;
			 }
		 }
		 
		 numBuffer.flip();
		 nprocs = numBuffer.getInt();
		 pids = new UUID[nprocs];
		 
		 if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("Num of Processes : "+ nprocs);
	     }
		 
		 initLock.signal();
		
	}
	
	private void doSendBackAck(SocketChannel socketChannel) throws IOException {
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("---doSendBackAck---");
        }
		
		ByteBuffer exitBuffer = ByteBuffer.allocate(20);
		while(exitBuffer.hasRemaining()){
			try{
				if(socketChannel.read(exitBuffer) == -1){
					throw new ClosedChannelException();
				}
			}
			catch(IOException e){
				e.printStackTrace();
				throw e;
			}
		}
		exitBuffer.flip();
		int rank = exitBuffer.getInt();
		long msb = exitBuffer.getLong();
		long lsb = exitBuffer.getLong();
		UUID ruid = new UUID(msb, lsb);
		
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("send exit ack back to rank<" + rank + ">");
        }
	  	
		ByteBuffer askBuffer = ByteBuffer.allocate(4);
		askBuffer.putInt(CPSERVER_EXIT_ACK);
		
		SocketChannel c = worldWritableTable.get(ruid);
		askBuffer.flip();
		while(askBuffer.hasRemaining()){
			try{
				if(c.write(askBuffer) == -1)
					throw new ClosedChannelException();
			}
			catch(IOException e){
				e.printStackTrace();
				throw e;
			}
		}
		
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("---finish doSendBackAck---");
        }
	}
	
	  private boolean doAccept(SelectableChannel keyChannel,
				Vector<SocketChannel> channelCollection, boolean blocking) throws Exception {
			
		  	if (DEBUG && logger.isDebugEnabled()) {
              logger.debug("---do accept---");
            }
			SocketChannel peerChannel = null;

		    synchronized (channelCollection) {

		      if(keyChannel.isOpen()) { 
		        peerChannel = ( (ServerSocketChannel) keyChannel).accept();
		      }
		      else { 
		        return false; 
		      }

		      channelCollection.add(peerChannel);

		      if (blocking == false) {
		        peerChannel.configureBlocking(blocking);
		        peerChannel.register(selector,
		                             SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		      }
		      else {
		        peerChannel.configureBlocking(blocking);
		      }

		      peerChannel.socket().setTcpNoDelay(true);

		         peerChannel.socket().setSendBufferSize(524288);
		         peerChannel.socket().setReceiveBufferSize(524288);

		      if (channelCollection.size() == nprocs) {
		        channelCollection.notify();
		        
		        return true;
		      }
			
		    }//synchronized
		    

		   peerChannel = null;

		   return false;
		    
	  }//end of doaccept
	  
	  private boolean doAccept(SelectableChannel keyChannel) throws Exception {
			
		  	if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("---do accept---");
          }

		  	if(keyChannel.isOpen()) { 
		        controlChannel = ( (ServerSocketChannel) keyChannel).accept();
		    }
		    else { 
		        return false; 
		    }

		  	controlChannel.configureBlocking(false);
		  	controlChannel.register(selector,
	                             SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		  	
		  	controlChannel.socket().setTcpNoDelay(true);

		  	controlChannel.socket().setSendBufferSize(524288);
		  	controlChannel.socket().setReceiveBufferSize(524288);

		   return false;
		    
	  }//end of doaccept
	  
	  /*
	   * This method is used during initialization.
	   */
	  void doBarrierRead(SocketChannel socketChannel, Hashtable table, boolean
	                     ignoreFirstFourBytes, boolean hasCheckpointInfo) throws Exception {
		  
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
	    	  e.printStackTrace();
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
	    size = nprocs;
	    
	    //if has already checkpointed, write to database 
	    if(hasCheckpointInfo){
	    	
	    	if (DEBUG && logger.isDebugEnabled()) {
	              logger.debug("--checkpointed--");
	        }
	    	barrBuffer.position(0);
	    	barrBuffer.limit(8);
	    	while(barrBuffer.hasRemaining()){
	    		try{
	    			if(socketChannel.read(barrBuffer) == -1)
	    				throw new ClosedChannelException();
	    		}
	    		catch(IOException e){
	    			e.printStackTrace();
	    			if (DEBUG && logger.isDebugEnabled()) {
	    	            logger.debug("reading not complete, this should not happen!");
	    	        }
	    			return;
	    		}
	    	}
	    	barrBuffer.flip();
	    	int processId =  barrBuffer.getInt();
	    	int verNum = barrBuffer.getInt();
	    	
	    	addItemToDatabase(rank, processId, verNum);
	    	updateContextDatabase();
	    }
	    
	    


	    synchronized (table) {
	      table.put(ruid, socketChannel);
	      
	      if (DEBUG && logger.isDebugEnabled()) {
              logger.debug("add rand" + rank + " to table:" + table);
              logger.debug("table size:" + table.size());
          }

	      if ( (table.size() == nprocs )) {
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
	  
	  private void updateContextDatabase() {
		  
		  if (DEBUG && logger.isDebugEnabled()) {
	            logger.debug("update context file and database");
	      }
		  
		  ContextDao dao = ContextFactory.getContextDao();
		  
		  Integer latestVer = dao.getLatestVersionId();
		  
		  if(latestVer == null)
			  return;
		  
		  List contextList = dao.getContextsByVersion(latestVer);
		  if(contextList.size() < nprocs){
			  return;
		  }
		  else if(contextList.size() == nprocs){
			  Integer nextVer = dao.getNextLatestVersionId(latestVer);
			  if(nextVer == null)
				  return;
			  
			  contextList = dao.getAllPrevContextsByVersion(latestVer);
			  for(int i = 0; i < contextList.size(); i++){
				  Context c = (Context)contextList.get(i);
				  File file = new File(c.getContextFilePath());
				  if(file.exists())
					  file.delete();
				  
				  file = new File(c.getTempFilePath());
				  if(file.exists())
					  file.delete();
			  }
			  
			  dao.delAllPrevContextsByVersion(latestVer);
			  
			  
		  }
		  else{
			  System.out.println("Version number error, Impossible!");
		  }
			  
	  }
	  
	  private void clearContextFileAndDatabase() {
			ContextDao dao = ContextFactory.getContextDao();
			Integer latestVer = dao.getLatestVersionId();
			if(latestVer != null){
				List contextList = dao.getAllPrevContextsByVersion(latestVer + 1 );
				if(contextList != null){
				  for(int i = 0; i < contextList.size(); i++){
					  Context c = (Context)contextList.get(i);
					  File file = new File(c.getContextFilePath());
					  if(file.exists())
						  file.delete();
					  
					  file = new File(c.getTempFilePath());
					  if(file.exists())
						  file.delete();
				  }
				  
				  dao.delAllPrevContextsByVersion(latestVer + 1);	
				}
			}
		
	  }

	private void addItemToDatabase(int rank, int processId, int verNum) {
		if (DEBUG && logger.isDebugEnabled()) {
            logger.debug("add to database, rank:" + rank + " processId:" + processId +
            		" versionId:" + verNum);
      }
		// TODO Auto-generated method stub
		String contextFilePath =  mpjHomeDir + File.separator + CONTEXT_DIR_NAME +
		File.separator + "context." + processId + "_Rank_" + rank + "_Ver_" + verNum;
		String tempFilePath = mpjHomeDir + File.separator + CONTEXT_DIR_NAME +
		File.separator + processId + "_Rank_" + rank + "_Ver_" + verNum;
		
		Context c = new Context();
		c.setRank(rank);
		c.setProcessId(processId);
		c.setVersionId(verNum);
		c.setContextFilePath(contextFilePath);
		c.setTempFilePath(tempFilePath);
		
		ContextDao dao = ContextFactory.getContextDao();
		dao.save(c);
		  
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
	  
	  
	  
	  private void doCheckpoint(SocketChannel socketChannel,
				Hashtable<UUID, SocketChannel> worldWritableTable) throws Exception {
			
		  	ByteBuffer cMsgBuffer = ByteBuffer.allocate(28);	  
		  	ByteBuffer ackBuffer = ByteBuffer.allocate(4);
		    
		    cMsgBuffer.limit(28);
		    cMsgBuffer.position(4);
		    
		    while (cMsgBuffer.hasRemaining()) {
		        try {
		          if (socketChannel.read(cMsgBuffer) == -1) {
		            throw new Exception(new ClosedChannelException());
		          }
		        }
		        catch (Exception e) {
		          throw e;
		        }
		    }
		    
		    int rank;
		    long msb,lsb;
		    UUID ruid;
		    cMsgBuffer.position(4);
		    rank = cMsgBuffer.getInt();
		    msb = cMsgBuffer.getLong();
		    lsb = cMsgBuffer.getLong();
		    ruid = new UUID(msb, lsb);
		    versionNum = cMsgBuffer.getInt(); 
		    
		    if(initializing == true)
		    	pids[rank]=ruid;
		    
		    
		    ackBuffer.limit(4);
		    ackBuffer.putInt(MARKER_ACK);
		    SocketChannel c = worldWritableTable.get(ruid);
		    
		    ackBuffer.flip();
		    while(ackBuffer.hasRemaining()){	    	
		    	try {
			          if (c.write(ackBuffer) == -1) {
			            throw new Exception(new ClosedChannelException());
			          }
			    }
		        catch (Exception e) {
		        	System.out.println("can not write back marker ack");
		        	throw e;
		        }
		    }
		    
		    cMsgBuffer.position(0);
		    cMsgBuffer.limit(28);
		    cMsgBuffer.putInt(START_CHECKPOINT);
		    cMsgBuffer.putInt(rank);
		    cMsgBuffer.putLong(msb);
		    cMsgBuffer.putLong(lsb);
		    cMsgBuffer.putInt(versionNum);
		    Iterator it = worldWritableTable.entrySet().iterator();
		    SocketChannel other = null;
		    while(it.hasNext()){
		    	java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
		    	if(((UUID)entry.getKey()).equals(ruid))
		    		continue;
		    	
		    	cMsgBuffer.flip();
		    	other = (SocketChannel)entry.getValue();
		    	while(cMsgBuffer.hasRemaining()){	    	
			    	try {
				          if (other.write(cMsgBuffer) == -1) {
				            throw new Exception(new ClosedChannelException());
				          }
				    }
			        catch (Exception e) {
			        	System.out.println("can not write marker to other process");
			        	throw e;
			        }
			    }
		    }
			
		}
	  
	  
	  private void realFinish(SocketChannel socketChannel) {
			// TODO Auto-generated method stub
			try {
				socketChannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	  };
	  
	  
	  public static void main(String[] args){
		  Context c = new Context();
		  c.setProcessId(10000);
		  c.setRank(0);
		  c.setVersionId(2);
		  c.setContextFilePath("context.10000_Rank_0_Ver_1");
		  c.setTempFilePath("10000_Rank_0_Ver_1");
		  

		  ContextDao dao = ContextFactory.getContextDao();
		  dao.save(c);
		  System.out.println(dao.getContextsByVersion(1).size());
		  System.out.println(dao.getLatestVersionId());
		  System.out.println(dao.getNextLatestVersionId(dao.getLatestVersionId()));
	  }
}
