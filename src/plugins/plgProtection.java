// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PROTECTION
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jirc.Channel;
import jirc.User;

import jeggybot.util.*;

import java.util.*;
import java.sql.*;

public class plgProtection extends Plugin
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private HashMap<String, Integer> warningList;	// warning counter for users who have flooded
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgProtection()
	{
		// initialisation
		warningList = new HashMap<String, Integer>();
		// set plugin information
		name = "Protection";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!ignore", "cmdIgnore", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!unignore", "cmdUnignore", ACCESS.GLOBAL, LEVEL.SOP, this);
		//ph.register("!clonescan", "cmdCloneScan", ACCESS.GLOBAL, LEVEL.SOP, this);
		//ph.register("!findhost", "cmdFindHost", ACCESS.GLOBAL, LEVEL.SOP, this);
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onBan(User user, Channel channel, String hostmask) {
				onBanEvent(user, channel, hostmask);
			}
			public void onCommandFlood(User user, String command) {
				onCommandFloodEvent(user, command);
			}
		});
		// load ignore list
		if (!loadIgnoreList()) {
			bot.log("I could not load the ignore list");
		}
	}

	// ********************************************************************************
    //          PRIVATE METHODS
    // ********************************************************************************
	
	private boolean loadIgnoreList()
	{
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve list of hostmasks from database
			ResultSet rs = db.query("SELECT * FROM " + bot.s("TABLE-PREFIX") + "ignores");
			while (rs.next()) {
				String hostmask = rs.getString("hostmask");
				// ignore this hostmask
				bot.ignore(hostmask);
			}
			// close database connection
			db.close();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}
	
	// ********************************************************************************
	//          IRC EVENTS
	// ********************************************************************************

	private void onBanEvent(User user, Channel channel, String hostmask)
	{
		// the following code caused problems when certain characters were matched in the regular expression
		// have temporarily disabled it until this bug can be fixed
		/*
		if (bot.isMyChannel(channel)) {
			User hm = new User(bot.getHostmask());
			if (hm.matches(hostmask)) {
				// if the ban matches the bot, remove ban
				bot.unban(channel.getName(), hostmask);
			}
		}
		*/
	}
    
    private void onCommandFloodEvent(User user, String command)
    {
		// determine if this user has had any prior warnings
		int warnings;
		if (warningList.containsKey(user.getHostmask())) {
			// get number of warnings for this user
			warnings = warningList.get(user.getHostmask());
			// increment number of warnings
			warnings++;
			warningList.put(user.getHostmask(), warnings);
		}
		else {
			// create new counter for this user
			warnings = 1;
			warningList.put(user.getHostmask(), 1);
		}
		// determine appropriate punishment for user
		switch (warnings) {
			case 1:
				bot.notice(user.getNickname(), "This is your 1st warning for spamming/flooding the bot");
				break;
			case 2:
				// kick user from bot channel
				bot.kick(user.getNickname(), bot.s("CHANNEL"), "flooding");
				bot.notice(user.getNickname(), "This is your 2nd warning for spamming/flooding the bot");
				break;
			case 3:
				// ignore this user
				bot.ignore(user.getNickname() + "!*@*");
				bot.ignore("*!*@" + user.getHostname());
				// kick ban user from bot channel
				bot.kickban(bot.s("CHANNEL"), user.getNickname(), "flooding");
				bot.notice(user.getNickname(), "This is your 3rd and final warning for spamming/flooding the bot");
				// log
				bot.log(user.getNickname() + " is being automatically ignored for flooding");
				break;
		}
    }
    
	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
	
    public void cmdIgnore(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String hostmask = p[0];
    		if (hostmask.matches(".+!.+@.+")) {
    			if (!hostmask.equals("*!*@*")) {
	    			if (!bot.isIgnored(hostmask)) {
	    				try {
							// attempt to add hostmask to database
							// connect to database
							DatabaseConnection db = bot.getDatabaseConnection();
							// insert hostmask into ignores table
							PreparedStatement ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "ignores VALUES (?)");
							ps.setString(1, hostmask);
							ps.executeUpdate();
							// close database connection
							db.close();
		    				// ignore hostmask in client
		    				bot.ignore(hostmask);
		    				// notice user
		    				bot.notice(m.getNickname(), "" + hostmask + " has been added to my ignore list");
		    				// log
		    				bot.log(m.getNickname() + " added " + hostmask + " to my ignore list");
	    				}
	    				catch (SQLException e) {
	    					bot.notice(m.getNickname(), "An error occured whilst adding hostmask to database");
	    				}
	    			}
	    			else {
	    				bot.notice(m.getNickname(), "" + hostmask + " is already in my ignore list");
	    			}	
    			}
    			else {
    				bot.notice(m.getNickname(), "I cannot ignore *!*@* as all users would be ignored");
    			}
    		}
			else {
				bot.notice(m.getNickname(), "You must specify a hostmask in the form of nickname!ident@hostname");
			}
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !ignore (hostmask)");
    	}
    }
    
    public void cmdUnignore(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
			String hostmask = p[0];
			if (hostmask.matches(".+!.+@.+")) {
				if (bot.isIgnored(hostmask)) {
					try {
						// attempt to remove hostmask from database
						// connect to database
						DatabaseConnection db = bot.getDatabaseConnection();
						// delete hostmask from ignores table
						PreparedStatement ps = db.prepareStatement("DELETE FROM " + bot.s("TABLE-PREFIX") + "ignores WHERE hostmask=?");
						ps.setString(1, hostmask);
						ps.executeUpdate();
						// close database connection
						db.close();
						// unignore hostmask from client
						bot.unignore(hostmask);
	    				// notice user
	    				bot.notice(m.getNickname(), "" + hostmask + " has been removed from my ignore list");
	    				// log
	    				bot.log(m.getNickname() + " removed " + hostmask + " from my ignore list");
					}
					catch (SQLException e) {
						bot.notice(m.getNickname(), "An error occured whilst removing hostmask from database");
					}
				}
				else {
					bot.notice(m.getNickname(), "" + hostmask + " is not in my ignore list");
				}
			}
			else {
				bot.notice(m.getNickname(), "You must specify a hostmask in the form of nickname!ident@hostname");
			}
		}
    	else {
			bot.notice(m.getNickname(), "Syntax: !unignore (hostmask)");
    	}
    }
    
    public void cmdCloneScan(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String channelname = p[0];
    		if (bot.isOn(channelname)) {
    			// get user list from channel
    			Channel channel = bot.getChannel(channelname);
    			HashMap users = channel.getUsers();
				// convert hashmap values to arraylist
				ArrayList<User> al = new ArrayList<User>(users.values());
				// find duplicate hostnames in array list
				HashMap<String, Vector> duplicates = new HashMap<String, Vector>();
				User user1;
				for (int x = 0; x < al.size(); x++) {
					user1 = al.get(x);
					if (!duplicates.containsKey(user1.getHostname())) {
						// create blank list of duplicate users for this hostname
						Vector dusers = new Vector();
						// loop through each user and match hostnames
						for (int y = x + 1; y < al.size(); y++) {
							User user2 = al.get(y);
							if (user1.getHostname().equals(user2.getHostname())) {
								// clone detected
								dusers.add(user2);
							}
						}
						if (!dusers.isEmpty()) {
							// we have found 1 or more clones for this hostname
							// add initial user to duplicate list
							dusers.add(user1);
							// add hostname and list of duplicate users to duplicate list
							duplicates.put(user1.getHostname(), dusers);
						}
					}
				}
				// notice user list of clones in channel
				bot.notice(m.getNickname(), "Clone Scan of " + channelname + "");
				if (duplicates.size() > 0) {
					Iterator it = (duplicates.keySet()).iterator();
					while (it.hasNext()) {
						String hostname = (String) it.next();
						Vector dusers = duplicates.get(hostname);
						bot.notice(m.getNickname(), "" + hostname + " (" + dusers.size() + " clones)");
						for (int x = 0; x < dusers.size(); x++) {
							User user = (User) dusers.get(x);
							bot.notice(m.getNickname(), "o " + user.getNickname());
						}
					}
				}
				else {
					bot.notice(m.getNickname(), "no clones found");
				}
    		}
    		else {
    			bot.notice(m.getNickname(), "I am not in " + channelname + "");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !clonescan (#channel)");
    	}
    }
    
    public void cmdFindHost(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String hostmask = p[0];
    		if (hostmask.matches(".+!.+@.+")) {
    			// scan for host in each channel
    			HashMap<String, User> matches = new HashMap<String, User>();
				Iterator it = (bot.getChannels().values()).iterator();
				while (it.hasNext()) {
					Channel channel = (Channel) it.next();
					// get hashmap of users on this channel
					HashMap users = channel.getUsers();
					// loop through each user and see if user matches this hostmask
					Iterator it2 = (users.values()).iterator();
					while (it2.hasNext()) {
						User user = (User) it2.next();
						if (user.matches(hostmask)) {
							// match found
							matches.put(user.getNickname(), user);
						}
					}
				}
				// notice user list of matches
				bot.notice(m.getNickname(), "Searching for " + hostmask + "...");
				if (matches.size() >= 6) {
					bot.notice(m.getNickname(), "too many matches, please simplify your hostmask");
				}
				else if (matches.size() > 0) {
					Iterator it3 = (matches.values()).iterator();
					while (it3.hasNext()) {
						User user = (User) it3.next();
						bot.notice(m.getNickname(), "o " + user.getNickname() + " (" + user.getIdent() + "@" + user.getHostname() + ")");
					}
				}
				else {
					bot.notice(m.getNickname(), "no users found");
				}
    		}
    		else {
    			bot.notice(m.getNickname(), "You must specify a hostmask in the form of nickname!ident@hostname");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !findhost (hostmask)");
    	}
    }
}