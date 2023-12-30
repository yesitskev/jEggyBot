// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								SEEN
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.DatabaseConnection;
import jeggybot.util.Utils;

import jirc.Channel;
import jirc.User;

import java.sql.*;

public class plgSeen extends Plugin
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	// define silly, random messages to be displayed when the user tries to be
	// smart with the bot.
	private String[] seenBot = {
					"Who on earth is that, and why would I know him?",
					"Don't be dumb, I'm right here",
					"Correct me if I'm wrong, but you're trying to find me?",
					"The last time I saw me was now me thinks",
					"There's always a smart one in the bunch",
					"I was last seen replying to a dumb idiot right now"
			};
	private String[] seenSelf = {
					"You can look for yourself idiot",
					"You're not very smart are you?",
					"You're right here dumbass",
					"Try looking in the mirror buddy",
					"If I weren't so smart, I'd probably display a proper reply",
					"You think anyone cares when you were last seen?"
			};
	private String[] seenChan = {
					"$target is right here in the channel",
					"You must be blind, because $target is here",
					"$target's in this channel",
					"$target is currently online in this channel",
					"Look for yourself, $target's here",
					"Are you by chance a dutchman? $target is here"
			};
	private String[] seenUnknown = {
					"Who the **** is $target?",
					"I've never seen $target before",
					"You must introduce me to $target some day",
					"I don't know $target, but ask mrg, he's a smart guy",
					"hahaha, is $target someone's real nickname!?",
					"Is $target hot, cause I'd love to meet her."
			};

	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgSeen()
	{
		// set plugin information
		name = "Seen";
		author = "Matthew Goslett";
		version = "1.4";
		// register plugin commands
		ph.register("!seen", "cmdSeen", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!resetseen", "cmdResetSeen", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
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
		});
	}
	
	// ********************************************************************************
    //          PRIVATE METHODS
    // ********************************************************************************
	
	private void updateSeen(String nickname, String action)
	{
		if (bot.s("SEEN-ENABLED").equalsIgnoreCase("ON")) {
			try {
				// add/update user's seen information in database
				// connect to database
				DatabaseConnection db = bot.getDatabaseConnection();
				// first deteremine whether we have a seen record for this nickname
				PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "seen WHERE nickname=?");
				ps.setString(1, nickname);
				ResultSet rs = ps.executeQuery();
				rs.next();
				int rows = rs.getInt(1);
				if (rows > 0) {
					// update seen data
					ps = db.prepareStatement("UPDATE " + bot.s("TABLE-PREFIX") + "seen SET action=?, lastseen=NOW() WHERE nickname=?");
					ps.setString(1, action);
					ps.setString(2, nickname);
				}
				else {
					// insert seen data
					ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "seen (nickname, action) VALUES (?, ?)");
					ps.setString(1, nickname);
					ps.setString(2, action);
				}
				ps.executeUpdate();
				// close database connection
				db.close();
			}
			catch (SQLException e) {
			}
		}
	}
	
	// ********************************************************************************
    //          IRC EVENTS
    // ********************************************************************************
    
    private void onJoinEvent(User user, Channel channel)
    {
    	if (bot.s("SEEN-JOIN").equalsIgnoreCase("ON")) {
    		updateSeen(user.getNickname(), "joining " + channel.getName());
    	}
    }
    
    private void onPartEvent(User user, Channel channel)
    {
    	if (bot.s("SEEN-PART").equalsIgnoreCase("ON")) {
    		updateSeen(user.getNickname(), "leaving " + channel.getName());
    	}
    }
	
	private void onNickChangeEvent(User user, String newnickname)
	{
    	if (bot.s("SEEN-NICK").equalsIgnoreCase("ON")) {
    		updateSeen(user.getNickname(), "changing his nick to " + newnickname);
    	}
	}
    
    private void onKickEvent(User user, String target, Channel channel, String reason)
    {
    	if (bot.s("SEEN-KICK").equalsIgnoreCase("ON")) {
    		updateSeen(target, "being kicked from " + channel.getName() + " by " + user.getNickname());
    		updateSeen(user.getNickname(), "kicking " + target + " from " + channel.getName());
    	}
    }
    
    private void onQuitEvent(User user, String reason)
    {
    	if (bot.s("SEEN-QUIT").equalsIgnoreCase("ON")) {
    		updateSeen(user.getNickname(), "quitting the server");
    	}
    }
    
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************
    
    public void cmdSeen(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String target = p[0];
    		// if the user tries to get the last seen time of the bot, himself or a user who
    		// is currently online, send a funny reply
			if (bot.isMe(target)) {
				bot.msg(m.getTarget(), "14" + seenBot[Utils.rand(seenBot.length)] + "");
				return;
			}
			if (target.equalsIgnoreCase(m.getNickname())) {
				bot.msg(m.getTarget(), "14" + seenSelf[Utils.rand(seenSelf.length)] + "");
				return;
			}
			if (bot.isOn(target, m.getTarget())) {
				bot.msg(m.getTarget(), "14" + seenChan[Utils.rand(seenChan.length)].replace("$target", target) + "");
				return;
			}
			try {
				// connect to database
				DatabaseConnection db = bot.getDatabaseConnection();
				// retrieve target's seen information from our database
				PreparedStatement ps = db.prepareStatement("SELECT action, lastseen, TIMESTAMPDIFF(SECOND, lastseen, now()) AS seconds FROM " + bot.s("TABLE-PREFIX") + "seen WHERE nickname=?");
				ps.setString(1, target);
				ResultSet rs = ps.executeQuery();
				if (db.getRowCount(rs) > 0) {
					rs.next();
					String action = rs.getString("action");
					Timestamp ts = rs.getTimestamp("lastseen");
					long seconds = rs.getLong("seconds");
					// format the difference as days, hours, minutes and seconds
					String timeAgo = "";
					if (seconds < 60) {
						timeAgo = "less then 1 minute";
					}
					else if (seconds < 120) {
						timeAgo = "less then 2 minutes";
					}
					else {
						int days = (int) seconds / 86400;
						seconds = seconds - (days * 86400);
						int hours = (int) seconds / 3600;
						seconds = seconds - (hours * 3600);
						int minutes = (int) seconds / 60;
						seconds = seconds - (minutes * 60);
						if (days > 0)		timeAgo += "" + days + " days, ";
						if (hours > 0)		timeAgo += "" + hours + " hours, ";
						if (minutes > 0)	timeAgo += "" + minutes + " minutes, ";
						if (seconds > 0)	timeAgo += "" + seconds + " seconds";
					}
					// send formatted reply
					bot.msg(m.getTarget(), "14" + target + " was last seen " + action + " " + timeAgo + " ago");
				}
				else {
					bot.msg(m.getTarget(), "14" + seenUnknown[Utils.rand(seenUnknown.length)].replace("$target", target) + "");
				}
			}
			catch (SQLException e) {
				bot.notice(m.getNickname(), "An error occured whilst retrieving seen information");
			}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !seen (target)");
    	}
    }
}