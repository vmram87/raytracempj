package org.qing.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;








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
	
	String localHostName = null;
	InetAddress localaddr = null;
	private String[] args = null;
	UUID[] pids = null;
	
	 /* Server Socket Channel */
	  ServerSocketChannel writableServerChannel = null;
	  ServerSocketChannel readableServerChannel = null;
	  
	  
	  
	/*
	   * This integer is used as the header to send initial control messages
	   */
	  private final int INIT_MSG_HEADER_DATA_CHANNEL = -21;
	
	  private final int INIT_MSG_HEADER_CTRL_CHANNEL = -20;
	  
	  private final int CHECKPOINT_RECONNECT = -40;
	  
	  private final int MARKER_ACK = -41;
	
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
	
	  private final int SEND_ACK_TO_SENDER = -80;
	
	  private final int RECV_IN_USER_MEMORY = -81;
	
	  private final int RECV_IN_DEV_MEMORY = -82;
	
	  private final int MORE_TO_WRITE = -83;
	
	  private final int MORE_TO_READ = -84;
	  
	
	Vector<SocketChannel> writableChannels = new Vector<SocketChannel> ();
	Vector<SocketChannel> tempWritableChannels = new Vector<SocketChannel> ();

	Vector<SocketChannel> readableChannels = new Vector<SocketChannel> ();
	Vector<SocketChannel> tempReadableChannels = new Vector<SocketChannel> ();
	
	private boolean isRenew = false;

	Hashtable<UUID, SocketChannel> worldWritableTable =
		new Hashtable<UUID, SocketChannel> ();

	Hashtable<UUID, SocketChannel> worldReadableTable =
		new Hashtable<UUID, SocketChannel> ();

	Hashtable<UUID, CustomSemaphore> writeLockTable =
		new Hashtable<UUID, CustomSemaphore> ();
	
	
	
	
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
		Map<String,String> map = System.getenv() ;
	    mpjHomeDir = map.get("MPJ_HOME");
	    
	    try {

	        localaddr = InetAddress.getLocalHost();
	        localHostName = localaddr.getHostName();

	      }
	      catch (UnknownHostException unkhe) {
	        throw new Exception(unkhe);
	      }

	      String[] argv={"0","mpj.conf","niodev"};
	      this.args = argv;
	      
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
		  ConfigReader reader = null;
	  
		    try {
		      reader = new ConfigReader(args[1]); 
		      nprocs = (new Integer(reader.readNoOfProc())).intValue();
		      psl = (new Integer(reader.readIntAsString())).intValue();
		      if(psl < 12) {      
		        psl = 12;  	      
		      }
		    }
		    catch (Exception config_error) {
		      throw new Exception(config_error);
		    }


		    pids = new UUID [nprocs];

		    String[] nodeList = new String[nprocs];
		    int[] pList = new int[nprocs];
		    int[] rankList = new int[nprocs];
		    int count = 0;

		    while (count < nprocs) {

		      String line = null;

		      try {
		        line = reader.readLine();
		      }
		      catch (IOException ioe) {
		        throw new Exception(ioe);
		      }

		      if (line == null || line.equals("") || line.startsWith("#")) {
		        continue;
		      }
		      
		      if(line.contains("$")){
		    	  //checkpoint must be declare before the nodes
		    	  line = line.trim();
			      StringTokenizer tokenizer = new StringTokenizer(line, "$");
			      server_host = tokenizer.nextToken();
			      my_server_port = (new Integer(tokenizer.nextToken())).intValue();
			      server_rank = (new Integer(tokenizer.nextToken())).intValue();
		    	  
		    	  continue;
		      }

		      line = line.trim();
		      StringTokenizer tokenizer = new StringTokenizer(line, "@");
		      nodeList[count] = tokenizer.nextToken();
		      pList[count] = (new Integer(tokenizer.nextToken())).intValue();
		      rankList[count] = (new Integer(tokenizer.nextToken())).intValue();
		      count++;

		    }

		    reader.close();
		    reader = null;
		    
		    

		    /* Open the selector */
		    try {
		      selector = Selector.open();
		    }
		    catch (IOException ioe) {
		      throw new Exception(ioe);
		    }
		    
		    /* Create control server socket */
		    SocketChannel[] wChannels = new SocketChannel[nodeList.length ];

		    
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


			        readableServerChannel = ServerSocketChannel.open();
			        readableServerChannel.configureBlocking(false);
			        readableServerChannel.socket().bind(
			            new InetSocketAddress( (my_server_port + 1)));
			        readableServerChannel.register(selector, SelectionKey.OP_ACCEPT);

		      }
		      catch (IOException ioe) {
		        isError = true;
		        try { Thread.sleep(500); } catch(Exception e){}
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



		    //addShutdownHook();


		    count = 0;

		    /* doAccept() and/or while
		     * loop above adds SocketChannels to writableChannels
		     * so access to writableChannels should be synchronized.
		     */
		    synchronized (writableChannels) {

		      if (writableChannels.size() != nprocs) {
		        try {
		          writableChannels.wait();
		        }
		        catch (Exception e) {
		          throw new Exception(e);
		        }
		      }

		    } //end sync.
		    
		    System.out.println("1");

		    /* This is for control-channels. */
		    synchronized (readableChannels) {

		      if (readableChannels.size() != nprocs) {
		        try {
		          readableChannels.wait();
		        }
		        catch (Exception e) {
		          throw new Exception(e);
		        }
		      }

		    } //end sync.
		    System.out.println("2");
		    
		    isRenew = true;


		    /*
		     * At this point, all-to-all connectivity has been acheived. 
		     */

		    /* worldTable is accessed from doBarrierRead or here, so their access
		     * should be synchronized */
		    synchronized (worldReadableTable) {
		      if ( (worldReadableTable.size() != nprocs)) {
		        try {
		          worldReadableTable.wait();
		        }
		        catch (Exception e) {
		          throw new Exception(e);
		        }
		      }
		    } //end sync
		    
		    System.out.println("3");
		    
		    
		    /*
		     * non checkpoint server node send the rank, msb, lsb to the checkpoint server
		     *  Do blocking-reads, record the every pair of <uuid, writableChannel>
		     *  the pair of <uuid, readableChannel> left for the selecotr thread to record
		     */
		    for (int i = 0; i < writableChannels.size(); i++) {
		      SocketChannel socketChannel = writableChannels.get(i);
		      try {
		        doBarrierRead(socketChannel, worldWritableTable, true);
		      }
		      catch (Exception xde) {
		        throw xde;
		      }
		    }
		    

		    synchronized (worldWritableTable) {
		      if ( (worldWritableTable.size() != nprocs)) {
		        try {
		          worldWritableTable.wait();
		        }
		        catch (Exception e) {
		          throw new Exception(e);
		        }
		      }
		    } //end sync
		    
		    System.out.println("4");


		    for (int k = 0; k < pids.length; k++) {
		    	writeLockTable.put(pids[k],new CustomSemaphore(1));
		    }

		    
		    
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

	      Set readyKeys = null;
	      long stop_ready_sendrecv =0l,  start_ready_sendrecv =0l ; 
	      Iterator<SelectionKey> readyItor = null;
	      SelectionKey key = null;
	      ByteBuffer lilBuffer = ByteBuffer.allocate(4);
	      SelectableChannel keyChannel = null;
	      SocketChannel socketChannel = null;
	      int tempRead = 0, read = 0, shutdownCounter = 0;
	      int header = 0;
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

	            if (key.isValid() && key.isAcceptable()) {

	              ServerSocketChannel sChannel =
	                  (ServerSocketChannel) keyChannel;
	              if (sChannel.socket().getLocalPort() == my_server_port) {

	            	  if(!isRenew)
	            		  doAccept(keyChannel, writableChannels, true);
	            	  else
	            		  doAccept(keyChannel, tempWritableChannels, true);
	              }
	              else {
	            	  if(!isRenew)
	            		  doAccept(keyChannel, readableChannels, false);
	            	  else
	            		  doAccept(keyChannel, tempReadableChannels, false);
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

	                /**
	                 * 
	                 * 
	                 */
	                switch (header) {
	                
	                  case INIT_MSG_HEADER_DATA_CHANNEL:
	                    doBarrierRead( ( (SocketChannel) keyChannel),
	                                  worldReadableTable, false);
	                    break;
	                    
	                  case INIT_MSG_HEADER_CTRL_CHANNEL:
	                      doBarrierRead( ( (SocketChannel) keyChannel),
	                                    worldReadableTable, false);
	                      break;

	                  case CHECKPOINT_RECONNECT:
	                	  if(isCheckpointing == false){
	                		  isCheckpointing = true;
	                		  (new Thread(renewThread)).start();
	                	  }
	                    doBarrierRead( ( (SocketChannel) keyChannel),
	                                  worldReadableTable, false);
	                    break;
	               
	                    
	                  case START_CHECKPOINT:
	                	  doCheckpoint( ( (SocketChannel) keyChannel),
	                              worldWritableTable);
	                	  
	                	  System.out.println("out checkpoint");
	                	  break;
	                	  
	                  case END_OF_STREAM:

	                      realFinish();

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
			 synchronized (tempWritableChannels) {

		      if (tempWritableChannels.size() != nprocs) {
		        try {
		        	tempWritableChannels.wait();
		        }
		        catch (Exception e) {
		          e.printStackTrace();
		        }
		      }

		    } //end sync.
			 for(int i=0;i < writableChannels.size();i++){
				try {
					writableChannels.get(i).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
			 
			 writableChannels.clear();
			 writableChannels = tempReadableChannels;
		    
		    System.out.println("r1");

		    /* This is for control-channels. */
		    synchronized (tempReadableChannels) {

		      if (tempReadableChannels.size() != nprocs) {
		        try {
		        	tempReadableChannels.wait();
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
		        }
		      }

		    } //end sync.
		    
		    for(int i=0;i < readableChannels.size();i++){
				try {
					readableChannels.get(i).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
			 
			readableChannels.clear();
		    readableChannels = tempReadableChannels;
		    
		    tempReadableChannels =  new Vector<SocketChannel> ();
		    tempWritableChannels =  new Vector<SocketChannel> ();
		    System.out.println("r2");
		    
		    
		    
		    /*
		     * At this point, all-to-all connectivity has been acheived. 
		     */
    
		    worldWritableTable.clear();
		    worldReadableTable.clear();
		    
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
		        }
		      }
		    } //end sync
		    System.out.println("r3");
		    
		    for (int i = 0; i < writableChannels.size(); i++) {
		      SocketChannel socketChannel = writableChannels.get(i);
		      try {
		        doBarrierRead(socketChannel, worldWritableTable, true);
		      }
		      catch (Exception xde) {
		    	  xde.printStackTrace();
		      }
		    }
		    
		    
		    synchronized (worldWritableTable) {
		      if ( (worldWritableTable.size() != nprocs - 1)) {
		        try {
		          worldWritableTable.wait();
		        }
		        catch (Exception e) {
		        	e.printStackTrace();
		        }
		      }
		    } //end sync
		    System.out.println("r4");
		  
		}
	};// end renew thread
	
	
	  private boolean doAccept(SelectableChannel keyChannel,
				Vector<SocketChannel> channelCollection, boolean blocking) throws Exception {
			
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
	  
	  /*
	   * This method is used during initialization.
	   */
	  void doBarrierRead(SocketChannel socketChannel, Hashtable table, boolean
	                     ignoreFirstFourBytes) throws Exception {

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

	    if (ignoreFirstFourBytes) {
	      barrBuffer.getInt();
	    }

	    rank = barrBuffer.getInt();
	    msb = barrBuffer.getLong();
	    lsb = barrBuffer.getLong();
	    barrBuffer.clear();
	    ruid = new UUID(msb, lsb);
	    pids[rank] = ruid; //, rank);
	    size = nprocs;


	    synchronized (table) {
	      table.put(ruid, socketChannel);


	      if ( (table.size() == nprocs )) {
	        try {
	          table.notify();
	        }
	        catch (Exception e) {
	          throw new Exception(e);
	        }
	      }

	    }

	  }//end of do barrier read
	  
	  
	  
	  private void doCheckpoint(SocketChannel socketChannel,
				Hashtable<UUID, SocketChannel> worldWritableTable) throws Exception {
			
		  	ByteBuffer cMsgBuffer = ByteBuffer.allocate(12);	  
		  	ByteBuffer ackBuffer = ByteBuffer.allocate(4);
		    
		    cMsgBuffer.limit(12);
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
		    cMsgBuffer.position(4);
		    rank =cMsgBuffer.getInt();
		    versionNum = cMsgBuffer.getInt();
		    
		    UUID uid = pids[rank];
		    
		    ackBuffer.limit(4);
		    ackBuffer.putInt(MARKER_ACK);
		    SocketChannel c = worldWritableTable.get(uid);
		    
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
		    cMsgBuffer.limit(12);
		    cMsgBuffer.putInt(START_CHECKPOINT);
		    cMsgBuffer.putInt(rank);
		    cMsgBuffer.putInt(versionNum);
		    Iterator it = worldWritableTable.entrySet().iterator();
		    SocketChannel other = null;
		    while(it.hasNext()){
		    	java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
		    	if(((UUID)entry.getKey()).equals(uid))
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
	  
	  
	  private void realFinish() {
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
	  };
}
