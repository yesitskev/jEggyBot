// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PICKUP
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package	plugins.Pickup;

import jeggybot.*;

import jeggybot.util.Utils;
import jeggybot.util.DatabaseConnection;

import jirc.Channel;
import jirc.User;

import jirc.FloodHandler;

import java.util.*;

import java.io.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class plgPickup extends Plugin
{
	// ********************************************************************************
	//			DECLARATIONS
	// ********************************************************************************

	private HashMap<String, Config> configs;			// pickup config list
	private Config cfg;										// default pickup config
	private PickupGame game;								// current game
	private PickupGame previousGame;						// previous game
	private Timer startGameTimer;							// start game timer after pickup is full
	private boolean intermediate;							// intermediate phase after pickup is full
	private static List<String> months;					// array of months used for statistics
	private FloodHandler fh;								// top10 command flood handler
	private String news = "(no news)";					// news displayed in topic
	
	// ********************************************************************************
	//			CONSTRUCTOR
	// ********************************************************************************
	
	static {
		String[] tmp = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    	months = Arrays.asList(tmp);
	}
	
	public plgPickup()
	{
		// initialisation
		configs = new HashMap<String, Config>();
		fh = new FloodHandler(1, 30);
		// set plugin information
		name = "Pickup Matches";
		author = "Matthew Goslett";
		version = "2.4";
		// register plugin commands
		ph.register("!start", "cmdStartGame", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!stop", "cmdStopGame", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!addme", "cmdAddMe", ACCESS.BOT_CHANNEL, LEVEL.USER, this);
		ph.register("!removeme", "cmdRemoveMe", ACCESS.BOT_CHANNEL, LEVEL.USER, this);
		ph.register("!teams", "cmdTeams", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!game", "cmdGame", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!lastgame", "cmdLastGame", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!lastteams", "cmdLastTeams", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!configs", "cmdListConfigs", ACCESS.GLOBAL, LEVEL.VOICE, this);
		ph.register("!maps", "cmdListMaps", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!servers", "cmdListServers", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!getpass", "cmdGetPass", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!shuffle", "cmdShuffle", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!moveme", "cmdMoveMe", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!move", "cmdMove", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!swap", "cmdSwap", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!forceremove", "cmdForceRemove", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!map", "cmdChangeMap", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!server", "cmdChangeServer", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!admin", "cmdChangeAdmin", ACCESS.BOTH, LEVEL.VOICE, this);
		ph.register("!addmap", "cmdAddMap", ACCESS.BOTH, LEVEL.SOP, this);
		ph.register("!delmap", "cmdDelMap", ACCESS.BOTH, LEVEL.SOP, this);
		ph.register("!addserver", "cmdAddServer", ACCESS.BOTH, LEVEL.SOP, this);
		ph.register("!delserver", "cmdDelServer", ACCESS.BOTH, LEVEL.SOP, this);
		ph.register("!top", "cmdTop", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!stats", "cmdStats", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!setnews", "cmdSetNews", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!clearnews", "cmdClearNews", ACCESS.GLOBAL, LEVEL.SOP, this);
		// register aliases
		ph.registerAlias("!sg", "!start");
		ph.registerAlias("!cg", "!stop");
		ph.registerAlias("!add", "!addme");
		ph.registerAlias("!rem", "!removeme");
		ph.registerAlias("!remove", "!removeme");
		ph.registerAlias("!listconfigs", "!configs");
		ph.registerAlias("!listmaps", "!maps");
		ph.registerAlias("!listservers", "!servers");
		ph.registerAlias("!changemap", "!map");
		ph.registerAlias("!changeserver", "!server");
		ph.registerAlias("!changeadmin", "!admin");
		ph.registerAlias("!ca", "!admin");
		ph.registerAlias("!top10", "!top");
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onRehash() {
				onRehashEvent();
			}
			public void onJoin(User user, Channel channel) {
				onJoinEvent(user, channel);
			}
			public void onPart(User user, Channel channel) {
				onPartEvent(user, channel);
			}
			public void onNickChange(User user, String newnickname) {
				onNickChangeEvent(user, newnickname);
			}
			public void onKick(User user, String target, Channel channel, String reason) {
				onKickEvent(user, target, channel, reason);
			}
			public void onQuit(User user, String reason) {
				onQuitEvent(user, reason);
			}
			public void onTopicChange(User user, Channel channel, String topic) {
				onTopicChangeEvent(user, channel, topic);
			}
			public void onDisconnect() {
				onDisconnectEvent();
			}
		});
		// load configs
		loadConfigs();
		if(isConfig(bot.s("DEFAULT-CONFIG"))) {
			cfg = getConfig(bot.s("DEFAULT-CONFIG"));
		} else {
			bot.log("The default pickup configuration file could not be loaded");
		}
		// load lastgame data file into memory so that we can show the
		// previous pickup's information
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(bot.s("LAST-GAME-DATA")));
			previousGame = (PickupGame) ois.readObject();
			ois.close();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		// game specific command user levels
		if (cfg.s("GAME-NAME").equals("Team Fortress 2")) {
			BotCommand c;
			c = ph.getCommand("!start");
			c.setUserLevel(LEVEL.OP);
			c = ph.getCommand("!stop");
			c.setUserLevel(LEVEL.OP);
		}
	}

	// ********************************************************************************
	//			PRIVATE METHODS
	// ********************************************************************************

	private void loadConfigs()
	{
		// clear config list
		configs.clear();
		// get list of all files in pickup configs directory
		File configDir = new File(bot.s("PICKUP-CONFIGS-DIR"));
		for (String filename: configDir.list()) {
			// extract config name
			String name = filename.substring(0, filename.lastIndexOf("."));
			// load config
			Config cfg = new Config(name, bot.s("PICKUP-CONFIGS-DIR") + "/" + filename);
			// add config to config list
			configs.put(name.toLowerCase(), cfg);
		}
	}

	private	boolean	isConfig(String name)
	{
		return configs.containsKey(name.toLowerCase());
	}

	private	Config getConfig(String name)
	{
		return configs.get(name.toLowerCase());
	}

	private	boolean	isServer(String servertag)
	{
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// check if server exists in database
			PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "pickupservers WHERE tag=?");
			ps.setString(1, servertag);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int rows = rs.getInt(1);
			// close database connection
			db.close();
			return (rows > 0);
		} catch (SQLException e) {
			return false;
		}
	}

	private PickupServer getPickupServer(String servertag)
	{
		try {
			PickupServer server = null;
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve pickup server information from our database
			PreparedStatement ps = db.prepareStatement("SELECT ip, port, password FROM " + bot.s("TABLE-PREFIX") + "pickupservers WHERE tag=?");
			ps.setString(1, servertag);
			ResultSet rs = ps.executeQuery();
			if (db.getRowCount(rs) > 0) {
				rs.next();
				String ip = rs.getString("ip");
				int port = rs.getInt("port");
				String password = rs.getString("password");
				server = new PickupServer(servertag, ip, port, password);
			}
			// close database connection
			db.close();
			return server;
		} catch (SQLException e) {
			return null;
		}
	}

	private boolean isMap(String map)
	{
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// check if map exists in database
			PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "pickupmaps WHERE map=?");
			ps.setString(1, map);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int rows = rs.getInt(1);
			// close database connection
			db.close();
			return (rows > 0);
		} catch (SQLException e) {
			return false;
		}
	}

	private String replaceTokens(String text)
	{
		if (isActive()) {
	    	text = text.replace("$admin", game.getAdmin());
	    	text = text.replace("$map", game.getMap());
	    	PickupServer server = game.getServer();
	    	text = text.replace("$server", server.getAddress() + " (" + server.getTag() + ")");
	    	text = text.replace("$password", game.getPassword());
		}
		text = text.replace("$news", news);
    	if (previousGame != null) {
			text = text.replace("$lastgame", bot.getFormattedTime(previousGame.getTime(), "dd/MM/yyyy HH:mm:ss"));
    	} else {
    		text = text.replace("$lastgame", "none");
    	}
		return text;
	}

	private void topic()
	{
		if (isActive()) {
			bot.setTopic(bot.s("CHANNEL"), replaceTokens(game.cfg.s("TOPIC-GAME")));
		} else {
			bot.setTopic(bot.s("CHANNEL"), replaceTokens(cfg.s("TOPIC")));
		}
	}

	private boolean isActive()
	{
		return (game != null);
	}

	private String getFormattedTeams()
	{
		return getFormattedTeams(game);
	}

	private String getFormattedTeams(PickupGame game)
	{
		// get arraylist of teams
		ArrayList<String> players = game.getTeams();
		// teams header
		String teams = "6 " + game.getPlayerCount() + "/" + players.size() + "   ";
		// add all player nickname to team list
		for (int team = 1; team <= 2; team++) {
			// team name
			teams += game.cfg.s("TEAM-NAME" + team) + " ";
			for (int x = players.size() - (players.size() / team); x < (players.size() / (3 - team)); x++) {
				String player = players.get(x);
				if (game.isAdmin(player)) {
					// underline and colour the admin's nickname
					teams += "2( " + player + " ) ";
				} else {
					teams += "14( " + player + " ) ";
				}
			}
		}
		return teams;
	}

	private void onFullTeams()
	{
		if (game.cfg.s("PAUSE-BEFORE-START").equalsIgnoreCase("OFF")) {
			// start the match immediately
			startGame();
		} else {
			if (!intermediate) {
				// allow for any changes within the next minute and then start the game
				intermediate = true;
				// message channel that the game will start in a minute
				bot.msg(bot.s("CHANNEL"), "14The game will automatically start in 60 seconds, before which admins/players can still make changes to the current teams");
				// schedule timer to start the game after 1 minute
				startGameTimer = new Timer();
				startGameTimer.schedule(new TimerTask() {
					public void run() {
						// check if teams are still full before we start the game
						intermediate = false;
						if (game.hasFullTeams()) {
							startGame();
						}
					}
				}, 60000);
			}
		}
	}

	private void startGame()
	{
		// message channel
		bot.msg(bot.s("CHANNEL"), getFormattedTeams());
		bot.msg(bot.s("CHANNEL"), "14PLEASE WAIT WHILE I MESSAGE ALL PLAYERS THE SERVER AND PASSWORD");
		// message server address and password all players who have subscribed
		PickupServer server = game.getServer();
		if (!game.getPassword().equals("")) {
			bot.msg(game.getTeams(), "14PLEASE CONNECT NOW TO " + server.getAddress() + " (" + server.getTag() + ")" + " WITH PASSWORD " + game.getPassword() + ".");
		} else {
			bot.msg(game.getTeams(), "14PLEASE CONNECT NOW TO " + server.getAddress() + " (" + server.getTag() + ")" + ".");
		}
		bot.msg(bot.s("CHANNEL"), "14SERVER AND PASSWORD HAS BEEN SENT TO ALL PLAYERS.  TYPE !GETPASS IF YOU DID NOT RECEIVE IT.");
		// send list of unformatted teams to admin
		bot.msg(game.getAdmin(), "14The teams for your pickup are as follows:");
		bot.msg(game.getAdmin(), jirc.Utils.stripFormatting(getFormattedTeams()));
		// store current time for game which is being played
		game.setTime(new Date().getTime());
		// set last game played to current game and write game data to file
		// so that previous game info can be restored when bot is restarted
		previousGame = game;
		try {
			ObjectOutput oos = new ObjectOutputStream(new FileOutputStream(bot.s("LAST-GAME-DATA")));
			oos.writeObject(previousGame);
			oos.close();
		} catch (IOException e) {
		}
		// update pickup stats in database
		updatePickupStats();
		// log
		bot.log(game.getTeams().toString());
		bot.log("pickup game closed");
		game = null;
		// set channel topic
		topic();
	}

	private void stopGame()
	{
    	// cancel startgame timer if running
    	if (intermediate) {
    		intermediate = false;
    		startGameTimer.cancel();
    	}
		// clear instance of game
		game = null;
	}

	private void updatePickupStats()
	{
		// insert pickup information into database
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// insert pickup information into database
			PreparedStatement ps = db.prepareStatement("INSERT INTO pickups (game, admin, map, server, time) VALUES (?, ?, ?, ?, ?)");
			ps.setString(1, game.cfg.s("GAME-NAME"));
			ps.setString(2, game.getAdmin());
			ps.setString(3, game.getMap());
			ps.setString(4, game.getServerAddress());
			ps.setTimestamp(5, new Timestamp(game.getTime()));
			ps.executeUpdate();
			// retrieve last id of pickup entered so that we can link the players to this pickup
			ResultSet rs = db.query("SELECT LAST_INSERT_ID()");
			rs.next();
			int id = rs.getInt(1);
			// insert each player into database
			// build each insert statement then execute them in a single batch
			ps = db.prepareStatement("INSERT INTO pickupplayers VALUES (?, ?)");
			ArrayList<String> teams = game.getTeams();
			for (String player: teams) {
				ps.setInt(1, id);
				ps.setString(2, player);
				ps.addBatch();
			}
			ps.executeBatch();
			// close database connection
			db.close();
		} catch (SQLException e) {
		}
	}

	private boolean hasMatchAccess(String nickname)
	{
		if (cfg.s("RESTRICT-GAME-ADMIN").equalsIgnoreCase("OFF")) {
			return true;
		} else {
			return  ((bot.getUserLevel(nickname) >= LEVEL.OP) || (isActive() && (game.isAdmin(nickname))));
		}
	}

	// ********************************************************************************
	//			IRC EVENTS
	// ********************************************************************************

    private void onRehashEvent()
    {
		// re-load config list
		loadConfigs();
		if(isConfig(bot.s("DEFAULT-CONFIG"))) {
			cfg = getConfig(bot.s("DEFAULT-CONFIG"));
		} else {
			bot.log("The default pickup configuration file could not be loaded");
		}
    }

    private void onJoinEvent(User user, Channel channel)
    {
		if (bot.isMyChannel(channel)) {
			if (bot.isMe(user)) {
				// BOT JOINED PICKUP CHANNEL
				// set channel topic
				topic();
			}
		}
    }

    private void onPartEvent(User user, Channel channel)
    {
    	if (bot.isMyChannel(channel)) {
    		if (isActive()) {
    			if (game.isPlaying(user.getNickname())) {
					// player left the channel without removing himself from the pickup
					// remove player from game
					game.removePlayer(user.getNickname());
					// notice channel and message new teams
					bot.msg(bot.s("CHANNEL"), "14" + user.getNickname() + " has been removed from the pickup because he parted the channel");
					bot.msg(bot.s("CHANNEL"), getFormattedTeams());
    			}
    		}
    	}
    }

    private void onNickChangeEvent(User user, String newnickname)
    {
    	if (isActive()) {
    		if (game.isAdmin(user.getNickname())) {
    			// the admin has changed his nickname
    			game.setAdmin(newnickname);
    			// message channel and change topic
    			bot.msg(bot.s("CHANNEL"), "14The admin has changed his nickname to " + newnickname + "");
    			topic();
    		}
			if (game.isPlaying(user.getNickname())) {
				// update user's nickname in the player list
				game.updatePlayer(user.getNickname(), newnickname);
			}
    	}
    }

	private void onKickEvent(User user, String target, Channel channel, String reason)
    {
    	if (bot.isMyChannel(channel)) {
			if (bot.isMe(target)) {
				// BOT KICKED FROM PICKUP CHANNEL
				// try to rejoin channel
				bot.join(bot.s("CHANNEL"));
			} else {
				// USER KICKED FROM PICKUP CHANNEL
				if (isActive()) {
					if (game.isPlaying(target)) {
						// remove target from the game, since he has been kicked from the channel
						game.removePlayer(target);
						bot.msg(bot.s("CHANNEL"), "14" + target + " has been removed from the pickup because " + user.getNickname() + " gave him the boot");
						bot.msg(bot.s("CHANNEL"), getFormattedTeams());
					}
				}
			}
    	}
    }

    private void onQuitEvent(User user, String reason)
    {
    	if (isActive()) {
    		if (game.isPlaying(user.getNickname())) {
				// player quit irc without removing himself from the pickup
				// remove player from game
				game.removePlayer(user.getNickname());
				// notice channel and message new teams
				bot.msg(bot.s("CHANNEL"), "14" + user.getNickname() + " has been removed from the pickup because he quit irc");
				bot.msg(bot.s("CHANNEL"), getFormattedTeams());
    		}
    	}
    }

	private void onTopicChangeEvent(User user, Channel channel, String topic)
	{
		if (bot.isMyChannel(channel)) {
			if (!bot.isMe(user)) {
				// a user has tried to change the channel topic
				// reset topic to what it previously was
				topic();
			}
		}
	}

	private void onDisconnectEvent()
	{
		if (isActive()) {
			// stop current game
			stopGame();
		}
	}

	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
	
    public void cmdStartGame(final MessageInfo m, final String[] p)
    {
    	if (cfg != null) {
	    	// map & server are optional values, if left blank, use defaults from config file
	    	String map;
	    	String servertag;
	    	if (p.length >= 1) {
	    		map = p[0];
	    		if (p.length >= 2) {
	    			servertag = p[1];
	    		} else {
	    			// use default server
	    			servertag = cfg.s("DEFAULT-SERVER");
	    		}
	    	} else {
	    		// use default map & server
	    		map = cfg.s("DEFAULT-MAP");
	    		servertag = cfg.s("DEFAULT-SERVER");
	    	}
	    	if (!isActive()) {
	    		if (isMap(map)) {
			    	if (isServer(servertag)) {
			    		// create new instance of a pickup game
			    		PickupServer server = getPickupServer(servertag);
			    		game = new PickupGame(cfg, m.getNickname(), map, server);
			    		// set channel topic
			    		topic();
			    		// message and advertise to channels
			    		if (cfg.s("ADVERTISE-GAME").equalsIgnoreCase("ON")) {
			    			bot.amsg("6*amsg* 14A pickup for " + cfg.s("GAME-NAME") + " has been started in channel " + bot.s("CHANNEL") + "");
			    		} else {
			    			bot.msg(bot.s("CHANNEL"), "14A pickup for " + cfg.s("GAME-NAME") + " has been started");
			    		}
			    		// log
			    		bot.log(m.getNickname() + " started a pickup on map " + map + " and server " + server.getAddress());
			    	} else {
			    		bot.notice(m.getNickname(), "" + servertag + " is not in my server list");
			    	}
	    		} else {
	    			bot.notice(m.getNickname(), "" + map + " is not in my map list");
	    		}
	    	}
    	} else {
    		bot.notice(m.getNickname(), "No pickup configuration file has been loaded");
    	}
    }

    public void cmdStopGame(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (isActive()) {
    		// message channel that game has been cancelled
    		bot.msg(bot.s("CHANNEL"), "14The pickup has been cancelled by " + m.getNickname() + "");
    		// stop current game
    		stopGame();
    		// set channel topic
			topic();
    		// log
    		bot.log(m.getNickname() + " cancelled the pickup");
    	}
    }

    public void cmdAddMe(final MessageInfo m, final String[] p)
    {
    	if (isActive()) {
    		if (!game.isPlaying(m.getNickname())) {
    			if (!game.hasFullTeams()) {
    				if (game.cfg.s("ADD-RANDOM").equalsIgnoreCase("ON")) {
    					// add player to a random slot
    					int slot = game.getRandomFreeSlot();
    					game.addPlayer(m.getNickname(), slot);
    				} else if (p.length == 1) {
						// player requested team
						String team = p[0];
						if ((team.equalsIgnoreCase(game.cfg.s("TEAM-CODE1"))) || (team.equalsIgnoreCase(game.cfg.s("TEAM-CODE2")))) {
							// get first free slot on requested team
							int slot = game.getFreeSlotOn(team);
							if (slot > -1) {
								// add player to slot
								game.addPlayer(m.getNickname(), slot);
							} else {
								bot.notice(m.getNickname(), "There are no free slots on team " + team.toUpperCase() + "");
								return;
							}
						}
					} else {
	    				// add player to next available slot
	    				game.addPlayer(m.getNickname());
					}
	    			// message channel the teams
	    			bot.msg(bot.s("CHANNEL"), getFormattedTeams());
			    	// start the game/intermediate timer if teams are full
					if (game.hasFullTeams()) onFullTeams();
			    }	
    		}
    	}		
    }

    public void cmdRemoveMe(final MessageInfo m, final String[] p)
    {
    	if (isActive()) {
    		if (game.isPlaying(m.getNickname())) {
				// remove player from game
				game.removePlayer(m.getNickname());
				// message channel the new teams
				bot.msg(bot.s("CHANNEL"), getFormattedTeams());
    		}
    	}
    }

    public void cmdTeams(final MessageInfo m, final String[] p)
    {
    	if (isActive()) bot.msg(m.getTarget(), getFormattedTeams());
    }

    public void cmdGame(final MessageInfo m, final String[] p)
    {
		if (isActive()) {
			PickupServer server = game.getServer();
			bot.msg(m.getTarget(), "14The current game will be admined by " + game.getAdmin() + " on the map " + game.getMap() + " and server " + server.getAddress() + " (" + server.getTag() + ")");
		} else {
			bot.msg(m.getTarget(), "14no pickup in progress");
		}
    }

    public void cmdLastGame(final MessageInfo m, final String[] p)
    {
    	if (previousGame != null) {
    		PickupServer server = previousGame.getServer();
    		bot.msg(m.getTarget(), "14The last game was admined by " + previousGame.getAdmin() + " on map " + previousGame.getMap() + " and server " + server.getAddress() + " (" + server.getTag()  + ") at " + bot.getFormattedTime(previousGame.getTime(), "HH:mm:ss, EEEE d MMMM") + "");
    	} else {
    		bot.msg(m.getTarget(), "14I have no record of a previous pickup game.");
    	}
    }

    public void cmdLastTeams(final MessageInfo m, final String[] p)
    {
    	if (previousGame != null) {
    		bot.msg(m.getTarget(), "14The teams for the previous pickup were as follows:");
    		bot.msg(m.getTarget(), getFormattedTeams(previousGame));
    	} else {
    		bot.msg(m.getTarget(), "14I have no record of a previous pickup game.");
    	}
    }

    public void cmdListConfigs(final MessageInfo m, final String[] p)
    {
		// build single list of configs
		String list = "";
		Iterator it = (configs.values()).iterator();
		while (it.hasNext()) {
            Config config = (Config) it.next();
            if (config.equals(cfg)) {
            	list += "" + config.getName() + ", ";
            } else {
            	list += config.getName() + ", ";
            }
		}
		// send config list
		bot.notice(m.getNickname(), "Pickup Config List");
		if (!list.equals("")) {
			bot.notice(m.getNickname(), list);
		} else {
			bot.notice(m.getNickname(), "config list is empty");
		}
    }

    public void cmdListMaps(final MessageInfo m, final String[] p)
    {
    	// build single list of maps
    	String list = "";
    	try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve list of pickup maps
			ResultSet rs = db.query("SELECT * FROM " + bot.s("TABLE-PREFIX") + "pickupmaps ORDER BY map");
			while (rs.next()) {
				String map = rs.getString("map");
				list += map + ", ";
			}
			// close database connection
			db.close();
			// send list of maps
			bot.notice(m.getNickname(), "Pickup Map List");
			if (!list.equals("")) {
				bot.notice(m.getNickname(), list);
			} else {
				bot.notice(m.getNickname(), "map list is empty");
			}
    	} catch (SQLException e) {
    		bot.notice(m.getNickname(), "An error occured whilst retrieving the pickup map list");
    	}
    }

    public void cmdListServers(final MessageInfo m, final String[] p)
    {
    	// build single list of servers
    	String list = "";
    	try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve list of pickup servers
			ResultSet rs = db.query("SELECT tag, ip, port FROM " + bot.s("TABLE-PREFIX") + "pickupservers ORDER BY tag");
			while (rs.next()) {
				String tag = rs.getString("tag");
				String ip = rs.getString("ip");
				int port = rs.getInt("port");
				list += tag + " (" + ip + ":" + port + "), ";
			}
			// close database connection
			db.close();
			// send list of servers
			bot.notice(m.getNickname(), "Pickup Server List");
			if (!list.equals("")) {
				bot.notice(m.getNickname(), list);
			} else {
				bot.notice(m.getNickname(), "server list is empty");
			}
    	} catch (SQLException e) {
    		bot.notice(m.getNickname(), "An error occured whilst retrieving the pickup server list");
    	}
    }

	public void cmdGetPass(final MessageInfo m, final String[] p)
	{
		if (previousGame != null) {
			if (previousGame.isPlaying(m.getNickname())) {
				PickupServer server = previousGame.getServer();
				bot.notice(m.getNickname(), "The password for your game on " + server.getAddress() + " (" + server.getTag() + ") is " + server.getPassword() + "");
			} else {
				bot.notice(m.getNickname(), "You were not subscribed to the previous pickup game");
			}
		}
	}

    public void cmdShuffle(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (isActive()) {
    		// randomise the teams
    		game.shuffleTeams();
    		// message channel that the teams have been shuffled
    		bot.msg(bot.s("CHANNEL"), "14" + m.getNickname() + " is *shuffling* the teams");
    		bot.msg(bot.s("CHANNEL"), getFormattedTeams());
    		// log
    		bot.log(m.getNickname() + " is shuffling the teams");
    	}
    }

	public void cmdMoveMe(final MessageInfo m, final String[] p)
	{
		String target = m.getNickname();
		if (isActive()) {
			if (game.isPlaying(target)) {
	    		// try to move player from his current team to opposing team
	    		if(game.movePlayer(target)) {
	    			bot.msg(bot.s("CHANNEL"), "14" + target + " moved himself to the opposing team");
	    			bot.msg(bot.s("CHANNEL"), getFormattedTeams());
	    			// log
	    			bot.log(m.getNickname() + " moved himself to the opposing team");
	    		} else {
	    			bot.notice(m.getNickname(), "There are no open slots on the opposing team");
	    		}
			}
		}
	}

    public void cmdMove(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (p.length == 1) {
    		String target = p[0];
	    	if (isActive()) {
		    	if (game.isPlaying(target)) {
		    		// try to move target from his current team to opposing team
		    		if(game.movePlayer(target)) {
		    			bot.msg(bot.s("CHANNEL"), "14" + m.getNickname() + " moved " + target + " to opposing team");
		    			bot.msg(bot.s("CHANNEL"), getFormattedTeams());
		    			// log
		    			bot.log(m.getNickname() + " moved " + target + " to opposing team");
		    		} else {
		    			bot.notice(m.getNickname(), "I could not move " + target + ", no open slots left in opposing team");
		    		}
		    	} else {
		    		bot.notice(m.getNickname(), "" + target + " is not playing in this pickup");
		    	}
	    	}
    	} else {
			bot.notice(m.getNickname(), "Syntax: !move (target)");
    	}
    }

    public void cmdSwap(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (p.length == 2) {
    		String player1 = p[0];
    		String player2 = p[1];
    		if (isActive()) {
    			if ((game.isPlaying(player1)) && (game.isPlaying(player2))) {
    				if (game.getTeamOf(player1) != game.getTeamOf(player2)) {
    					// swap the 2 players
    					game.swapPlayers(player1, player2);
    					// message channel
		    			bot.msg(bot.s("CHANNEL"), "14" + m.getNickname() + " swapped " + player1 + " with " + player2 + "");
		    			bot.msg(bot.s("CHANNEL"), getFormattedTeams());
		    			// log
		    			bot.log(m.getNickname() + " swapped " + player1 + " with " + player2);
    				}
    			} else {
    				bot.notice(m.getNickname(), "" + player1 + " and " + player2 + " must both be playing in the pickup");
    			}
    		}
    	} else {
    		bot.notice(m.getNickname(), "Syntax: !swap (player 1) (player 2)");
    	}
	}
	
    public void cmdForceRemove(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (p.length == 1) {
    		String target = p[0];
	    	if (isActive()) {
		    	if (game.isPlaying(target)) {
		    		// remove target from player list
		    		game.removePlayer(target);
		    		// message channel
		    		bot.msg(bot.s("CHANNEL"), "14" + m.getNickname() + " removed " + target + " from the pickup game");
		    		bot.msg(bot.s("CHANNEL"), getFormattedTeams());
		    		// log
		    		bot.log(m.getNickname() + " removed " + target + " from the pickup game");
		    	} else {
		    		bot.notice(m.getNickname(), "" + target + " is not playing in this pickup");
		    	}
	    	}	
    	} else {
			bot.notice(m.getNickname(), "Syntax: !forceremove (target)");
    	}
    }

    public void cmdChangeMap(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (p.length == 1) {
    		String map = p[0];
			if (isActive()) {
				if (!map.equals(game.getMap())) {
					if (isMap(map)) {
						// change map name
						game.setMap(map);
						// message channel
						bot.msg(bot.s("CHANNEL"), "14Map has been changed to " + map + "");
						// update channel topic
						topic();
						// log
						bot.log(m.getNickname() + " changed the pickup map to " + map);
					} else {
						bot.notice(m.getNickname(), "" + map + " is not in my map list");
					}
				}
			}
    	} else {
			bot.notice(m.getNickname(), "Syntax: !changemap (map)");
    	}
    }

    public void cmdChangeServer(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (p.length == 1) {
    		String servertag = p[0];
			if (isActive()) {
				if (isServer(servertag)) {
					PickupServer server = getPickupServer(servertag);
					if (!server.getAddress().equalsIgnoreCase(game.getServerAddress())) {
						// update server address
						game.setServer(server);
						// message channel
						bot.msg(bot.s("CHANNEL"), "14We will now be playing on " + game.getServerAddress() + "");
						// update channel topic
						topic();
						// log
						bot.log(m.getNickname() + " changed the pickup server to " + game.getServerAddress());
					}
				} else {
					bot.notice(m.getNickname(), "" + servertag + " is not in my server list");
				}
			}
    	} else {
			bot.notice(m.getNickname(), "Syntax: !changeserver (server)");
    	}
    }

    public void cmdChangeAdmin(final MessageInfo m, final String[] p)
    {
    	if (!hasMatchAccess(m.getNickname())) {
    		bot.notice(m.getNickname(), "You are not the admin of this pickup match.");
    		return;
    	}
    	if (p.length == 1) {
    		String target = p[0];
	    	if (isActive()) {
	    		if (!target.equalsIgnoreCase(game.getAdmin())) {
	    			if (bot.isLevel(target, LEVEL.VOICE)) {
	    				// update admin nickname
	    				game.setAdmin(target);
	    				//message channel
						bot.msg(bot.s("CHANNEL"), "14" + target + " will now be admining the pickup");
						// update topic
						topic();
						// log
						bot.log(m.getNickname() + " changed the pickup admin to " + target);
	    			} else {
						bot.notice(m.getNickname(), "" + target + " does not have admin access");
	    			}
	    		}
	    	}	
    	} else {
			bot.notice(m.getNickname(), "Syntax: !changeadmin (target)");
    	}
    }

    public void cmdAddMap(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String map = p[0];
	    	if (!isMap(map)) {
				try {
					// attempt to add map to database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// insert server address in pickupservers table
					PreparedStatement ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "pickupmaps VALUES (?)");
					ps.setString(1, map);
					ps.executeUpdate();
					// close database connection
					db.close();
					// notice user
					bot.notice(m.getNickname(), "" + map + " added to my map list");
					// log
					bot.log(m.getNickname() + " added " + map + " to the map list");
				} catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst adding map to database");
				}
	    	} else {
	    		bot.notice(m.getNickname(), "" + map + " is already in my map list");
	    	}
    	} else {
			bot.notice(m.getNickname(), "Syntax: !addmap (map)");
    	}
    }

    public void cmdDelMap(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String map = p[0];
	    	if (isMap(map)) {
				try {
					// attempt to remove map from database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// delete server from database
					PreparedStatement ps = db.prepareStatement("DELETE FROM " + bot.s("TABLE-PREFIX") + "pickupmaps WHERE map=?");
					ps.setString(1, map);
					ps.executeUpdate();
					// close database connection
					db.close();
					// notice user
					bot.notice(m.getNickname(), "" + map + " removed from my map list");
					// log
					bot.log(m.getNickname() + " removed " + map + " from the map list");
				} catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst removing map from database");
				}
	    	} else {
	    		bot.notice(m.getNickname(), "" + map + " is not in my map list");
	    	}
    	} else {
			bot.notice(m.getNickname(), "Syntax: !delmap (map)");
    	}
    }

    public void cmdAddServer(final MessageInfo m, final String[] p)
    {
    	/*
    	if (p.length == 1) {
    		String server = p[0];
	    	if (!isServer(server)) {
	    		if (server.matches(".+:\\d{1,5}")) {
					try {
						// attempt to add server to database
						// connect to database
						DatabaseConnection db = bot.getDatabaseConnection();
						// insert server address in pickupservers table
						PreparedStatement ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "pickupservers VALUES (?)");
						ps.setString(1, server);
						ps.executeUpdate();
						// close database connection
						db.close();
						// notice user
						bot.notice(m.getNickname(), "" + server + " added to my server list");
						// log
						bot.log(m.getNickname() + " added " + server + " to the server list");
					}
					catch (SQLException e) {
						bot.notice(m.getNickname(), "An error occured whilst adding server to database");
					}
	    		}
				else {
					bot.notice(m.getNickname(), "Syntax: !addserver (ip:port)");
				}
	    	}
	    	else {
	    		bot.notice(m.getNickname(), "" + server + " is already in my server list");
	    	}
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !addserver (ip:port)");
    	}
    	*/
    }
    
    public void cmdDelServer(final MessageInfo m, final String[] p)
    {
    	/*
    	if (p.length == 1) {
    		String server = p[0];
	    	if (isServer(server)) {
				try {
					// attempt to remove server address from database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// delete server from database
					PreparedStatement ps = db.prepareStatement("DELETE FROM " + bot.s("TABLE-PREFIX") + "pickupservers WHERE remoteaddress=?");
					ps.setString(1, server);
					ps.executeUpdate();
					// close database connection
					db.close();
					// notice user
					bot.notice(m.getNickname(), "" + server + " removed from my server list");
					// log
					bot.log(m.getNickname() + " removed " + server + " from the server list");
				}
				catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst removing server from database");
				}
	    	}
	    	else {
	    		bot.notice(m.getNickname(), "" + server + " is not in my server list");
	    	}
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !delserver (ip:port)");
    	}
    	*/
    }
    
    private HashMap getStatsPeriod(String period)
    {
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("PERIOD", period);
		if (period.equals("TODAY")) {
			map.put("TITLE", "Today's ");
			map.put("WHERE", "DATE(time)=CURRENT_DATE()");
		} else if (period.equals("YESTERDAY")) {
			map.put("TITLE", "Yesterday's ");
			map.put("WHERE", "DATE(time)=DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY)");
		} else if (period.matches("\\d{4}")) {
			map.put("TITLE", period + "'s ");
			map.put("WHERE", "YEAR(time)=" + period);
		} else if (months.contains(period)) {
			// change case of month name to lowercase
			period = period.toLowerCase();
			// make the first letter a capital letter
			period = period.substring(0, 1).toUpperCase() + period.substring(1);
			map.put("TITLE", period + "'s ");
			map.put("WHERE", "MONTH(time)=" + (months.indexOf(period.toUpperCase()) + 1) + " AND YEAR(time)=YEAR(CURRENT_DATE())");
		} else {
			map.put("TITLE", "");
			map.put("WHERE", "1");
		}
		return map;
    }
    
    public void cmdTop(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String type = p[0].toUpperCase();
    		String period;
    		// set time period
    		if (p.length == 2) {
    			period = p[1].toUpperCase();
    		} else {
    			period = "ALL";
    		}
			// get sql where clause and title for time period
			HashMap map = getStatsPeriod(period);
    		// determine type of stats to fetch from database
    		if (type.equals("PLAYERS")) {
    			cmdTopPlayers(m, map);
    		} else if (type.equals("ADMINS")) {
    			cmdTopAdmins(m, map);
    		} else if (type.equals("MAPS")) {
    			cmdTopMaps(m, map);
    		} else if (type.equals("SERVERS")) {
    			cmdTopServers(m, map);
    		} else if (type.equals("DAYS")) {
    			cmdTopDays(m, map);
    		} else {
    			bot.notice(m.getNickname(), "Syntax: !top (players/admins/maps/servers/days) [today/yesterday/year/month]");
    		}
    	} else {
    		bot.notice(m.getNickname(), "Syntax: !top (players/admins/maps/servers/days) [today/yesterday/year/month]");
    	}
    }

    private void cmdTopPlayers(final MessageInfo m, final HashMap period)
    {
		if (fh.isFlooding("PLAYERS-" + period.get("TITLE") + "-" + m.getTarget().toUpperCase())) {
			return;
		}
    	// build sql query
    	String query = "SELECT nickname, COUNT(*) AS c FROM pickupplayers,pickups WHERE pickups.id=pickupplayers.pickupid AND game='" + cfg.s("GAME-NAME") + "' AND " + period.get("WHERE") + " GROUP BY nickname ORDER BY c DESC LIMIT 10";
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve top players from database
			ResultSet rs = db.query(query);
			if (db.getRowCount(rs) > 0) {
				bot.msg(m.getTarget(), "14" + period.get("TITLE") + "Most Active Players");
				int rank = 1;
				while (rs.next()) {
					String item = rs.getString(1);
					int count = rs.getInt(2);
					bot.msg(m.getTarget(), "14" + rank++ + ". 7" + item + " 14has played7 " + count + " 14pickups");
				}			
			} else {
				bot.msg(m.getTarget(), "14no stats available");
			}
			// close database connection
			db.close();
		} catch (SQLException e) {
			bot.notice(m.getNickname(), "An error occured whilst retrieving pickup stats from the database");
		}
    }

    private void cmdTopAdmins(final MessageInfo m, final HashMap period)
    {
		if (fh.isFlooding("ADMINS-" + period.get("TITLE") + "-" + m.getTarget().toUpperCase())) {
			return;
		}
    	// build sql query
    	String query;
    	if (cfg.s("GAME-NAME").equals("Call of Duty 4")) {
    		query = "SELECT admin, COUNT(*) AS c FROM pickups WHERE game='" + cfg.s("GAME-NAME") + "' AND " + period.get("WHERE") + " GROUP BY admin ORDER BY c DESC LIMIT 10";
    	} else {
			query = "SELECT admin, COUNT(*) AS c FROM pickups,pickupplayers WHERE pickupplayers.pickupid=pickups.id AND pickupplayers.nickname=pickups.admin AND game='" + cfg.s("GAME-NAME") + "' AND " + period.get("WHERE") + " GROUP BY admin ORDER BY c DESC LIMIT 10";
    	}
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve top admins from database
			ResultSet rs = db.query(query);
			if (db.getRowCount(rs) > 0) {
				bot.msg(m.getTarget(), "14" + period.get("TITLE") + "Most Active Admins");
				int rank = 1;
				while (rs.next()) {
					String item = rs.getString(1);
					int count = rs.getInt(2);
					bot.msg(m.getTarget(), "14" + rank++ + ". 7" + item + " 14has admined7 " + count + " 14pickups");
				}			
			} else {
				bot.msg(m.getTarget(), "14no stats available");
			}
			// close database connection
			db.close();
		} catch (SQLException e) {
			bot.notice(m.getNickname(), "An error occured whilst retrieving pickup stats from the database");
		}
    }

    private void cmdTopMaps(final MessageInfo m, final HashMap period)
    {
		if (fh.isFlooding("MAPS-" + period.get("TITLE") + "-" + m.getTarget().toUpperCase())) {
			return;
		}
    	// build sql query
    	String query = "SELECT map, COUNT(*) AS c FROM pickups WHERE game='" + cfg.s("GAME-NAME") + "' AND " + period.get("WHERE") + " GROUP BY map ORDER BY c DESC LIMIT 10";
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve top maps from database
			ResultSet rs = db.query(query);
			if (db.getRowCount(rs) > 0) {
				bot.msg(m.getTarget(), "14" + period.get("TITLE") + "Most Played Maps");
				int rank = 1;
				while (rs.next()) {
					String item = rs.getString(1);
					int count = rs.getInt(2);
					bot.msg(m.getTarget(), "14" + rank++ + ". 7" + item + " 14has been played7 " + count + " 14times");
				}			
			} else {
				bot.msg(m.getTarget(), "14no stats available");
			}
			// close database connection
			db.close();
		} catch (SQLException e) {
			bot.notice(m.getNickname(), "An error occured whilst retrieving pickup stats from the database");
		}
    }

    private void cmdTopServers(final MessageInfo m, final HashMap period)
    {
		if (fh.isFlooding("SERVERS-" + period.get("TITLE") + "-" + m.getTarget().toUpperCase())) {
			return;
		}
    	// build sql query
    	String query = "SELECT server, COUNT(*) AS c FROM pickups WHERE game='" + cfg.s("GAME-NAME") + "' AND " + period.get("WHERE") + " GROUP BY server ORDER BY c DESC LIMIT 10";
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve top servers from database
			ResultSet rs = db.query(query);
			if (db.getRowCount(rs) > 0) {
				bot.msg(m.getTarget(), "14" + period.get("TITLE") + "Most Used Servers");
				int rank = 1;
				while (rs.next()) {
					String item = rs.getString(1);
					int count = rs.getInt(2);
					bot.msg(m.getTarget(), "14" + rank++ + ".7 " + item + " 14has been used7 " + count + " 14times");
				}			
			} else {
				bot.msg(m.getTarget(), "14no stats available");
			}
			// close database connection
			db.close();
		} catch (SQLException e) {
			bot.notice(m.getNickname(), "An error occured whilst retrieving pickup stats from the database");
		}
    }
    
    private void cmdTopDays(final MessageInfo m, final HashMap period)
    {
		if (fh.isFlooding("DAYS-" + period.get("TITLE") + "-" + m.getTarget().toUpperCase())) {
			return;
		}
    	// build sql query
    	String query = "SELECT DAYNAME(time) AS day, COUNT(*) AS c FROM pickups WHERE game='" + cfg.s("GAME-NAME") + "' AND " + period.get("WHERE") + " GROUP BY day ORDER BY c DESC LIMIT 10";
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve top pickup days from database
			ResultSet rs = db.query(query);
			if (db.getRowCount(rs) > 0) {
				bot.msg(m.getTarget(), "14" + period.get("TITLE") + "Top Pickup Days");
				int rank = 1;
				while (rs.next()) {
					String item = rs.getString(1);
					int count = rs.getInt(2);
					bot.msg(m.getTarget(), "14" + rank++ + ". 7" + item + " 14-7 " + count + "");
				}			
			} else {
				bot.msg(m.getTarget(), "14no stats available");
			}
			// close database connection
			db.close();
		} catch (SQLException e) {
			bot.notice(m.getNickname(), "An error occured whilst retrieving pickup stats from the database");
		}
    }

	public void cmdStats(final MessageInfo m, final String[] p)
	{
		String target;
		if (p.length == 0) {
			target = m.getNickname();
		} else {
			target = p[0];
		}
		try {
			// determine if we have stats for this user
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM pickups,pickupplayers WHERE pickupplayers.pickupid=pickups.id AND game='" + cfg.s("GAME-NAME") + "' AND nickname=? GROUP BY nickname LIMIT 1");
			ps.setString(1, target);
			ResultSet rs = ps.executeQuery();
			if (db.getRowCount(rs) > 0) {
				// get number of pickups that this player has played
				rs.next();
				int pickups = rs.getInt(1);
				// get number of times this player has admined
				String query;
				if (cfg.s("GAME-NAME").equals("Call of Duty 4")) {
					query = "SELECT COUNT(*) AS c FROM pickups WHERE game='" + cfg.s("GAME-NAME") + "' AND admin=? GROUP BY admin LIMIT 1";
				} else {
					query = "SELECT COUNT(*) AS c FROM pickups,pickupplayers WHERE game='" + cfg.s("GAME-NAME") + "' AND admin=? AND pickupplayers.pickupid=pickups.id AND pickupplayers.nickname=pickups.admin GROUP BY admin LIMIT 1";
				}
				ps = db.prepareStatement(query);
				ps.setString(1, target);
				rs = ps.executeQuery();
				int admined;
				if (db.getRowCount(rs) > 0) {
					rs.next();
					admined = rs.getInt(1);
				} else {
					admined = 0;
				}
				// show stats
				if (admined > 0) {
					bot.msg(m.getTarget(), "14" + target + " has played " + pickups + " pickup(s) and admined " + admined + " pickup(s)");
				} else {
					bot.msg(m.getTarget(), "14" + target + " has played " + pickups + " pickup(s)");
				}
			} else {
				bot.msg(m.getTarget(), "14" + target + " has not played a pickup");
			}
			// close database connection
			db.close();
		} catch (SQLException e) {
			bot.notice(m.getNickname(), "An error occured whilst retrieving pickup stats from the database");
		}
	}
	
	public void cmdSetNews(final MessageInfo m, final String[] p)
	{
		if (p.length >= 1) {
			String text = Utils.arrayToString(p);
			news = text;
			bot.notice(m.getNickname(), "topic news set to '" + news + "'");
			// update channel topic
			topic();
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !setnews (news)");
		}
	}
	
	public void cmdClearNews(final MessageInfo m, final String[] p)
	{
		news = "(no news)";
		bot.notice(m.getNickname(), "topic news cleared");
		// update channel topic
		topic();
	}
}