package netapi;

import netapi.packet.NetPacket;
import netapi.packet.NetP2PPacket;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
* A thread that handles all packet handling
* from server to client. A different build exists
* for client and server
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class NetPacketThread extends Thread {
	/**
	* The current send queue
	*
	* @since	0.1
	*/
	private	ConcurrentLinkedQueue<NetPacket> sendQueue = new ConcurrentLinkedQueue<NetPacket>();
	/**
	* The output stream for this thread
	*
	* @since	0.1
	*/
	private ObjectOutputStream	oos		= null;
	/**
	* The input stream for this thread
	*
	* @since	0.1
	*/
	private ObjectInputStream	ois		= null;
	/**
	* Whether this current thread is alive
	*
	* @since	0.1
	*/
	private	boolean				alive = true;
	/**
	* True if sender thread, false if receiver
	*
	* @since	0.1
	*/
	private boolean				sender = true;
	/**
	* Name of user sending data from this thread
	*
	* @since	0.1
	*/
	private String				senderName;
	/**
	* The logger we are logging with
	*
	* @since	0.1
	*/
	private	Logger				log	= Logger.getLogger("Minecraft");
	/**
	* The socket we are connected to
	*
	* @since	0.1
	*/
	private Socket				socket;
	
	/**
	* Called at creation of a new server
	*
	* @since	0.1
	* @throws	IOException	If cannot connect data stream
	* @param	socket		The socket we are connecte to
	* @param	mode		True if a sender thread, false if receiver
	*/
	public NetPacketThread(Socket socket, boolean mode) {
		sender 		= mode;
		this.socket = socket;
	}
	
	/**
	* Called at creation of a new server
	*
	* @since	0.1
	* @param	socket		The socket we are connecte to
	* @param	oos			The sending data stream
	* @param	mode		True if a sender thread, false if receiver
	*/
	public NetPacketThread(Socket socket, ObjectOutputStream oos) {
		sender 		= true;
		this.socket = socket;
		this.oos	= oos;	
	}
	
	/**
	* Called at creation of a new server
	*
	* @since	0.1
	* @param	socket		The socket we are connecte to
	* @param	ois			The receiving data stream
	* @param	mode		True if a sender thread, false if receiver
	*/
	public NetPacketThread(Socket socket, ObjectInputStream ois) {
		sender 		= false;
		this.socket = socket;
		this.ois	= ois;
	}
	
	//===============
	// Setters/ Adders
	//===============
	
	/**
	* Send the given packet
	*
	* @since	0.1
	* @param	packet	New packet to send
	*/
	public void send(NetPacket packet) {
		sendQueue.add(packet);
	}
	
	/**
	* Set the username for sending data
	*
	* @since	0.1
	* @param	username	Username of this sender
	*/
	public void setSenderName(String username) {
		this.senderName = username;
	}
	
	//===============
	// Data flow and execution
	//===============
	
	/**
	* Execution of this net thread
	*
	* @since	0.1
	*/
	public void run() {
		try {
			if(!sender && (ois == null)) {
				log.info("(NetAPI) Packet reading thread opening");
				ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			} else if(sender && (oos == null)) {
				log.info("(NetAPI) Packet writing thread opening");
				oos = new ObjectOutputStream(socket.getOutputStream());
			}
			
			while(alive) {
				if(sender) {
					sendNewPackets();
				} else {
					receiveNewPackets();
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
			
		} catch (IOException e) {
			log.info("(NetAPI) Connection failed to stream " + e.getMessage());
		}
		
		log.info("(NetAPI) Closing packet thread");
	}
	
	/**
	* Send all new packets on the packet queue
	*
	* @since	0.1
	*/
	private void sendNewPackets() {
		while(alive && (sendQueue.size() != 0)) {
			log.info("(NetAPI) Sending packet");
			NetPacket send = sendQueue.peek();
			try {
				oos.writeObject(send);
				sendQueue.poll();
			} catch (IOException e) {
				System.err.println("(NetAPI) Could not send packet: " + e.getMessage());
			}
		}
	}
	
	/**
	* Receive all new packets on the packet queue
	*
	* @since	0.1
	*/
	private void receiveNewPackets() {
		log.info("(NetAPI) Packet thread in receive mode");
		Object	in = null;
		
		try {
			while(alive) {	
				try {
					in = ois.readObject();
					log.info("(NetAPI) Packet received");
					// P2P packets are ignored by the server
					if(in instanceof NetP2PPacket) {
						NetP2PPacket packet = (NetP2PPacket) in;
						log.info("(NetAPI) Received a P2P Packet: " + packet.getClass().getName());
						
						processP2PPacket(packet);
					// P2S packets are controlled by the server
					} else if(in instanceof NetPacket) {
						NetPacket packet = (NetPacket) in;
						
						processPacket(packet);
					}
				} catch (SocketTimeoutException e) {
					// Ignore
				}
			}
		} catch (IOException e) {
			log.info("(NetAPI) IOException in receiving: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			log.info("(NetAPI) Could not find class: " + e.getMessage());
		}
		
		log.info("(NetAPI) Packet thread receving stopped");
	}
	
	/**
	* Process a regular packet
	*
	* @param	packet	Packet to handle
	*/
	private void processPacket(NetPacket packet) {	
		packet.setSender(senderName);					
		NetPacketHandler[] handlers = NetAPI.getHandlers(packet);
		
		for(NetPacketHandler handler : handlers) {
			// To avoid a handler locking up the receiver thread
			// We will shove them into a temporary thread
			HandlerThread p = new HandlerThread(handler, packet);
			p.start();
			handler.handle(packet);
		}
	}
	
	/**
	* Process an incoming P2P packet and send it on to
	* any real peers
	*
	* @param	packet	Packet to send
	*/
	private void processP2PPacket(NetP2PPacket packet) {
		packet.setSender(senderName);
		NetAPI.sendPacketToPlayers(packet);
	}
	
	//===============
	// Thread control methods
	//===============
	
	/**
	* Called at server close time
	*
	* @since	0.1
	*/
	public void stopThread() {
		log.info("(NetAPI) Stopping net packet thread");
		alive = false;
		
		try {
			if(oos != null) {
				oos.close();
			}
			if(ois != null) {
				ois.close();
			}
		} catch (IOException e) {
			// Why the hell is that being thrown here? Doesn't matter.
		}
	}
	
	/**
	* A temporary packet handling thread to avoid locking the main
	* net packet thread
	*
	* @author	Clinton Alexander
	* @since	0.1
	*/
	private class HandlerThread extends Thread {
		/**
		* The handler for this thread
		*
		* @since	0.1
		*/
		private NetPacketHandler	handler;
		/**
		* The packet for this thread
		*
		* @since	0.1
		*/
		private NetPacket			packet;
		/**
		* Create the handler thread
		*
		* @since	0.1
		* @param	handler	Handler we are handling with
		* @param	packet	Packet to handler
		*/
		public HandlerThread(NetPacketHandler handler, NetPacket packet) {
			this.packet  = packet;
			this.handler = handler;
		}
		
		
		public void run() {
			handler.handle(packet);
		}
	}
}