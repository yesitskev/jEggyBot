// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								SERVER QUERY
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.ServerQuery;

import jeggybot.*;

import jeggybot.util.*;

import jirc.User;

import modules.serverquery.*;
import modules.serverquery.games.*;

import java.util.*;

import java.sql.*;

public class plgServerQuery extends Plugin
{	
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private	HashMap<String, QueryServer> servers;			// server list
	private HashMap<String, String> triggers;				// server query triggers
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgServerQuery()
	{
		// initialisation
		servers = new HashMap<String, QueryServer>();
		triggers = new HashMap<String, String>();
		// set plugin information
		name = "Server Query";
		author = "Matthew Goslett";
		version = "0.1";
		// register plugin commands
		ph.register("!q", "cmdQ", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!qf", "cmdQF", ACCESS.CHANNEL, LEVEL.USER, this);
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onRehash() {
				onRehashEvent();
			}
			public void onMessage(User user, String target, String message) {
				onMessageEvent(user, target, message);
			}
		});
		// load query servers
		if (!loadServers()) {
			bot.log("I could not load the game query server list");
		}
		// load server query triggers
		triggers.put("!cspickup1", "196.4.79.18:27016");
		triggers.put("!cspickup2", "196.4.79.10:27016");
		triggers.put("!cspickup3", "196.4.79.69:27016");
	}
	
	// ********************************************************************************
	//			PRIVATE METHODS
	// ********************************************************************************
    
    private boolean loadServers()
    {
    	// clear server list
    	servers.clear();
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve server list from database
			PreparedStatement ps = db.prepareStatement("SELECT * FROM " + bot.s("TABLE-PREFIX") + "queryservers");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String ip = rs.getString("ip");
				int port = rs.getInt("port");
				String protocol = rs.getString("protocol");
				// create server type based on game protocol
				QueryServer server = new QueryServer(ip, port, protocol);
				// add server to server list
				servers.put(ip.toLowerCase() + ":" + port, server);
			}
			// close database connection
			db.close();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
    }
    
	private boolean isServer(String address)
	{
		return servers.containsKey(address.toLowerCase());
	}

    private HashMap getMatchingPlayers(String name)
    {
		// create regex pattern from player name
		String pattern = name;
		// escape special regex characters
		String chars[] = new String[]{"\\", "$", "^", ".", "*", "+", "?", "[", "]", "{", "}", "(", ")", "|"};
		for (int x = 0; x < chars.length; x++) {
			pattern = pattern.replace(chars[x], "\\" + chars[x]);
		}
		pattern = "(?i).*(" + pattern + ").*";
		// create empty result hashset
		HashMap results = new HashMap();
		// search for player on each server
		Iterator it = (servers.values()).iterator();
		while (it.hasNext()) {
			QueryServer server = (QueryServer) it.next();
			// create matches arraylist for this server
			ArrayList matches = new ArrayList<String>();
			// get player list on this server
			ArrayList<Player> players = server.getPlayerList();
			if (players != null) {
				// loop through each player and check if nickname is a match
				for (Player player: players) {
					if (player.getName().matches(pattern)) {
						matches.add(player.getName());
					}
				}
				// add matches for this server to total results
				if (!matches.isEmpty()) {
					results.put(server, matches);
				}
			}
		}
		return results;
    }
    
    private int getMatchingCount(HashMap results)
    {
    	// count total number of matching players on all servers
    	int nPlayers = 0;
    	Iterator it = (results.values()).iterator();
		while (it.hasNext()) {
			ArrayList players = (ArrayList) it.next();
			nPlayers += players.size();
		}
		return nPlayers;
    }

	// ********************************************************************************
	//			IRC EVENTS
	// ********************************************************************************

	private void onRehashEvent()
	{
		// re-load server list
		if (!loadServers()) {
			bot.log("An error occured whilst re-loading the game query server list");
		}
	}
    
    private void onMessageEvent(User user, String target, String message)
    {
    	if (target.startsWith("#")) {
			// check if message is a trigger
			String trigger = message.toLowerCase();
			if (triggers.containsKey(trigger)) {
				// get server address which belongs to trigger
				String address = triggers.get(trigger);
				// prepare !q command parameters
				MessageInfo m = new MessageInfo(user, target, message);
				cmdQ(m, new String[]{address});
			}	
    	}
    }
    
	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
    
    public void cmdQ(final MessageInfo m, final String[] p)
    {
		new Thread() {
			public void run() {
		    	if (p.length == 1) {
					String address = p[0];
					if (isServer(address)) {
						// get instance of query server
						QueryServer server = servers.get(address.toLowerCase());	
						// get server details
						ServerDetails sd = server.getServerDetails();	
						if (sd != null) {
							// format response
							String format = bot.s("QUERY-FORMAT");
							format = format.replace("$ip", server.getRemoteAddress());
							format = format.replace("$port", String.valueOf(server.getRemotePort()));
							format = format.replace("$server", sd.getServerName());
							format = format.replace("$map", sd.getMap());
							format = format.replace("$game", sd.getGameName());
							format = format.replace("$players", String.valueOf(sd.getPlayerCount()));
							format = format.replace("$maxplayers", String.valueOf(sd.getMaxPlayers()));
							// message response
							bot.msg(m.getTarget(), format);
						}
						else {
							bot.msg(m.getTarget(), "14I could not query " + address + "");
						}
					}
					else {
						bot.notice(m.getNickname(), "" + address + " is not in my query server list");
					}
		    	}
		    	else {
		    		bot.notice(m.getNickname(), "Syntax: !q (ip:port)");
		    	}
			}
		}.start();
    }
    
    public void cmdQF(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String player = Utils.arrayToString(p);
    		// find matching player names on all servers
    		HashMap<QueryServer, ArrayList> results = getMatchingPlayers(player);
			// get number of matches found
			int nMatches = getMatchingCount(results);
			if (nMatches > 6) {
				bot.msg(m.getTarget(), "14I found too many matching players with the name *" + player + "*");
			}
			else if (nMatches > 0) {
				Iterator it = (results.keySet()).iterator();
				while (it.hasNext()) {
					QueryServer server = (QueryServer) it.next();
					ArrayList<String> matches = (ArrayList) results.get(server);
					// create a single list of players found on this server
					String list = "";
					for (String match: matches) {
						list += "" + match + ", ";
					}
					list = list.substring(0, list.length() - 2);
					// set server name
					ServerDetails sd = server.getServerDetails();
					String name;
					if (sd != null) {
						name = sd.getServerName() + " (" + server.getRemoteAddress() + ":" + server.getRemotePort() + ")";
					}
					else {
						name = server.getRemoteAddress() + ":" + server.getRemotePort();
					}
					// show result
					bot.msg(m.getTarget(), "14I have found " + list + " on " + name + "");
				}
			}
			else {
				bot.msg(m.getTarget(), "14I could not find *" + player + "* on any servers");
			}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !qf (player name)");
    	}
    }
}