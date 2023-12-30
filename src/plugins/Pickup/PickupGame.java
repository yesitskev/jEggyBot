// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PICKUPGAME
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.Pickup;

import jeggybot.Config;

import jeggybot.util.Utils;

import java.util.ArrayList;

import java.io.Serializable;

public class PickupGame implements Serializable
{
	// ********************************************************************************
	//			DECLARATIONS
	// ********************************************************************************
	
	public Config cfg;					// pickup config
	private	String admin;				// admin nickname
	private	String map;					// map
	private	PickupServer server;		// server details
	private	long time;					// time when pickup started
	private	int	nPlayers;				// number of players
	private	ArrayList<String> players;	// teams

	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public PickupGame(Config cfg, String admin, String map, PickupServer server)
	{
		// store pickup information
		this.cfg = cfg;
		this.admin = admin;
		this.map = map;
		this.server = server;
		players = new ArrayList<String>(cfg.i("MAXPLAYERS"));
		// allocate empty player slots to teams
		for (int x = 0; x < cfg.i("MAXPLAYERS"); x++) {
			players.add("?");
		}
	}

	// ********************************************************************************
	//			ACCESSOR +	MUTATOR	METHODS
	// ********************************************************************************

	public void setAdmin(String admin)
	{
		this.admin = admin;
	}

	public String getAdmin()
	{
		return admin;
	}

	public void	setMap(String map)
	{
		this.map = map;
	}

	public String getMap()
	{
		return map;
	}

	public void	setServer(PickupServer server)
	{
		this.server = server;
	}

	public PickupServer getServer()
	{
		return server;
	}

	public String getServerAddress()
	{
		return server.getAddress();
	}

	public String getPassword()
	{
		return server.getPassword();
	}

	public int getPlayerCount()
	{
		return nPlayers;
	}

	public int getMaxPlayers()
	{
		return cfg.i("MAXPLAYERS");
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public long	getTime()
	{
		return time;
	}

	public Config getConfig()
	{
		return cfg;
	}

	public ArrayList<String> getTeams()
	{
		return players;
	}

	// ********************************************************************************
	//			PUBLIC METHODS
	// ********************************************************************************

	public boolean isPlaying(String nickname)
	{
		return players.contains(nickname) && !nickname.equals("?");
	}

    public void addPlayer(String nickname)
    {
		// determine first available free slot
		int freeSlot = players.indexOf("?");
		// add player to slot
		addPlayer(nickname, freeSlot);
    }

	public void addPlayer(String nickname, int slot)
	{
		// add nickname to specified player slot
		players.set(slot, nickname);
		// increment player count
		nPlayers++;
	}

	public void	removePlayer(String nickname)
	{
    	// remove nickname from his slot
    	int slot = players.indexOf(nickname);
    	players.set(slot, "?");
    	// decrement player count
    	nPlayers--;
	}

    public boolean movePlayer(String nickname)
    {
    	// determine player's current team
    	int slot = players.indexOf(nickname);
    	int cTeam;
    	if (slot < (cfg.i("MAXPLAYERS") / 2)) {
    		cTeam = 1;
    	} else {
    		cTeam = 2;
    	}
    	// determine if their's a free slot on the opposing team, and if so, add player to team
    	int oTeam = 3 - cTeam;
    	int freeSlot = getFreeSlotOn(oTeam);
    	if (freeSlot > -1) {
    		// move player to team
    		players.set(slot, "?");
    		players.set(freeSlot, nickname);
    		return true;
    	} else {
    		// no free slots on team
    		return false;
    	}
    }

	public void swapPlayers(String player1, String player2)
	{
		// get slots of both players
		int slot1 = players.indexOf(player1);
		int slot2 = players.indexOf(player2);
		// swap these 2 players
		players.set(slot1, player2);
		players.set(slot2, player1);
	}

	public void	updatePlayer(String	oldnickname, String newnickname)
	{
		// update player's nickname in team list
		int	x =	players.indexOf(oldnickname);
		players.set(x, newnickname);
	}

	public void	shuffleTeams()
	{
		// randomly shuffle the teams
		for (int x = 0; x < players.size(); x++) {
			// get a random slot number
			int rslot = Utils.rand(x, players.size() - x);
			// swap these players
			String swap = players.get(x);
			players.set(x, players.get(rslot));
			players.set(rslot, swap);
		}
	}

	public boolean isAdmin(String nickname)
	{
		return admin.equalsIgnoreCase(nickname);
	}
	
	public int getFreeSlots()
	{
		return (cfg.i("MAXPLAYERS") - nPlayers);
	}
	
	public int getFreeSlotOn(String team)
	{
		// determine team number
		int nTeam;
		if (team.equalsIgnoreCase(cfg.s("TEAM-CODE1"))) {
			nTeam = 1;
		} else {
			nTeam = 2;
		}
		return getFreeSlotOn(nTeam);
	}
	
	public int getFreeSlotOn(int team)
	{
    	// check for first available slot on specified team
    	int slot = -1;
    	for (int x = cfg.i("MAXPLAYERS") - (cfg.i("MAXPLAYERS") / team); x < (cfg.i("MAXPLAYERS") / (3 - team)); x++) {
    		if (players.get(x).equals("?")) {
    			// open slot found
    			slot = x;
    			break;
    		}
    	}
		return slot;
	}
	
	public int getRandomFreeSlot()
	{
		int n = 0;
		int[] slots = new int[getFreeSlots()];
		for (int x = 0; x < cfg.i("MAXPLAYERS"); x++) {
			if (players.get(x).equals("?")) {
				slots[n++] = x;
			}
		}
		int r = Utils.rand(slots.length);
		return slots[r];
	}
	
	public int getTeamOf(String nickname)
	{
    	int slot = players.indexOf(nickname);
    	if (slot < (cfg.i("MAXPLAYERS") / 2)) {
    		return 1;
    	} else {
    		return 2;
    	}
	}
	
	public boolean hasFullTeams()
	{
		return (nPlayers == cfg.i("MAXPLAYERS"));
	}
}