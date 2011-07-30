package netapi;

import java.net.Socket;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashSet;

/**
* NetAPI for easy controlling of the sending and receiving of packets
* to and from the server and client. 
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class NetAPI {
	/**	
	* The packet handler mappings
	*
	* @since	0.1
	*/
	private static Hashtable<NetPacket, HashSet<NetPacketHandler>> handlers 
		= new Hashtable<NetPacket, HashSet<NetPacketHandler>>();
	/**
	* The packet thread
	*
	* @since	0.1
	*/
	private static NetPacketThread netThread = null;
	
	//===================
	// Packet methods
	//===================
	
	/**
	* Send a given packet
	*
	* @param	packet		The packet to send
	*/
	public static void sendPacket(NetPacket packet) {
		netThread.send(packet);
	}
	
	//===================
	// Handler handling methods
	//===================
	
	/**
	* Get a array of all handlers for a given packet
	*
	* @return	Array of all handlers for a given object
	*/
	public static NetPacketHandler[] getHandlers(NetPacket packet) {
		if(!handlers.containsKey(packet)) {
			return new NetPacketHandler[0];
		} else {
			HashSet<NetPacketHandler> s = handlers.get(packet);
			
			return s.toArray(new NetPacketHandler[s.size()]);
		}
	}
	
	/**
	* Add a packet handler for a given packet
	*
	* @param	packet		Packet we are adding handler for
	* @param	handler		The handler we are adding to the packet
	*/
	public static void addHandler(NetPacket packet, NetPacketHandler handler) {
		if(!handlers.containsKey(packet)) {
			handlers.put(packet, new HashSet<NetPacketHandler>());
		}
		
		handlers.get(packet).add(handler);
	}
	
	/**
	* Remove a packet handler for a given packet
	*
	* @param	packet		Packet we are deleting a handler for
	* @param	handler		The handler we are deleting
	*/
	public static void removeHandler(NetPacket packet, NetPacketHandler handler) {
		if(handlers.containsKey(packet)) {
			HashSet<NetPacketHandler> set = handlers.get(packet);
			
			if(set.contains(handler)) {
				set.remove(handler);
			}
			
			if(set.size() == 0) {
				removeAllHandlers(packet);
			}
		}
	}
	
	/**
	* Remove all packet handlers for a given packet
	*
	* @param	packet 		Packer we are deleting all handlers for
	*/
	public static void removeAllHandlers(NetPacket packet) {
		if(handlers.containsKey(packet)) {
			handlers.remove(packet);
		}
	}
	
	//============
	// Operation methods. Not for API
	//============
	
	/**
	* Create and set a new net thread when connecting
	* to a new server
	*
	* @throws	IOException	When the thread could not connect the data streams
	* @param	socket	Socket we have connected to
	* @param	mode	True if the thread is a sender thread, false if receiver
	*/
	public static NetPacketThread getNewNetThread(Socket socket, boolean mode) 
		throws IOException {
		netThread = new NetPacketThread(socket, mode);
		
		return netThread;
	}
}