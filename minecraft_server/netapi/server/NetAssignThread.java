package netapi.server;

import java.util.concurrent.ConcurrentHashMap;
import java.net.Socket;
import java.util.Map;
import java.io.IOException;
import java.net.SocketException;

import net.minecraft.server.MinecraftServer;
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
	*
	* @since	0.1
	*/
	private ConcurrentHashMap<String, Socket> playerTable = new ConcurrentHashMap<String, Socket>();
	/**
	* True while alive
	*
	* @since	0.1
	*/
	private boolean alive = true;
	/**
	* Minecraft server instance primarily for logging
	*
	* @since	0.1
	*/
	private MinecraftServer mcServer;
	
	/**
	* Create a new net accept 
	*
	* @since	0.1
	* @param	server	Server instance
	*/
	public NetAssignThread(MinecraftServer server) {
		mcServer = server;
	}
	
	/**
	* Assign a player with given username and socket
	*
	* @since	0.1
	* @param	username	Username we are adding
	* @param	socket		Socket for given username
	*/
	public void assign(String username, Socket socket) {
		playerTable.put(username, socket);
	}
	
	/**
	* Get a player from the pool by name
	*
	* @since	0.1
	* @param	name		Name of player
	*/
	private EntityPlayerMP getPlayer(String name) {
		return mcServer.configManager.getPlayerEntity(name);
	}
	
	/**
	* Update and attempt to assign all users
	*
	* @since	0.1
	*/
	private void updateAllUsers() {
		//mcServer.logger.warning("(NetAPI) Updating all users...");
		EntityPlayerMP 	player;
		String 			name;
		Socket 			sock;
		// Scan over each current attempted login 
		// If the login matches a player, st them up
		// to be able to send net commands.
		for(Map.Entry<String, Socket> entry : playerTable.entrySet()) {
			name = entry.getKey();
			sock = entry.getValue();
			mcServer.logger.warning("(NetAPI) Checking player " + name);
			if((player = getPlayer(name)) != null) {
				mcServer.logger.warning("(NetAPI) Found user " + name + " in player list and table");
				NetworkManager netMan = player.playerNetServerHandler.netManager;
				mcServer.logger.warning("(NetAPI) " + netMan.getSocketAddress() + " == " + sock.getInetAddress());
				// Check if they are from same address, if not, remove the
				// player in case of a mix up/ hack (n.b: this is integrity code)
				if(netMan.getSocketAddress().equals(sock.getInetAddress())) {
					mcServer.logger.warning("(NetAPI) Found full user, adding to net manager");
					try {
						netMan.setUsername(name);
						netMan.setNetAPISocket(sock);
					} catch (IOException e) {
						System.out.println("(NetAPI) Could not connect player " + name);
					}
				} 
				playerTable.remove(name);
			}
		}
		//mcServer.logger.warning("(NetAPI) Finished updating all users");
	}
	
	/**
	* Run the thread and start accepting clients
	*
	* @since	0.1
	*/
	public void run() {
		mcServer.logger.warning("(NetAPI) User assignment starting");
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