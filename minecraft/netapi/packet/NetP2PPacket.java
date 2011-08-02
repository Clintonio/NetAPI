package netapi.packet;

/**
* A packet explicitly for sending data from one player
* to another. Not truly P2P, but behaves as such, and is
* safely verified by the server.
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class NetP2PPacket extends NetPacket {
	private String[] 	recipients;
	private NetPacket 	payload;
	
	/**
	* Create a packet with the given user as the receipient
	*
	* @since	0.1
	* @param	username	Player to send to
	* @param	packet		Packet to send
	*/
	public NetP2PPacket(String username, NetPacket packet) {
		this(username, packet, "");
	}
	
	/**
	* Create a packet with the given users as the recipeints
	*
	* @since	0.1
	* @param	recipients	Players to send to
	* @param	packet		Packet to send
	*/
	public NetP2PPacket(String[] recipients, NetPacket packet) {
		super();
		this.recipients = recipients;
		payload = packet;
	}
	
	/**
	* Create a P2P packet with a mod ID
	*
	* @since	0.1
	* @param	username	Player to send to
	* @param	packet		Packet to send
	* @param	id			Mod ID
	*/
	protected NetP2PPacket(String username, NetPacket packet, String id) {
		super(id);
		recipients 		= new String[1];
		recipients[0] 	= username;
		payload 		= packet;
	}
	
	/**
	* Get the users to send the packet to 
	*
	* @return	Recipients
	*/
	public String[] getRecipients() {
		return recipients;
	}
	
	/**
	* Get the packet to send
	*
	* @since	0.1
	* @return	Packet to send
	*/
	public NetPacket getPayload() {
		return payload;
	}
}