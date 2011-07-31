package netapi;

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
	private String		sender;
	
	/**
	* Create a packet with the given user as the receipient
	*
	* @since	0.1
	* @param	username	Player to send to
	* @param	packet		Packet to send
	*/
	public NetP2PPacket(String username, NetPacket packet) {
		recipients 		= new String[1];
		recipients[0] 	= username;
		payload 		= packet;
	}
	
	/**
	* Create a packet with the given users as the recipeints
	*
	* @since	0.1
	* @param	recipients	Players to send to
	* @param	packet		Packet to send
	*/
	public NetP2PPacket(String[] recipients, NetPacket packet) {
		this.recipients = recipients;
		payload = packet;
	}
	
	/**
	* Set the sender, which occurs on the server
	*
	* @since	0.1
	* @param	sender		Sender of this packet
	*/
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	/**
	* Get the sender
	*
	* @return	Sender
	*/
	public String getSender() {
		return sender;
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