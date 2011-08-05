package netapi.server;

import netapi.NetAPI;

import java.util.concurrent.ConcurrentHashMap;
import java.net.Socket;
import java.util.Map;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetworkManager;

/**
* NetAPI thread for assigning joining players
* to their NetAPI socket
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class NetAssignThread extends Thread {
	/**
	* Current player pool
	* Index 0 = Socket
	* Index 1 = Object input stream. 
	* Hacked due to Java's lack of tuples
	*
	* @since	0.1
	*/
	private ConcurrentHashMap<String, Object[]> playerTable = new ConcurrentHashMap<String, Object[]>();
	/**
	* True while alive
	*
	* @since	0.1
	*/
	private boolean alive = true;
	
	/**
	* Create a new net accept 
	*
	* @since	0.1
	* @param	server	Server instance
	*/
	public NetAssignThread() {
	}
	
	/**
	* Assign a player with given username and socket
	*
	* @since	0.1
	* @param	username	Username we are adding
	* @param	socket		Socket for given username
	* @param	ois			Object input stream for the given
	* 						user and socket
	*/
	public void assign(String username, Socket socket, ObjectInputStream ois) {
		Object[] store = { socket, ois };
		playerTable.put(username, store);
	}
	
	/**
	* Update and attempt to assign all users
	*
	* @since	0.1
	*/
	private void updateAllUsers() {
		// Scan over each current attempted login 
		// If the login matches a player, st them up
		// to be able to send net commands.
		for(Map.Entry<String, Object[]> entry : playerTable.entrySet()) {
			EntityPlayerMP		player;
			String				name 	= entry.getKey();
			Object[] 			store 	= entry.getValue();
			Socket				sock	= (Socket) store[0];
			ObjectInputStream	ois		= (ObjectInputStream) store[1];
			
			NetAPI.log.info("(NetAPI) Checking player " + name);
			if((player = NetAPI.getPlayer(name)) != null) {
				addNewPlayer(player, name, sock, ois);
				playerTable.remove(name);
			}
		}
	}
	
	/**
	* Add a new user to the player list if they are valid
	*
	* @since	0.1
	* @param	name		Name of player
	* @param	sock		Socket connecting to
	* @param	ois			Input stream for player
	*/
	private void addNewPlayer(EntityPlayerMP player, String name, 
							  Socket sock, ObjectInputStream ois) {
		NetworkManager netMan = player.playerNetServerHandler.netManager;
		
		// Check if they are from same address, if not, remove the
		// player in case of a mix up/ hack (n.b: this is integrity code)
		if(netMan.getSocketAddress().equals(sock.getInetAddress())) {
			NetAPI.log.info("(NetAPI) Authenticated " + name);
			try {
				netMan.setUsername(name);
				netMan.setNetAPISocket(sock, ois);
			} catch (IOException e) {
				System.out.println("(NetAPI) Could not connect player " + name);
			}
		} 
	}
	
	/**
	* Run the thread and start accepting clients
	*
	* @since	0.1
	*/
	public void run() {
		NetAPI.log.info("(NetAPI) User assignment starting");
		while(alive) {
			updateAllUsers();
			
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// Psh
			}
		}
	}
	
	/** 
	* Stop the thread
	*
	* @since	0.1
	*/
	public void stopThread() {
		alive = false;
	}
}