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
	private int		timestamp;
	
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
	public int getTimestamp() {
		return timestamp;
	}
	
	/**
	* Get the modID for this packet
	*
	* @since	0.1
	* @return	Mod ID for this packet
	*/
	public int getModID() {
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