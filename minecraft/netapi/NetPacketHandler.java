package netapi;

import netapi.packet.NetPacket;

/**
* A handler that is executed every time
* a packet of a certain type is received
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public interface NetPacketHandler {
	/**
	* Handle a single packet being received
	*
	* @since	0.1
	* @param	packet		Received packet
	*/
	public void handle(NetPacket packet);
}