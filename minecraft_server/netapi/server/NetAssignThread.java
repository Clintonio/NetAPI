package netapi.server;

import netapi.NetAPI;
import netapi.NetPacketThread;

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
	* Hashmap of player objects
	*
	* @since	0.1
	*/
	private ConcurrentHashMap<String, NetPlayer> playerTable = new ConcurrentHashMap<String, NetPlayer>();
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
	
	//=====================
	// Updaters
	//=====================
	
	/**
	* Update and attempt to assign all users
	*
	* @since	0.1
	*/
	private void updateAllUsers() {
		// Scan over each current attempted login 
		// If the login matches a player, st them up
		// to be able to send net commands.
		for(Map.Entry<String, NetPlayer> entry : playerTable.entrySet()) {
			EntityPlayerMP		player;
			String				name 	= entry.getKey();
			NetPlayer			store	= entry.getValue();
			
			if(store.player == null) {				
				NetAPI.log.info("(NetAPI) Checking player " + name);
				if((player = NetAPI.getPlayer(name)) != null) {
					store.player = player;
					addNewPlayer(store);
				}
			}
		}
	}
	
	//=====================
	// Player Management
	//=====================
	
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
		NetPlayer store = new NetPlayer();
		store.socket 	= socket;
		store.ois		= ois;
		playerTable.put(username, store);
	}
	
	/**
	* Add a new user to the player list if they are valid
	*
	* @since	0.1
	* @param	name		Name of player
	* @param	sock		Socket connecting to
	* @param	ois			Input stream for player
	*/
	private void addNewPlayer(NetPlayer player) {
		NetworkManager netMan = player.player.playerNetServerHandler.netManager;
		
		// Check if they are from same address, if not, remove the
		// player in case of a mix up/ hack (n.b: this is integrity code)
		if(player.socket.getInetAddress().equals(player.socket.getInetAddress())) {
			String username = player.player.username;
			NetAPI.log.info("(NetAPI) Authenticated " + username);
			try {
				netMan.setUsername(username);
				NetAPI.log.info("(NetAPI) Creating packet threads");
				player.sendThread 		= NetAPI.getNewNetThread(player.socket, username, true);
				player.receiveThread	= NetAPI.getNewNetThread(player.socket, username, player.ois);
				
				NetAPI.log.info("(NetAPI) Starting packet threads");
				player.sendThread.start();
				player.receiveThread.start();
			} catch (IOException e) {
				NetAPI.log.warning("(NetAPI) Could not connect player: " + username + ": " + e.getMessage());
			}
		}
	}
	
	/**
	* Disconnect a player by name
	*
	* @since	0.1
	* @param	username	Name of player
	*/
	public void playerDisconnected(String username) {
		if(playerTable.containsKey(username)) {
			NetAPI.log.info("(NetAPI) Disconnecting user " + username);
			NetPlayer store = playerTable.get(username);
			
			try {
				store.socket.close();
			} catch (IOException e) { }
			store.sendThread.stopThread();
			store.receiveThread.stopThread();
			
			playerTable.remove(username);
		}
	}
	
	//=====================
	// Thread Management
	//=====================
	
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
		
		for(Map.Entry<String, NetPlayer> entry : playerTable.entrySet()) {
			NetPlayer store = entry.getValue();
			
			try {
				store.socket.close();
			} catch (IOException e) { }
			store.sendThread.stopThread();
			store.receiveThread.stopThread();
		}
		
		playerTable = null;
	}

	/**
	* NetAPI player details
	* No encapsulation since it's just a convenience class
	*
	* @author	Clinton Alexander
	* @version	0.1
	* @since	0.1
	*/
	private class NetPlayer {
		public EntityPlayerMP		player;
		public Socket 				socket;
		public NetPacketThread		sendThread;
		public NetPacketThread		receiveThread;
		
		public ObjectInputStream 	ois;
	}
}