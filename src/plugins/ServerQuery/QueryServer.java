// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								QUERYSERVER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.ServerQuery;

import jeggybot.util.CachedResult;

import modules.serverquery.*;
import modules.serverquery.games.*;

import java.util.ArrayList;

public class QueryServer
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private GameServer server;				// server instance
	private String address;					// remote address
	private int port;						// port
	private int protocol;					// server/game protocol
	private CachedResult cPlayers;			// cached player list
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public QueryServer(String address, int port, String protocol)
	{
		this.address = address;
		this.port = port;
		// set game protocol
		this.protocol = PROTOCOL.getProtocol(protocol);
	}
	
	// ********************************************************************************
    //          ACCESSOR +	MUTATOR	METHODS
    // ********************************************************************************
	
	public String getRemoteAddress()
	{
		return address;
	}
	
	public int getRemotePort()
	{
		return port;
	}
	
	public int getProtocol()
	{
		return protocol;
	}
	
	// ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************
	
	public ServerDetails getServerDetails()
	{
		switch (protocol) {
			case PROTOCOL.GOLDSRC:
				return GoldSourceServer.getServerDetails(address, port);
			case PROTOCOL.SRC:
				return SourceServer.getServerDetails(address, port);
			case PROTOCOL.Q3:
				return Quake3Server.getServerDetails(address, port);
			default:
				return null;
		}
	}
	
	public ArrayList getPlayerList()
	{
		if ((cPlayers != null) && (cPlayers.getTimeDifference() < 120)) {
			// return cached result
			return (ArrayList) cPlayers.getResult();
		}
		else {
			// fetch player list from server
			ArrayList players;
			switch (protocol) {
				case PROTOCOL.GOLDSRC:
					players = GoldSourceServer.getPlayerList(address, port);
					break;
				case PROTOCOL.SRC:
					players = SourceServer.getPlayerList(address, port);
					break;
				case PROTOCOL.Q3:
					players = Quake3Server.getPlayerList(address, port);
					break;
				default:
					return null;
			}
			// cache player list
			cPlayers = new CachedResult(players);
			return players;
		}
	}
	
	public long getPing()
	{
		switch (protocol) {
			case PROTOCOL.GOLDSRC:
				return GoldSourceServer.getPing(address, port);
			case PROTOCOL.SRC:
				return SourceServer.getPing(address, port);
			case PROTOCOL.Q3:
				return Quake3Server.getPing(address, port);
			default:
				return -1;
		}
	}
}