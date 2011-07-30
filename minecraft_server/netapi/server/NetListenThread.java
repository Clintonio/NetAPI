package netapi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.EntityPlayerMP;

import netapi.UsernamePacket;

/**
* The thread for letting netapi servers listen to netapi connections
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class NetListenThread extends Thread {
	/**
	* Current server socket
	*/
	private ServerSocket netAPISocket;
	/**
	* The minecraft server instance
	*/
	private MinecraftServer mcServer;
	/**
	* Current player pool
	*/
	private Hashtable<String, Socket> playerTable = new Hashtable<String, Socket>();
	/**
	* True while alive
	*/
	private boolean alive = true;
	
	/**
	* Start a new net listen thread
	*/
	public NetListenThread(ServerSocket sock, MinecraftServer server) {
		server.logger.warning("(NetAPI) NetAPI Server Started");
		mcServer 		= server;
		netAPISocket	= sock;
	}
	
	/**
	* Process a given user
	*
	* @param	socket		Socket being accepted
	*/
	private void processUser(Socket socket) {	
		mcServer.logger.warning("(NetAPI) Finding the name of the connected user");
		String username = getUsername(socket);
		
		if(username instanceof String) {
			mcServer.logger.warning("(NetAPI) Added user " + username + " to player table");
			playerTable.put(username, socket);
		} else {
			mcServer.logger.warning("(NetAPI) No username, disconnecting user");
		}
	}
	
	/**
	* Update and attempt to assign all users
	*/
	private void updateAllUsers() {
		EntityPlayerMP 	player;
		String 			name;
		Socket 			sock;
		// Scan over each current attempted login 
		// If the login matches a player, st them up
		// to be able to send net commands.
		for(Map.Entry<String, Socket> entry : playerTable.entrySet()) {
			name = entry.getKey();
			sock = entry.getValue();
			if((player = getPlayer(name)) != null) {
				NetworkManager netMan = player.playerNetServerHandler.netManager;
				// Check if they are from same address, if not, remove the
				// player in case of a mix up/ hack (n.b: this is integrity code)
				if(netMan.getSocketAddress() == sock.getInetAddress()) {
					try {
						netMan.setNetAPISocket(sock);
					} catch (IOException e) {
						System.out.println("(NetAPI) Could not connect player " + name);
					}
				} 
				playerTable.remove(name);
			}
		}
	}
	
	/**
	* Get a player from the pool by name
	*
	* @param	name		Name of player
	*/
	public EntityPlayerMP getPlayer(String name) {
		return mcServer.configManager.getPlayerEntity(name);
	}
	
	/**
	* Get the username packet from a player and return
	* the username
	*
	* @return	A found username
	*/
	public String getUsername(Socket socket) {
		try {
			mcServer.logger.warning("(NetAPI) Creating input stream");
			ObjectInputStream  ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			mcServer.logger.warning("(NetAPI) Attempting to read name packet");
			Object in = ois.readObject();
			
			if(in instanceof UsernamePacket) {
				UsernamePacket u = (UsernamePacket) in;
				mcServer.logger.warning("(NetAPI) Username: " + u.username + " found");
				return u.username;
			} else if(in instanceof Object) {
				mcServer.logger.warning("(NetAPI) Received: " + in.getClass().getName());
			} else {
				mcServer.logger.warning("(NetAPI) Received null");
			}
			
			ois.close();
		} catch (IOException e) {
			mcServer.logger.warning("(NetAPI) IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			mcServer.logger.warning("(NetAPI) Could not find class: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	* Run the thread and start accepting clients
	*/
	public void run() {
		mcServer.logger.warning("(NetAPI) Listening for NetAPI connections");
		while(alive) {
			try {
				Socket socket = netAPISocket.accept();
				
				processUser(socket);
				updateAllUsers();
				
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// Psh
				}
				netAPISocket.close();
			} catch (Exception e) {
				// Don't care. Probably not important
			}
		}
	}
	
	/** 
	* Stop the thread
	*/
	public void stopThread() {
		alive = false;
	}
}