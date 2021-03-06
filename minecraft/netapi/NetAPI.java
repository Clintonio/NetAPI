package netapi;

import netapi.packet.NetPacket;
import netapi.packet.NetP2PPacket;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Collection;
import java.util.logging.Logger;

import net.minecraft.src.EntityPlayer;

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
	private static Hashtable<Class, HashSet<NetPacketHandler>> handlers 
		= new Hashtable<Class, HashSet<NetPacketHandler>>();
	/**
	* The packet thread for sending packets
	*
	* @since	0.1
	*/
	private static NetPacketThread netThread = null;
	/**
	* The logger for netAPI 
	*
	* @since	0.1
	*/
	public static final Logger log = Logger.getLogger("Minecraft");
	
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
	
	/**
	* Sends a packet to a specific player
	*
	* @param	packet		The packet to send
	* @param	player		Player to send packet to
	*/
	public static void sendPacketToPlayer(NetPacket packet, EntityPlayer player) {
		String username = player.username;
		if(username != null) {
			netThread.send(new NetP2PPacket(username, packet));
		}
	}
	
	/**
	* Send a packet to many players
	*
	* @param	packet		The packet to send
	* @param	players		The players to send the packet to
	*/
	public static void sendPacketToPlayers(NetPacket packet, EntityPlayer[] players) {
		String[] unames = new String[players.length];
		
		for(int x = 0; x < players.length; x++) {
			unames[x] = players[x].username;
		}
		
		netThread.send(new NetP2PPacket(unames, packet));
	}
	
	/**
	* Send a packet to many players
	*
	* @param	packet		The packet to send
	* @param	players		The players to send to
	*/
	public static void sendPacketToPlayers(NetPacket packet, Collection<EntityPlayer> players) {
		sendPacketToPlayers(packet, players.toArray(new EntityPlayer[players.size()]));
	}
	
	/**
	* Send a P2P packet to many players
	*
	* @param	packet		The packet to send
	*/
	public static void sendPacketToPlayers(NetP2PPacket packet) {
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
		Class packetClass = packet.getClass();
		if(!handlers.containsKey(packetClass)) {
			return new NetPacketHandler[0];
		} else {
			HashSet<NetPacketHandler> s = handlers.get(packetClass);
			
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
		Class packetClass = packet.getClass();
		if(!handlers.containsKey(packetClass)) {
			handlers.put(packetClass, new HashSet<NetPacketHandler>());
		}
		
		handlers.get(packetClass).add(handler);
	}
	
	/**
	* Remove a packet handler for a given packet
	*
	* @param	packet		Packet we are deleting a handler for
	* @param	handler		The handler we are deleting
	*/
	public static void removeHandler(NetPacket packet, NetPacketHandler handler) {
		Class packetClass = packet.getClass();
		if(handlers.containsKey(packetClass)) {
			HashSet<NetPacketHandler> set = handlers.get(packetClass);
			
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
		Class packetClass = packet.getClass();
		if(handlers.containsKey(packetClass)) {
			handlers.remove(packetClass);
		}
	}
	
	//============
	// Client side only
	//============
	
	/**
	* Check if currently connected to a server
	*
	* @return	True if connected
	*/
	public static boolean isConnected() {
		return (netThread != null);
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
		if(mode) {
			netThread = new NetPacketThread(socket, mode);
		
			return netThread;
		} else {
			return new NetPacketThread(socket, mode);
		}
	}
	
	/**
	* Create and set a new net thread when connecting
	* to a new server. Not a part of the API.
	*
	* @since	0.1
	* @param	oos			Data sending stream
	* @param	username	Username that is sending new packet threads
	*/
	public static NetPacketThread getNewNetThread(Socket socket, ObjectOutputStream oos) {
		netThread = new NetPacketThread(socket, oos);
	
		return netThread;
	}
	
	/**
	* Create and set a new net thread when connecting
	* to a new server. Not a part of the API.
	*
	* @since	0.1
	* @param	ois			Data sending stream
	* @param	username	Username that is receiving new packet threads
	*/
	public static NetPacketThread getNewNetThread(Socket socket, ObjectInputStream ois) {
		return new NetPacketThread(socket, ois);
	}
	
	/**
	* Called when disconnecting
	*/
	public static void onDisconnect() {
		netThread = null;
	}
}