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
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

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
	* The packet threads for sending packets
	*
	* @since	0.1
	*/
	private static Hashtable<String, NetPacketThread> netSendThreads = new Hashtable<String, NetPacketThread>();
	/**
	* The logger for netAPI 
	*
	* @since	0.1
	*/
	public static final Logger log = Logger.getLogger("Minecraft");
	/**
	* The minecraft server instances for the vanilla netAPI
	*
	* @since	0.1
	*/
	private static MinecraftServer server;
	
	//===================
	// Packet methods
	//===================
	
	/**
	* Send a given packet to all players
	*
	* @param	packet		The packet to send
	*/
	public static void sendPacket(NetPacket packet) {
		for(Map.Entry<String, NetPacketThread> entry : netSendThreads.entrySet()) {
			entry.getValue().send(packet);
		}
	}
		
	/**
	* Sends a packet to a specific player
	*
	* @param	packet		The packet to send
	* @param	player		Player to send packet to
	*/
	public static void sendPacketToPlayer(NetPacket packet, EntityPlayer player) {
		String username = player.username;
		NetPacketThread t; 
		// Check if the user exists
		if((username != null) && ((t = netSendThreads.get(username)) != null)) {
			t.send(packet);
		}
	}
	
	/**
	* Send a packet to many players
	*
	* @param	packet		The packet to send
	* @param	players		The players to send the packet to
	*/
	public static void sendPacketToPlayers(NetPacket packet, EntityPlayer[] players) {
		for(int x = 0; x < players.length; x++) {
			sendPacketToPlayer(packet, players[x].username);
		}
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
	* Sends a packet to a specific player
	*
	* @param	packet		The packet to send
	* @param	username	Player to send packet to
	*/
	public static void sendPacketToPlayer(NetPacket packet, String username) {
		NetPacketThread t; 
		// Check if the user exists
		if((username != null) && ((t = netSendThreads.get(username)) != null)) {
			t.send(packet);
		}
	}
	
	/**
	* Send a packet to many players
	*
	* @param	packet		The packet to send
	* @param	players		The players to send the packet to
	*/
	public static void sendPacketToPlayers(NetPacket packet, String[] players) {
		for(int x = 0; x < players.length; x++) {
			sendPacketToPlayer(packet, players[x]);
		}
	}
	
	/**
	* Send a P2P packet to many players
	*
	* @param	packet		The packet to send
	*/
	public static void sendPacketToPlayers(NetP2PPacket packet) {
		String[] 	recipients = packet.getRecipients();
		String		username;
		NetPacketThread t; 
		
		for(int x = 0; x < recipients.length; x++) {
			username = recipients[x];
			// Check if the user exists
			if((username != null) && ((t = netSendThreads.get(username)) != null)) {
				t.send(packet);
			}
		}
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
	// Server API only
	//============
	
	/**
	* Get a player by their username
	*
	* @since	0.1
	* @param	username	Player's username
	* @return	Player object
	*/
	public static EntityPlayerMP getPlayer(String username) {
		return server.configManager.getPlayerEntity(username);
	}
	
	//============
	// Operation methods. Not for API
	//============
	
	/**
	* Create and set a new net thread when connecting
	* to a new server. Not a part of the API.
	*
	* @since	0.1
	* @throws	IOException	When the thread could not connect the data streams
	* @param	socket		Socket we have connected to
	* @param	username	Username that is requesting new packet threads
	* @param	mode		True if the thread is a sender thread, false if receiver
	*/
	public static NetPacketThread getNewNetThread(Socket socket, String username, boolean mode) 
		throws IOException {
		if(mode) {
			NetPacketThread netThread = new NetPacketThread(socket, mode);
			netThread.setSenderName(username);
			netSendThreads.put(username, netThread);
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
	* @param	socket		Socket connected to
	* @param	oos			Data sending stream
	* @param	username	Username that is sending new packet threads
	*/
	public static NetPacketThread getNewNetThread(Socket socket, String username, ObjectOutputStream oos) {
		NetPacketThread netThread = new NetPacketThread(socket, oos);
		netThread.setSenderName(username);
		netSendThreads.put(username, netThread);
		return netThread;
	}
	
	/**
	* Create and set a new net thread when connecting
	* to a new server. Not a part of the API.
	*
	* @since	0.1
	* @param	socket		Socket connected to
	* @param	ois			Data sending stream
	* @param	username	Username that is receiving new packet threads
	*/
	public static NetPacketThread getNewNetThread(Socket socket, String username, ObjectInputStream ois) {
		NetPacketThread netThread = new NetPacketThread(socket, ois);
		netThread.setSenderName(username);
		return netThread;
	}
	
	/**
	* On a player disconnect, delete this player's entry
	*
	* @since	0.1
	* @param	username	Player who is disconnecting
	*/
	public static void playerDisconnected(String username) {
		if(netSendThreads.containsKey(username)) {
			netSendThreads.remove(username);
		}
	}
	
	/**
	* Set the server instance
	*
	* @since	0.1
	* @param	server	Server instance
	*/
	public static void setServer(MinecraftServer server) {
		if(NetAPI.server != null) {
			throw new RuntimeException("Don't re-set this value");
		} else {
			NetAPI.server = server;
		}
	}
}