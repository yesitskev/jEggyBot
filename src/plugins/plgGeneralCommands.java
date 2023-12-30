// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								GENERAL COMMANDS
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.Utils;

public class plgGeneralCommands extends Plugin
{	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgGeneralCommands()
	{
		// set plugin information
		name = "General Commands";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!join", "cmdJoin", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!part", "cmdPart", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!msg", "cmdMsg", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!amsg", "cmdAMsg", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!notice", "cmdNotice", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!kick", "cmdKick", ACCESS.GLOBAL, LEVEL.OP, this);
		ph.register("!ban", "cmdBan", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!kickban", "cmdKickBan", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!raw", "cmdRaw", ACCESS.GLOBAL, LEVEL.USER, this, false);
		// register aliases
		ph.registerAlias("!j", "!join");
		ph.registerAlias("!p", "!part");
		ph.registerAlias("!kb", "!kickban");
		ph.registerAlias("!bankick", "!kickban");
	}
	
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************
    
    public void cmdJoin(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
	    	String channel = p[0];
	    	if (!bot.isOn(channel)) {
	    		// attempt to join channel
	    		bot.notice(m.getNickname(), "Attempting to join " + channel + "");
	    		bot.join(channel);
	    		// log
	    		bot.log(m.getNickname() + " used !join to make bot join " + channel);
	    	}
	    	else {
	    		bot.notice(m.getNickname(), "I'm already on " + channel);
	    	}
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !join (#channel)");
    	}
    }
    
    public void cmdPart(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
	    	String channel = p[0];
	    	if (bot.isOn(channel)) {
	    		if (!channel.equalsIgnoreCase(bot.s("CHANNEL"))) {
	    			// part channel
	    			bot.part(channel);
	    			bot.notice(m.getNickname(), "I have parted " + channel + "");
	    			// log
	    			bot.log(m.getNickname() + " used !part to make bot leave " + channel);
	    		}
	    		else {
	    			bot.notice(m.getNickname(), "I cannot leave this channel");
	    		}
	    	}
	    	else {
				bot.notice(m.getNickname(), "I'm not in " + channel);
	    	}
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !part (#channel)");
    	}
    }
    
    public void cmdMsg(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 2) {
    		String target = p[0];
    		String message = Utils.arrayToString(p, 1);
			// message target
			bot.msg(target, message);
			// log
			bot.log(m.getNickname() + " used !msg to message " + target);
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !msg (target) (message)");
    	}
    }
    
    public void cmdAMsg(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String message = Utils.arrayToString(p);
    		// send message to all channels
    		bot.amsg(message);
    		// log
    		bot.log(m.getNickname() + " used !amsg to message all channels");
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !amsg (message)");
    	}
    }
    
    public void cmdNotice(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 2) {
    		String target = p[0];
    		String message = Utils.arrayToString(p, 1);
			// notice target
			bot.notice(target, message);
			// log
			bot.log(m.getNickname() + " used !notice to message " + target);
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !notice (target) (message)");
    	}
    }
    
    public void cmdKick(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 2) {
    		String channel = p[0];
    		String target = p[1];
    		// set kick reason
    		String reason;
    		if (p.length == 2) {
    			// use admin's nickname as reason
    			reason = m.getNickname();
    		}
    		else {
    			// user has specified a kick reason
    			reason = Utils.arrayToString(p, 2);
    		}
    		if (bot.isOn(channel)) {
				if (!bot.isMe(target)) {
					// attempt to kick target from the channel, with set reason
					bot.notice(m.getNickname(), "Attempting to kick " + target + " from " + channel + "");
					bot.kick(target, channel, reason);
					// log
					bot.log(m.getNickname() + " used !kick to remove " + target + " from " + channel);
				}
				else {
					bot.notice(m.getNickname(), "I will not kick myself!");
				}
    		}
    		else {
				bot.notice(m.getNickname(), "I am not in " + channel + "");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !kick (#channel) (target) [reason]");
    	}
    }
    
    public void cmdBan(final MessageInfo m, final String[] p)
    {
    	if (p.length == 2) {
    		String channel = p[0];
    		String target = p[1];
    		if (bot.isOn(channel)) {
    			// attempt to ban user
    			bot.ban(channel, target);
    			bot.notice(m.getNickname(), "Attempting to ban " + target + " from " + channel + "");
    			// log
    			bot.log(m.getNickname() + " used !ban to ban " + target + " from " + channel);
    		}
    		else {
    			bot.notice(m.getNickname(), "I am not in " + channel + "");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !ban (#channel) (target)");
    	}
    }
    
    public void cmdKickBan(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 2) {
    		String channel = p[0];
    		String target = p[1];
    		// set kick reason
    		String reason;
    		if (p.length == 2) {
    			// use admin's nickname as reason
    			reason = m.getNickname();
    		}
    		else {
    			// user has specified a kick reason
    			reason = Utils.arrayToString(p, 2);
    		}
    		if (bot.isOn(channel)) {
				if (!bot.isMe(target)) {
					// attempt to kickban target from the channel
					bot.notice(m.getNickname(), "Attempting to kickban " + target + " from " + channel + "");
					bot.kickban(channel, target, reason);
					// log
					bot.log(m.getNickname() + " used !kickban to remove " + target + " from " + channel);
				}
				else {
					bot.notice(m.getNickname(), "I will not kickban myself!");
				}
    		}
    		else {
				bot.notice(m.getNickname(), "I am not in " + channel + "");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !kickban (#channel) (target) [reason]");
    	}
    }
    
    public void cmdRaw(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 2) {
    		String password = p[0];
    		String command = Utils.arrayToString(p, 1);
			if (password.equals(bot.s("RAW-PASSWORD"))) {
				// send raw line
				bot.sendraw(command);
				// log
				bot.log(m.getNickname() + " used the !raw command (" + command + ")");
			}
			else {
				bot.log(m.getNickname() + " attempted to use the !raw command (" + command + ")");
			}
    	}
    }
}