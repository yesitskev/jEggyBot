// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								GREET
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.DatabaseConnection;
import jeggybot.util.Utils;

import jirc.Channel;
import jirc.User;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class plgGreet extends Plugin
{
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgGreet()
	{
		// set plugin information
		name = "Channel Greet";
		author = "Matthew Goslett";
		version = "1.1";
		// register plugin commands
		ph.register("!rules", "cmdRules", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!addgreet", "cmdAddGreet", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!delgreet", "cmdDelGreet", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!set_join_greet", "cmdSetJoinGreet", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!set_part_greet", "cmdSetPartGreet", ACCESS.GLOBAL, LEVEL.SOP, this);
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onJoin(User user, Channel channel) {
				onJoinEvent(user, channel);
			}
			public void onPart(User user, Channel channel) {
				onPartEvent(user, channel);
			}
		});
	}
	
	// ********************************************************************************
    //          IRC EVENTS
    // ********************************************************************************
    
    private void onJoinEvent(User user, Channel channel)
    {
		if ((bot.isMyChannel(channel)) && (!bot.isMe(user))) {
			// greet the user
			if (bot.s("AUTO-GREET-JOIN").equalsIgnoreCase("ON")) {
				// replace identifiers in custom greet message, and notice user
				String message = bot.s("JOIN-MESSAGE");
				message = message.replace("$channel", channel.getName());
				message = message.replace("$nickname", user.getNickname());
				bot.notice(user.getNickname(), message);
				bot.notice(user.getNickname(), "Type !rules to view a copy of the channel rules, and !help if you need assistance.");
			}
			if (bot.s("CUSTOM-GREETS").equalsIgnoreCase("ON")) {
				try {
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// retrieve target's seen information from our database
					PreparedStatement ps = db.prepareStatement("SELECT message FROM " + bot.s("TABLE-PREFIX") + "greets WHERE nickname=?");
					ps.setString(1, user.getNickname());
					ResultSet rs = ps.executeQuery();
					if (db.getRowCount(rs) > 0) {
						rs.next();
						// display greeting in channel
						String message = rs.getString("message");
						bot.msg(channel.getName(), "14" + message + "");
					}
					// close database connection
					db.close();
				}
				catch (SQLException e) {
				}
			}
		}
    }
    
    private void onPartEvent(User user, Channel channel)
    {
		if ((bot.isMyChannel(channel)) && (!bot.isMe(user))) {
			// say goodbye to user
			if (bot.s("AUTO-GREET-PART").equalsIgnoreCase("ON")) {
				// replace identifiers in custom greet message, and notice user
				String message = bot.s("PART-MESSAGE");
				message = message.replace("$channel", channel.getName());
				message = message.replace("$nickname", user.getNickname());
				bot.notice(user.getNickname(), message);
			}
		}
    }
    
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************
    
    public void cmdRules(final MessageInfo m, final String[] p)
    {
    	// dcc send user a copy of the channel rules
    	bot.dccSend(m.getNickname(), bot.s("RULES-FILE"));
    }
    
    public void cmdAddGreet(final MessageInfo m, final String[] p)
    {
    	if (bot.s("CUSTOM-GREETS").equalsIgnoreCase("ON")) {
	    	if (p.length >= 2) {
	    		String nickname = p[0];
	    		String message = Utils.arrayToString(p, 1);
				try {
					// add the user greeting to database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// first deteremine whether the greeting already exists
					PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "greets WHERE nickname=?");
					ps.setString(1, nickname);
					ResultSet rs = ps.executeQuery();
					rs.next();
					int rows = rs.getInt(1);
					if (rows == 0) {
						// add greeting to database
						ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "greets VALUES (?, ?)");
						ps.setString(1, nickname);
						ps.setString(2, message);
						ps.executeUpdate();
						// notice user
						bot.notice(m.getNickname(), "Added greeting for " + nickname + "");
						// log
						bot.log(m.getNickname() + " added greet message for " + nickname);
					}
					else {
						bot.notice(m.getNickname(), "" + nickname + " already has a greeting");
					}
					// close database connection
					db.close();
				}
				catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst adding greet message to database");
				}
	    	}
	    	else {
	    		bot.notice(m.getNickname(), "Syntax: !addgreet (nickname) (message)");
	    	}
    	}
    	else {
    		bot.notice(m.getNickname(), "Custom greet messages have been disabled");
    	}	
    }
    
    public void cmdDelGreet(final MessageInfo m, final String[] p)
    {
    	if (bot.s("CUSTOM-GREETS").equalsIgnoreCase("ON")) {
			if (p.length == 1) {
				String nickname = p[0];
				try {
					// remove greeting from database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// first deteremine whether the greeting exists
					PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "greets WHERE nickname=?");
					ps.setString(1, nickname);
					ResultSet rs = ps.executeQuery();
					rs.next();
					int rows = rs.getInt(1);
					if (rows > 0) {
						// delete the greeting
						ps = db.prepareStatement("DELETE FROM " + bot.s("TABLE-PREFIX") + "greets WHERE nickname=?");
						ps.setString(1, nickname);
						ps.executeUpdate();
						// notice user
						bot.notice(m.getNickname(), "Removed greet message for " + nickname + "");
						// log
						bot.log(m.getNickname() + " removed greet message for " + nickname);
					}
					else {
						bot.notice(m.getNickname(), "" + nickname + " does not have a greet message");
					}
					// close database connection
					db.close();
				}
				catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst removing greet message from database");
				}
			}
			else {
				bot.notice(m.getNickname(), "Syntax: !delgreet (nickname)");
			}	
    	}
    	else {
    		bot.notice(m.getNickname(), "Custom greet messages have been disabled");
    	}
    }
    
    public void cmdSetJoinGreet(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String greeting = Utils.arrayToString(p);
    		// set new join greet message
    		bot.setValue("JOIN-MESSAGE", greeting);
    		bot.notice(m.getNickname(), "Join greet message has been changed");
    		// log
    		bot.log(m.getNickname() + " changed the join greet message");
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !set_join_greet (greeting)");
    	}
    }
    
    public void cmdSetPartGreet(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String greeting = Utils.arrayToString(p);
    		// set new part greet message
    		bot.setValue("PART-MESSAGE", greeting);
    		bot.notice(m.getNickname(), "Part greet message has been changed");
    		// log
    		bot.log(m.getNickname() + " changed the part greet message");
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !set_part_greet (greeting)");
    	}
    }
}