package netapi;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	private ObjectOutputStream	oos;
	/**
	* The input stream for this thread
	*
	* @since	0.1
	*/
	private ObjectInputStream	ois;
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
	* Called at creation of a new server
	*
	* @since	0.1
	* @throws	IOException	When the packets can't be sent or received
	* @param	socket		The socket we are connecte to
	* @param	mode		True if a sender thread, false if receiver
	*/
	public NetPacketThread(Socket socket, boolean mode) throws IOException {
		if(!mode) {
			System.out.println("(NetAPI) Creating Input Stream");
			ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		} else {
			System.out.println("(NetAPI) Creating Output Stream");
			oos = new ObjectOutputStream(socket.getOutputStream());
		}
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
	
	//===============
	// Data flow and execution
	//===============
	
	/**
	* Execution of this net thread
	*
	* @since	0.1
	*/
	public void run() {
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
	}
	
	/**
	* Send all new packets on the packet queue
	*
	* @since	0.1
	*/
	private void sendNewPackets() {
		while(alive && (sendQueue.size() != 0)) {
			NetPacket send = sendQueue.peek();
			try {
				System.out.println("(NetAPI) Sending a " + send.getClass().getName() + " packet");
				oos.writeObject(send);
				oos.flush();
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
		Object	in;
		
		try {
			while(alive && ((in = ois.readObject()) != null)) {
				if(in instanceof NetPacket) {
					NetPacket packet = (NetPacket) in;
					System.out.println("(NetAPI) Received a " + packet.getClass().getName() + " packet");
					
					NetPacketHandler[] handlers = NetAPI.getHandlers(packet);
					
					for(NetPacketHandler handler : handlers) {
						// To avoid a handler locking up the receiver thread
						// We will shove them into a temporary thread
						HandlerThread p = new HandlerThread(handler, packet);
						p.start();
						handler.handle(packet);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("(NetAPI) IOException in receiving: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("(NetAPI) Could not find class: " + e.getMessage());
		}
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