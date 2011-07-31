package netapi.packet;

/**
* A username packet to allow users
* to send their current username to login
* to the service
*
* @author	Clinton Alexander
* @version	0.1
* @since	0.1
*/
public class UsernamePacket extends NetPacket {
	public String username;
	
	/**
	* Create a new username packet
	*
	* @param	name		Username to send
	*/
	public UsernamePacket(String name) {
		super();
		username = name;
	}
}