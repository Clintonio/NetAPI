package netapi.packet;/*** A packet containing only a string ** @author	Clinton Alexander* @version	0.1* @since	0.1*/public class StringPacket extends NetPacket {	/**	* The data we are sending	*	* @since	0.1	*/	private String	data;		/**	* Create a string packet with no mod id	*	* @since	0.1	* @param	data	Data to send	*/	public StringPacket(String data) {		super();		this.data = data;	}		/**	* Create a string packet for a mod with the given ID	*	* @since	0.1	* @param	data	Data to send	* @param	id		Mod ID	*/	public StringPacket(String data, String id) {		super(id);		this.data = data;	}		/**	* Get the data from this packet	*	* @since	0.1	* @return	This packet's data	*/	public String getData() {		return data;	}}