package netapi.packet;

import java.io.Serializable;
import java.util.Date;

/**
* A single net packet for sending data to the MC server
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public abstract class NetPacket implements Serializable { 
	/**
	* ID of the mod that has sent this packet.
	* Note: This "ID" is not enforced internally.
	* 
	* @since	0.1
	*/
	private String modID = "";
	/**
	* Timestamp that this packet was sent at
	*
	* @since	0.1
	*/
	private long		timestamp;
	/**
	* The sender of the packet, set on the
	* server
	*
	* @since	0.1
	*/
	private String		sender		= "";
	
	/**
	* Create a standard packet
	*
	* @since	0.1
	*/
	protected NetPacket() {
		timestamp = (new Date()).getTime();
	}
	
	/**
	* Create a packet with a mod ID 
	*
	* @since	0.1
	* @param	id		Mod ID
	*/
	protected NetPacket(String id) {
		this();
		if(id != null) {
			modID = id;
		}
	}
	
	/**
	* Get the timestamp for this packet
	*
	* @since	0.1
	* @return	Timestamp this packet was created
	*/
	public long getTimestamp() {
		return timestamp;
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
	* Get the modID for this packet
	*
	* @since	0.1
	* @return	Mod ID for this packet
	*/
	public String getModID() {
		return modID;
	}	
	
	/**
	* Check whether this mod has an ID attached
	*
	* @since	0.1
	* @return	true if this mod has a specific ID attached
	*/
	public boolean hasModID() {
		return (modID != "");
	}
}