package netapi.client;

import netapi.packet.UsernamePacket;

import net.minecraft.src.NetworkManager;

import java.net.Socket;
import java.net.InetAddress;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
* This class handles the connection of the NetAPI
* client to the NetAPI server
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class NetConnectThread extends Thread {
	private NetworkManager 	netManager;
	private InetAddress		addr;
	private int				port;
	private String			user;
	
	/**
	* Create the connection thread with the given address
	*
	* @since	0.1
	* @throws	IOException	When a connection fails
	* @param	addr	Address to connect
	* @param	port	Port to connect 
	* @param	user	User connecting
	*/
	public NetConnectThread(NetworkManager netManager, 
							InetAddress addr, int port, String user) {
		this.addr 			= addr;
		this.netManager		= netManager;
		this.port			= port;
		this.user			= user;
	}
		
	/**
	* Create and connect the user
	*
	* @since	0.1
	*/
	public void run() {
		try {
			// Create second, identical, port for sending data for the
			// NetAPI data
			System.out.println("(NetAPI) Connecting to " + addr + ":" + port);
			Socket netAPISocket = new Socket(addr, port);
			System.out.println("(NetAPI) Creating output stream");
			ObjectOutputStream oos = new ObjectOutputStream(netAPISocket.getOutputStream());
			System.out.println("(NetAPI) Authenticating with username packet");
			sendUsernamePacket(netAPISocket, oos, user);	
			System.out.println("(NetAPI) Setting network management");
			netManager.setNetAPISocket(netAPISocket, oos);
		} catch (IOException e) {
			System.out.println("(NetAPI) Connection failed: " + e.getMessage());
		}			
	}
	
	/**
	* Send the username packet separate to the other 
	* packets
	*
	* @throws	IOException	If something fails
	* @param	socket		Socket to use
	* @param	username	NAme of user to send
	*/
	private void sendUsernamePacket(Socket socket, ObjectOutputStream oos, String username)
		throws IOException {
		// Login with username 
		System.out.print("(NetAPI) Logging into server with username " + username + " ...");
		oos.writeObject(new UsernamePacket(username));
		System.out.println(" connected");
		oos.flush();
	}
}