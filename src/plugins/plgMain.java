// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								MAIN
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.Utils;

import jirc.User;
import jirc.Channel;
import jirc.FloodHandler;

import java.io.File;

import java.util.*;

public class plgMain extends Plugin
{	
	// ********************************************************************************
	//          DECLARATIONS
	// ********************************************************************************
	
	private FloodHandler fh;					// help command flood handler

	// ********************************************************************************
	//          CONSTRUCTOR
	// ********************************************************************************
    
	public plgMain()
	{
		// initialisation
		fh = new FloodHandler(1, 240);
		// set plugin information
		name = "Main";
		author = "Matthew Goslett";
		version = "1.3";
		// register plugin commands
		ph.register("!help", "cmdHelp", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!version", "cmdVersion", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!time", "cmdTime", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!uptime", "cmdUptime", ACCESS.CHANNEL, LEVEL.USER, this);
		ph.register("!status", "cmdStatus", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!rehash", "cmdRehash", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!log", "cmdLog", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!logsearch", "cmdLogSearch", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!quit", "cmdQuit", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!die", "cmdDie", ACCESS.GLOBAL, LEVEL.USER, this, false);
		// register aliases
		ph.registerAlias("!cmds", "!help");
		ph.registerAlias("!commands", "!help");
		ph.registerAlias("!disconnect", "!quit");
		ph.registerAlias("!exit", "!die");
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onVoice(User user, Channel channel, String target) {
				onVoiceEvent(user, channel, target);
			}
		});
	}
	
	// ********************************************************************************
	//			IRC EVENTS
	// ********************************************************************************
	
	private void onVoiceEvent(User user, Channel channel, String target)
	{
		if (bot.isMyChannel(channel)) {
			if (!(user.getNickname()).equalsIgnoreCase("CHANSERV")) {
				// devoice user
				bot.deVoice(channel.getName(), target);
			}
		}
	}
	
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************

	public void cmdHelp(final MessageInfo m, final String[] p)
	{
		if (fh.isFlooding(m.getUser())) {
			return;
		}
    	bot.notice(m.getNickname(), "jEggyBot " + bot.getBotVersion() + "");
    	bot.notice(m.getNickname(), "");
    	// convert hashmap of bot commands to a vector and then sort
    	// the vector alphabetically
		Vector<String> commands = new Vector<String>((ph.getCommandList()).keySet());
		Collections.sort(commands);
    	// build a list of commands which are enabled, visible and that are accessible
    	// by this user
    	String list = "";
    	int counter = 0;
		for (int x = 0; x < commands.size(); x++) {
			BotCommand cmd = ph.getCommand(commands.get(x));
			if ((bot.isLevel(m.getNickname(), cmd.getUserLevel())) && (cmd.isEnabled()) && (cmd.isVisible())) {
				// append command to command list
				list += cmd.getTrigger() + " ";
				// increment command counter
				counter++;
			}
			if ((counter == 16) || (x == commands.size() - 1)) {
				// send list of commands
				bot.notice(m.getNickname(), list);
				// clear command list and reset counter
				counter = 0;
				list = "";
			}
		}
    	bot.notice(m.getNickname(), "");
    	bot.notice(m.getNickname(), "Copyright 2008 mrg");
	}

	public void cmdVersion(final MessageInfo m, final String[] p)
	{
		bot.notice(m.getNickname(), "I am running jEggyBot " + bot.getBotVersion() + " by mrg");
	}
    
    public void cmdTime(final MessageInfo m, final String[] p)
    {
		bot.msg(m.getTarget(), "14The time is now " + bot.getFormattedTime("HH:mm:ss, d MMMM yyyy") + "");
    }
    
    public void cmdUptime(final MessageInfo m, final String[] p)
    {
    	long seconds = bot.getUptime();
    	// format seconds into days, hours, minutes and seconds
    	String timeAgo = "";
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
		bot.msg(m.getTarget(), "14I have been up for " + timeAgo + "");
    }
    
    public void cmdStatus(final MessageInfo m, final String[] p)
    {
    	if (p.length == 0) {
    		bot.notice(m.getNickname(), "You have level " + bot.getUserLevel(m.getNickname()) + " access");
    	}
    	else {
    		bot.notice(m.getNickname(), p[0] + " has level " + bot.getUserLevel(p[0]) + " access");
    	}
    }
    
    public void cmdRehash(final MessageInfo m, final String[] p)
    {
    	// re-load all configuration files
    	bot.rehash();
    	bot.notice(m.getNickname(), "Rehashing configuration files...");
    	// log
    	bot.log(m.getNickname() + " is rehashing the bot");
    }
    
    public void cmdLog(final MessageInfo m, final String[] p)
    {
    	if (p.length == 2) {
    		String type = p[0];
    		String date = p[1];
    		if ((date.equalsIgnoreCase("TODAY")) || (date.equalsIgnoreCase("YESTERDAY")) || (date.matches("\\d{4}-\\d{2}-\\d{2}"))) {
    			// set date of log file to send
    			if (date.equalsIgnoreCase("TODAY")) {
    				date = bot.getFormattedTime("yyyy-MM-dd");
    			}
    			else if (date.equalsIgnoreCase("YESTERDAY")) {
    				// create calendar and roll it back 1 day
    				Calendar calendar = new GregorianCalendar();
    				calendar.roll(GregorianCalendar.DATE, -1);
    				date = bot.getFormattedTime(calendar.getTime(), "yyyy-MM-dd");
    			}
    			// determine type of log file to send and set filename accordingly
    			String filename;
    			if (type.equalsIgnoreCase("SYSTEM")) {
    				filename = bot.getLogDirectory() + "/gb" + date + ".log";
    			}
    			else {
    				filename = bot.getChatLogDirectory() + "/" + type.toLowerCase() + "." + date + ".log";
    			}
				// check if requested log file exists
	    		File logFile = new File(filename);
				if (logFile.exists()) {
					// dcc send log file to user
					bot.dccSend(m.getNickname(), logFile.getPath());
					bot.notice(m.getNickname(), "Sending " + type + " log file for " + date + "");
					// log
					bot.log(m.getNickname() + " requested " + type + " log file for " + date);
				}
				else {
					bot.notice(m.getNickname(), "The log file which you specified does not exist");
				}
    		}
    		else {
    			bot.notice(m.getNickname(), "You must specify a date in the format of today/yesterday/YYYY-MM-DD");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !log (system/#channel/nickname) (yyyy-mm-dd/today/yesterday)");
    	}
    }
    
    public void cmdLogSearch(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String pattern = p[0];
    		if (pattern.equalsIgnoreCase("TODAY")) {
    			pattern = ".+" + bot.getFormattedTime("yyyy-MM-dd");
    		}
    		else if (pattern.equalsIgnoreCase("YESTERDAY")) {
    			// create calendar and roll it back 1 day
    			Calendar calendar = new GregorianCalendar();
    			calendar.roll(GregorianCalendar.DATE, -1);
    			pattern = ".+" + bot.getFormattedTime(calendar.getTime(), "yyyy-MM-dd");
    		}
    		else {
		    	// replace wildcard * with .*
		    	pattern = pattern.replaceAll("\\*", ".*");
		    	// replace ? with .{1}
		    	pattern = pattern.replaceAll("\\?", ".{1}");
		    	// allow for case insensitive matching
		    	pattern = "(?i)" + pattern;
    		}
    		// append .log extension
    		pattern += "\\.log";
    		// get list of log files in chatlog directory
    		File directory = new File(bot.getChatLogDirectory());
    		Vector<String> matches = new Vector<String>();
			for (String filename: directory.list()) {
				if (filename.matches(pattern)) {
					// we found a match
					matches.add(filename);
				}
			}
			bot.notice(m.getNickname(), "Searching for " + pattern + "...");
			if (matches.size() >= 25) {
				bot.notice(m.getNickname(), "too many matches, please simplify pattern");
			}
			else if (matches.size() > 0) {
				// build single list of matches
				String list = "";
				int counter = 0;
				for (int x = 0; x < matches.size(); x++) {
					list += matches.get(x) + ", ";
					counter++;
					if ((counter == 6) || (x == matches.size() - 1)) {
						// send list of matches
						bot.notice(m.getNickname(), list);
						// reset counter
						counter = 0;
						// clear command list
						list = "";
					}
				}
			}
			else {
				bot.notice(m.getNickname(), "no matches found");
			}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !logsearch (today/yesterday/pattern)");
    	}
    }
    
    public void cmdQuit(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String reason = Utils.arrayToString(p);
    		// quit with reason
    		bot.quit(reason);
    	}
    	else {
    		// quit
    		bot.quit();
    	}
    	// log
    	bot.log(m.getNickname() + " disconnected the bot from the server");
    }
    
    public void cmdDie(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String password = p[0];
    		if (password.equals(bot.s("DIE-PASSWORD"))) {
    			// log
    			bot.log(m.getNickname() + " used the !die command to kill the bot");
    			// close application
    			System.exit(0);
    		}
    		else {
    			bot.log(m.getNickname() + " attempted to use the !die command");
    		}
    	}
    }
}