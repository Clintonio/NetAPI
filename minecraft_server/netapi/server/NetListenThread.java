package netapi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

import netapi.packet.UsernamePacket;

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
	private Logger log = Logger.getLogger("Minecraft");
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
	public NetListenThread(ServerSocket sock) {
		log.info("(NetAPI) NetAPI Server Started");
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
		log.info("(NetAPI) Finding the name of the connected user");
		log.info("(NetAPI) Creating input stream");
		
		try {
			ObjectInputStream  ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			log.info("(NetAPI) Attempting to read name packet");
			String username = getUsername(ois);
			
			if(username instanceof String) {
				assignThread.assign(username, socket, ois);
			} else {
				log.info("(NetAPI) No username, disconnecting user");
			}
		} catch (IOException e) {
			log.info("(NetAPI) Input stream failed; " + e.getMessage());
		}
	}
	
	/**
	* Get the username packet from a player and return
	* the username
	*
	* @since	0.1
	* @return	A found username
	*/
	private String getUsername(ObjectInputStream  ois) {
		try {
			Object in = ois.readObject();
			
			if(in instanceof UsernamePacket) {
				UsernamePacket u = (UsernamePacket) in;
				log.info("(NetAPI) Username: " + u.username + " found");
				return u.username;
			} else if(in instanceof Object) {
				log.info("(NetAPI) Received: " + in.getClass().getName());
			} else {
				log.info("(NetAPI) Received null");
			}
		} catch (IOException e) {
			log.info("(NetAPI) IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			log.info("(NetAPI) Could not find class: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	* Run the thread and start accepting clients
	*
	* @since	0.1
	*/
	public void run() {
		log.info("(NetAPI) Listening for NetAPI connections");
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