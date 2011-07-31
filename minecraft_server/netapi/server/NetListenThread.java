package netapi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.SocketException;

import net.minecraft.server.MinecraftServer;

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
	*
	* @since	0.1
	*/
	private ServerSocket netAPISocket;
	/**
	* The thread for sending accepted players for validation
	*
	* @since	0.1
	*/
	private NetAssignThread	assignThread;
	/**
	* The minecraft server instance
	*
	* @since	0.1
	*/
	private MinecraftServer mcServer;
	/**
	* True while alive
	*
	* @since	0.1
	*/
	private boolean alive = true;
	
	/**
	* Start a new net listen thread
	*
	* @since	0.1
	*/
	public NetListenThread(ServerSocket sock, MinecraftServer server) {
		server.logger.warning("(NetAPI) NetAPI Server Started");
		mcServer 		= server;
		netAPISocket	= sock;
		assignThread	= new NetAssignThread(server);
		
		try {
			netAPISocket.setSoTimeout(150);
		} catch (SocketException e) {
			// Ignore
		}
	}
	
	/**
	* Process a given user
	*
	* @since	0.1
	* @param	socket		Socket being accepted
	*/
	private void processUser(Socket socket) {	
		mcServer.logger.warning("(NetAPI) Finding the name of the connected user");
		String username = getUsername(socket);
		
		if(username instanceof String) {
			assignThread.assign(username, socket);
		} else {
			mcServer.logger.warning("(NetAPI) No username, disconnecting user");
		}
	}
	
	/**
	* Get the username packet from a player and return
	* the username
	*
	* @since	0.1
	* @return	A found username
	*/
	private String getUsername(Socket socket) {
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
			
			ois.reset();
		} catch (IOException e) {
			mcServer.logger.warning("(NetAPI) IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			mcServer.logger.warning("(NetAPI) Could not find class: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	* Run the thread and start accepting clients
	*
	* @since	0.1
	*/
	public void run() {
		mcServer.logger.warning("(NetAPI) Listening for NetAPI connections");
		assignThread.start();
		while(alive) {
			try {
				Socket socket = netAPISocket.accept();
				processUser(socket);
				Thread.sleep(150);
			} catch (IOException e) {
				// Intended. I dislike using exceptions as control flow though.
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
		assignThread.stop();
	}
}