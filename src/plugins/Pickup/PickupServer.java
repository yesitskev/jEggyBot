// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PICKUPSERVER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.Pickup;

import java.io.Serializable;

public class PickupServer implements Serializable
{
	// ********************************************************************************
	//			DECLARATIONS
	// ********************************************************************************
	
	private String tag;
	private String ip;
	private int port;
	private String password;
	
	// ********************************************************************************
	//			CONSTRUCTOR
	// ********************************************************************************
	
	public PickupServer(String tag, String ip, int port, String password)
	{
		this.tag = tag;
		this.ip = ip;
		this.port = port;
		this.password = password;
	}
	
	// ********************************************************************************
	//			ACCESSOR +	MUTATOR	METHODS
	// ********************************************************************************
	
	public String getTag()
	{
		return tag;
	}
	
	public String getIP()
	{
		return ip;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String getAddress()
	{
		return ip + ":" + port;
	}
}