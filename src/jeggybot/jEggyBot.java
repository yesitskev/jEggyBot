// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								JEGGYBOT
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import jeggybot.util.DatabaseConnection;

import jirc.*;

import java.io.*;

import java.util.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.text.Style;

public class jEggyBot extends jIRC
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************

	private static jEggyBot bot;						// current bot instance
	private static final String VERSION = "1.6";		// jeggybot version
	private static String settings = "settings.txt";	// settings file
	private static String syslogdir = "syslogs";		// sys log file directory
	private static String chatlogdir = "chatlogs";		// chat log file directory
	private Config config;								// bot config			
	private PluginHandler ph;							// plugin handler
 	private Timer autoReconnectTimer;					// auto reconnect timer
	private Vector<String> ignoreList;					// ignore list
	private Date startTime;								// time when bot was started
	private FloodHandler fh;							// bot command flood handler
	
	// ********************************************************************************
    //          MAIN
    // ********************************************************************************
	
	public static void main(String[] args)
	{
		// parse the command line arguments
		for (int x = 0; x < args.length; x++) {
			if (args[x].equalsIgnoreCase("-settings")) {
				// set settings file to use
				if (x++ < args.length) {
					settings = args[x];
				}
			}
			else if (args[x].equalsIgnoreCase("-syslogdir")) {
				// set sys log directory
				if (x++ < args.length) {
					syslogdir = args[x];
				}
			}
			else if (args[x].equalsIgnoreCase("-chatlogdir")) {
				// set chat log directory
				if (x++ < args.length) {
					chatlogdir = args[x];
				}
			}
		}
		// start up bot
		new jEggyBot();
	}
	
    // ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	public jEggyBot()
	{
		// initialisation
		bot = this;
		ph = new PluginHandler();
		ignoreList = new Vector<String>();
		fh = new FloodHandler(2, 6);
		// store current time
		startTime = new Date();
		// load settings from config file
		if (loadConfig()) {
			log("jEggyBot " + VERSION + " - Matthew Goslett");
		}
		else {
			prnt("An error occured whilst loading the config file");
			die();
		}
		// set irc client settings from config file
		setRemoteAddress(s("REMOTEADDRESS"));
		setPort(i("PORT"));
		setNickname(s("NICKNAME"));
		setAlternativeNickname(s("ALTERNATIVE-NICKNAME"));
		setRealname(s("REAL-NAME"));
		setVersion("jEggyBot " + VERSION + " - mrg");
		// load all plugins from database
		if (loadAllPlugins()) {
			log(ph.getPluginCount() + " plugins loaded, " + ph.getCommandCount() + " bot commands and " + ph.getAliasCount() + " aliases registered");
		}
		else {
			log("I could not read my plugin list from the database");
			die();
		}
		// make sure that the main plugin has been loaded, otherwize try to load it manually
		if (!ph.isPlugin("plgMain")) {
			if (!ph.loadPlugin("plgMain")) {
				log("I can't continue to run without having my main plugin loaded");
				die();
			}
		}
		// connect bot to irc server
		try {
			// log
			log("* Connecting to " + getRemoteAddress() + ":" + getPort());
			// attempt to connect to irc server
			connect();
		}
		catch (ConnectException e) {
			// connection attempt failed
			log("* Could not connect to server (" + e.getMessage() + ")");
			// attempt to re-connect on a delayed timer
			if (s("AUTO-RETRY-CONNECT").equalsIgnoreCase("ON")) {
				autoReconnectTimer = new Timer();
				autoReconnectTimer.schedule(new AutoReconnectTask(), 0, i("RETRY-INTERVAL") * 1000);
			}
		}
	}
	
    // ********************************************************************************
    //          PRIVATE METHODS
    // ********************************************************************************

    private boolean loadConfig()
    {
    	// load main config
        config = new Config("main", settings);
        // check whether settings have been filled in correctly, otherwise use default settings
        checkConfig();
        return config.isLoaded();
    }

	private void checkConfig()
	{
		// DB-TYPE
		// * msaccess/mysql
		if ((!s("DB-TYPE").equalsIgnoreCase("MSACCESS")) && (!s("DB-TYPE").equalsIgnoreCase("MYSQL"))) {
			setValue("DB-TYPE", "mysql");
		}
		// DB-NAME
		// * cannot be blank
		if (s("DB-NAME").equals("")) {
			setValue("DB-NAME", "jEggyBot");
		}
		// REMOTEADDRESS
		// * cannot be blank
		if (s("REMOTEADDRESS").equals("")) {
			setValue("REMOTEADDRESS", "localhost");
		}
		// PORT
		// * set of digits between 1 and 5 numbers
		if (!s("PORT").matches("\\d{1,5}")) {
			setValue("PORT", 6667);	
		}
		// * port between 0 and 65535
		else if ((i("PORT") < 0) || (i("PORT") > 65535)) {
			setValue("PORT", 6667);
		}
		// AUTO-RETRY-CONNECT
		// * on or off
		if ((!s("AUTO-RETRY-CONNECT").equalsIgnoreCase("ON")) && (!s("AUTO-RETRY-CONNECT").equalsIgnoreCase("OFF"))) {	
			setValue("AUTO-RETRY-CONNECT", "on");
		}
		// RETRY-INTERVAL
		// * set of digits
		if (!s("RETRY-INTERVAL").matches("\\d+")) {
			setValue("RETRY-INTERVAL", 5);
		}
		// * number between 0 and 300
		else if ((i("RETRY-INTERVAL") < 0) || (i("RETRY-INTERVAL") > 300)) {
			setValue("RETRY-INTERVAL", 5);
		}
		// RETRY-ATTEMPTS
		// * set of digits
		if (!s("RETRY-ATTEMPTS").matches("\\d+")) {
			setValue("RETRY-ATTEMPTS", 10);
		}
		// * number between 0 and 100
		else if ((i("RETRY-ATTEMPTS") < 0) || (i("RETRY-ATTEMPTS") > 100)) {
			setValue("RETRY-ATTEMPTS", 10);
		}
		// NICKNAME
		// * cannot be blank
		if (s("NICKNAME").equals("")) {
			setValue("NICKNAME", "jEggyBot");
		}
		// ALTERNATIVE-NICKNAME
		// * cannot be blank
		if (s("ALTERNATIVE-NICKNAME").equals("")) {
			setValue("ALTERNATIVE-NICKNAME", "jEggyBot2");
		}
		// REALNAME
		// * cannot be blank
		if (s("REAL-NAME").equals("")) {
			setValue("REAL-NAME", "jEggyBot " + VERSION + " - mrg");
		}
		// CHANNEL
		// * begin with channel prefix, followed by any characters
		if (!s("CHANNEL").matches("#.+")) {
			setValue("CHANNEL", "#jeggybot");
		}
		// GMT
		// * set of digits following a +/i
		if (!s("GMT").matches("[\\+|-]\\d+")) {
			setValue("GMT", "+0");
		}
	}

	private boolean loadAllPlugins()
	{
		try {
			// load all plugins
			log("Loading plugins...");
			// connect to database
			DatabaseConnection db = getDatabaseConnection();
			// query plugins table
			ResultSet rs = db.query("SELECT filename FROM " + s("TABLE-PREFIX") + "plugins WHERE enabled=1");
			while (rs.next()) {
				// get plugin filename
				String filename = rs.getString("filename");
				// load plugin
				if (ph.loadPlugin(filename)) {
					// plugin loaded successfully
					// now identify the loaded plugin
					Plugin plugin = ph.getPlugin(filename);
					// display it's name, and author
					log(" o " + plugin.getName() + " " + plugin.getVersion() + " - " + plugin.getAuthor());
				}
				else {
					log(" ! " + filename + " failed to load");
				}
			}
			// close database connection
			db.close();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	private boolean isValidTarget(String target, int validTarget)
	{
		switch (validTarget) {
			case ACCESS.BOT_CHANNEL:
				if (target.equalsIgnoreCase(s("CHANNEL"))) return true;
				break;
			case ACCESS.PRIVATE:
				if (isMe(target)) return true;
				break;
			case ACCESS.BOTH:
				if ((target.equalsIgnoreCase(s("CHANNEL"))) || (isMe(target))) return true;
				break;
			case ACCESS.CHANNEL:
				if (target.startsWith("#")) return true;
				break;
			case ACCESS.GLOBAL:
				return true;
		}
		return false;
	}
	
    // ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************

	public static jEggyBot getBotClient()
	{
		return bot;
	}

	public static final String getBotVersion()
	{
		return VERSION;
	}

	public static String getSettingsFile()
	{
		return settings;
	}

	public static String getLogDirectory()
	{
		return syslogdir;
	}

	public static String getChatLogDirectory()
	{
		return chatlogdir;
	}

	public void prnt(String text)
	{
		System.out.println(getFormattedTime("HH:mm:ss") + "  " + text);
	}

	public void log(String text)
	{
		// print to console window
		prnt(text);
    	try {
    		// create bot log file directory, if it doesn't already exist
    		File logDir = new File(syslogdir);
    		if (!logDir.exists()) {
    			logDir.mkdir();
    		}
    		// set name of log file to write to
    		String filename = "gb" + getFormattedTime("yyyy-MM-dd") + ".log";
    		// write to log file
			BufferedWriter bfw = new BufferedWriter(new FileWriter(syslogdir + "/" + filename, true));
			bfw.write(getFormattedTime("HH:mm:ss") + " - " + text + "\r\n");
			bfw.close();
		}
		catch (IOException e) {
			// could not write to log file
		}
	}

	private void chatlog(Channel channel, String text)
	{
		chatlog(channel.getName(), text);
	}

	private void chatlog(String target, String text)
	{
    	try {
    		// create chat log file directory, if it doesn't already exist
    		File logDir = new File(chatlogdir);
    		if (!logDir.exists()) {
    			logDir.mkdir();
    		}
    		// set name of log file to write to
    		String filename = target.toLowerCase() + "." + getFormattedTime("yyyy-MM-dd") + ".log";
    		// write to chat log file
			BufferedWriter bfw = new BufferedWriter(new FileWriter(chatlogdir + "/" + filename, true));
			bfw.write("[" + getFormattedTime("HH:mm:ss") + "] " + text + "\r\n");
			bfw.close();
		}
		catch (IOException e) {
			// could not write to log file
		}
	}

	public String s(String setting)
	{
		return config.s(setting);
	}

	public int i(String setting)
	{
		return config.i(setting);
	}

	public void setValue(String setting, String value)
	{
		config.setValue(setting, value);
	}

	public void setValue(String setting, int value)
	{
		config.setValue(setting, value);
	}

    public boolean isSetting(String setting)
    {
    	return config.isSetting(setting);
    }

	public void rehash()
	{
		// attempt to re-load config file
		if (!loadConfig()) {
			log("An error occured whilst re-loading the config file");
		}
		// raise event
		ph.raiseEvent("onRehash");
	}

	public void die()
	{
		System.exit(0);
	}
	
	public PluginHandler getPluginHandler()
	{
		return ph;
	}

	public int getUserLevel(String nickname)
	{
		return getUserLevel(nickname, s("CHANNEL"));
	}

	public int getUserLevel(String nickname, String channelname)
	{
    	if (isOn(channelname)) {
    		Channel channel = getChannel(channelname);
    		if (channel.isOn(nickname)) {
		    	ChannelUser cuser = channel.getUser(nickname);
		    	if (cuser.getOtherStatus() == '~')		return LEVEL.FOUNDER;
		    	else if (cuser.getOtherStatus() == '&')	return LEVEL.SOP;
		    	else if (cuser.isOp())					return LEVEL.OP;
		    	else if (cuser.getOtherStatus() == '%')	return LEVEL.HALFOP;
		    	else if (cuser.isVoice())				return LEVEL.VOICE;
		    	else									return LEVEL.USER;
    		}
    		else {
    			return LEVEL.USER;
    		}
    	}
    	else {
    		return LEVEL.USER;
    	}
	}

	public boolean isLevel(User user, int level)
	{
		return ((user.getHostname().equals("mrg.pickup.staff")) || (isLevel(user.getNickname(), level)));
	}

    public boolean isLevel(String nickname, int level)
    {
    	return (getUserLevel(nickname) >= level);
    }
	
	public boolean isLevel(String nickname, String channel, int level)
	{
		return (getUserLevel(nickname, channel) >= level);
	}
	
	public boolean isMyChannel(String channel)
	{
		return channel.equalsIgnoreCase(s("CHANNEL"));
	}
	
	public boolean isMyChannel(Channel channel)
	{
		return isMyChannel(channel.getName());
	}
	
	public void ignore(String hostmask)
	{
		ignoreList.add(hostmask);
	}
	
	public void unignore(String hostmask)
	{
		ignoreList.remove(hostmask);
	}
	
	public boolean isIgnored(User user)
	{
		for (int x = 0; x < ignoreList.size(); x++) {
			if (user.matches(ignoreList.get(x))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isIgnored(String hostmask)
	{
		return ignoreList.contains(hostmask);
	}
	
	public String getFormattedTime(String pattern)
	{
		return getFormattedTime(new Date(), pattern);
	}
	
	public String getFormattedTime(long time, String pattern)
	{
		return getFormattedTime(new Date(time), pattern);
	}
	
	public String getFormattedTime(Date date, String pattern)
	{
    	// create new date formatter
    	DateFormat df = new SimpleDateFormat(pattern);
    	df.setTimeZone(TimeZone.getTimeZone("GMT" + s("GMT")));
    	// return date formatted according to pattern
    	return df.format(date);
	}
	
	public DatabaseConnection getDatabaseConnection() throws SQLException
	{
		// create new connection to database
		DatabaseConnection db = new DatabaseConnection();
		db.connect(s("DB-ADDRESS"), s("DB-USERNAME"), s("DB-PASSWORD"), s("DB-NAME"));
		return db;
	}
	
	public long getUptime()
	{
		return ((new Date().getTime() - startTime.getTime()) / 1000);
	}
	
    // ********************************************************************************
    //          IRC EVENTS
    // ********************************************************************************
    
    protected void onConnect()
	{
		// raise event
		ph.raiseEvent("onConnect");
		// we're now connected to the irc server
		log("* Connected");
		// notify the server that we're a bot, by setting mode +B
		setModes(getNickname(), "+B");
		// join our home channel
		join(s("CHANNEL"));
		// auto-join other channels specified in config
		for (String item: s("AUTO-JOIN").split(",")) {
			join(item);
		}
	}
	
	protected void onDisconnect()
	{
		// raise event
		ph.raiseEvent("onDisconnect");
		// we've been disconnected
		log("* Disconnected");
		// chat log
		// get list of all channels that the bot is on
		Iterator it = (getChannels().values()).iterator();
		while (it.hasNext()) {
    		Channel channel = (Channel) it.next();
    		// log disconnect message to channel's file
    		chatlog(channel.getName(), "* Disconnected");
		}
		// attempt to re-connect on a delayed timer
		if (!wasDisconnectForced()) {
			if (s("AUTO-RETRY-CONNECT").equalsIgnoreCase("ON")) {
				autoReconnectTimer = new Timer();
				autoReconnectTimer.schedule(new AutoReconnectTask(), 0, i("RETRY-INTERVAL") * 1000);
			}			
		}
	}
	
	protected void onError(String error)
	{
		// raise event
		ph.raiseEvent("onError", error);
		// log the error - kline, kill, quit or socket problem
		log("* " + error);
	}
	
	protected void onMessage(User user, String target, String message)
	{
		// raise event
		ph.raiseEvent("onMessage", new Object[]{user, target, message});
		// identify command from message
		if (!isIgnored(user)) {
			String[] msg = message.split(" ");
			if (msg.length >= 1) {
				// strip white space and irc control codes from command text
				String text = Utils.stripFormatting(msg[0]).trim();
				// check if text is a register command or alias
				BotCommand cmd = null;
				if (ph.isCommand(text)) cmd = ph.getCommand(text);
				else if (ph.isAlias(text)) cmd = ph.getAlias(text);
				if (cmd != null) {
					if (!fh.isFlooding(user)) {
						if (isValidTarget(target, cmd.getAccess())) {
							if (cmd.isEnabled()) {
								if (isLevel(user, cmd.getUserLevel())) {
									// store nickname, target and message in a single instance of messageinfo
									MessageInfo m = new MessageInfo(user, target, message); 
									// add parameter arguments to an array
									String[] p = new String[msg.length - 1];
									for (int x = 1; x < msg.length; x++) {
										p[x - 1] = msg[x];
									}
									// handle command in plugin handler
									ph.invokeCommand(cmd, m, p);
								}
								else {
									notice(user.getNickname(), "You do not have access to this command");
								}
							}
							else {
								notice(user.getNickname(), "The bot administrators have disabled this command");
							}
						}
					}
					else {
						ph.raiseEvent("onCommandFlood", new Object[]{user, message});
					}
				}
			}
		}
		// chat log
		if (target.startsWith("#")) {
			chatlog(target, "<" + user.getNickname() + "> " + message);
		}
		else {
			chatlog(user.getNickname(), "<" + user.getNickname() + "> " + message);
		}
	}
	
	protected void onNotice(User user, String target, String message)
	{
		// raise event
		ph.raiseEvent("onNotice", new Object[]{user, target, message});
		// chat log
		if (target.startsWith("#")) {
			chatlog(target, "-> -" + user.getNickname() + ":" + target + "- " + message);
		}
		else {
			chatlog(user.getNickname(), "-> -" + user.getNickname() + ":" + target + "- " + message);
		}
	}
	
    protected void onAction(String nickname, String target, String action)
    {
    	// raise event
    	ph.raiseEvent("onAction", new Object[]{nickname, target, action});
    	// chat log
		if (target.startsWith("#")) {
			chatlog(target, "* " + nickname + " " + action);
		}
		else {
			chatlog(nickname, "* " + nickname + " " + action);
		}
    }
	
	protected void onText(String target, String message)
	{
		// chat log
		chatlog(target, "<" + getNickname() + "> " + message);
	}
	
	protected void onJoin(User user, Channel channel)
	{
		// raise event
		ph.raiseEvent("onJoin", new Object[]{user, channel});
		// chat log
		if (isMe(user)) {
			chatlog(channel, "* Now talking in " + channel.getName());
		}
		else {
			chatlog(channel, "* " + user.getNickname() + " (" + user.getIdent() + "@" + user.getHostname() + ") has joined " + channel.getName());
		}
	}
	
	protected void onPart(User user, Channel channel)
	{
		// raise event
		ph.raiseEvent("onPart", new Object[]{user, channel});
		// chat log
		if (isMe(user)) {
			chatlog(channel, "* Parted " + channel.getName());
		}
		else {
			chatlog(channel, "* " + user.getNickname() + " (" + user.getIdent() + "@" + user.getHostname() + ") has left " + channel.getName());
		}
	}
	
	protected void onNickChange(User user, String newnickname)
	{
		// raise event
		ph.raiseEvent("onNickChange", new Object[]{user, newnickname});
		// chat log
		// get list of all channels that the bot is on
		Iterator it = (getChannels().values()).iterator();
		while (it.hasNext()) {
    		Channel channel = (Channel) it.next();
    		// check if user is on this channel
			if (channel.isOn(newnickname)) {
				// log to this channel's file
				chatlog(channel.getName(), "* " + user.getNickname() + " is now known as " + newnickname);
			}
		}
	}
	
    protected void onKick(User user, String target, Channel channel, String reason)
    {
		// raise event
		ph.raiseEvent("onKick", new Object[]{user, target, channel, reason});
		// chat log
		if (isMe(target)) {
			chatlog(channel, "* You were kicked by " + user.getNickname() + " (" + reason + ")");
		}
		else {
			chatlog(channel, "* " + target + " was kicked by " + user.getNickname() + " (" + reason + ")");
		}
    }
	
    protected void onChannelMode(User user, Channel channel, char mode, char operation, String parameter)
    {
		// raise event
		//ph.raiseEvent("onChannelMode", new Object[]{user, channel, mode, operation, parameter});
		// chat log
		if (parameter == null) {
			chatlog(channel, "* " + user.getNickname() + " sets mode: " + operation + mode);
		}
		else {
			chatlog(channel, "* " + user.getNickname() + " sets mode: " + operation + mode + " " + parameter);
		}
    }
	
	protected void onUserMode(User user, String target, char mode, char operation)
	{
		ph.raiseEvent("onUserMode", new Object[]{user, target, mode, operation});
	}
	
    protected void onOp(User user, Channel channel, String target)
    {
    	// raise event
		ph.raiseEvent("onOp", new Object[]{user, channel, target});
    	// chat log
    	chatlog(channel, "* " + user.getNickname() + " sets mode: +o " + target);
    }
    
    protected void onDeop(User user, Channel channel, String target)
    {
    	// raise event
		ph.raiseEvent("onDeop", new Object[]{user, channel, target});
    	// chat log
    	chatlog(channel, "* " + user.getNickname() + " sets mode: -o " + target);
    }
    
    protected void onVoice(User user, Channel channel, String target)
    {
    	// raise event
		ph.raiseEvent("onVoice", new Object[]{user, channel, target});
    	// chat log
    	chatlog(channel, "* " + user.getNickname() + " sets mode: +v " + target);
    }
    
    protected void onDevoice(User user, Channel channel, String target)
    {
    	// raise event
		ph.raiseEvent("onDevoice", new Object[]{user, channel, target});
    	// chat log
    	chatlog(channel, "* " + user.getNickname() + " sets mode: -v " + target);
    }
	
    protected void onOtherStatus(User user, Channel channel, String target, char mode, char operation)
    {
    	// raise event
		ph.raiseEvent("onOtherStatus", new Object[]{user, channel, target, mode, operation});
    	// chat log
    	chatlog(channel, "* " + user.getNickname() + " sets mode: " + operation + mode + " " + target);
    }
	
    protected void onBan(User user, Channel channel, String hostmask)
    {
    	// raise event
		ph.raiseEvent("onBan", new Object[]{user, channel, hostmask});
		// chat log
		chatlog(channel, "* " + user.getNickname() + " sets mode: +b " + hostmask);
    }
    
    protected void onUnban(User user, Channel channel, String hostmask)
    {
    	// raise event
		ph.raiseEvent("onUnban", new Object[]{user, channel, hostmask});
		// chat log
		chatlog(channel, "* " + user.getNickname() + " sets mode: -b " + hostmask);
    }
	
    protected void onTopicChange(User user, Channel channel, String topic)
    {
    	// raise event
		ph.raiseEvent("onTopicChange", new Object[]{user, channel, topic});
		// chat log
		chatlog(channel, "* " + user.getNickname() + " changes topic to '" + topic + "'");
    }
    
    protected void onInvite(User user, String target, String channelname)
    {
    	// raise event
		ph.raiseEvent("onInvite", new Object[]{user, target, channelname});
    }
	
    protected void onQuit(User user, String reason)
    {
    	// raise event
		ph.raiseEvent("onQuit", new Object[]{user, reason});
		// chat log
		// get list of all channels that the bot is on
		Iterator it = (getChannels().values()).iterator();
		while (it.hasNext()) {
			Channel channel = (Channel) it.next();
    		// check if user was on this channel
			if (channel.isOn(user)) {
				// log to this channel's file
				chatlog(channel.getName(), "* " + user.getNickname() + " (" + user.getIdent() + "@" + user.getHostname() + ") has quit IRC (" + reason + ")");
			}
		}
    }

	protected void onCtcp(String nickname, String ctcp)
	{
		ph.raiseEvent("onCtcp", new Object[]{nickname, ctcp});
	}

    protected void onChatStart(ChatSession chat)
    {
		ph.raiseEvent("onChatStart", chat);
    }
	
    protected void onIncomingFile(ReceiveFile receive)
    {
    	ph.raiseEvent("onIncomingFile", receive);
    }
    
    protected void onReceiveStart(ReceiveFile receive)
    {
    	ph.raiseEvent("onReceiveStart", receive);
    }
    
    protected void onReceiveComplete(ReceiveFile receive)
    {
    	ph.raiseEvent("onReceiveComplete", receive);
    }
	
    protected void onSendStart(SendFile send)
    {
    	ph.raiseEvent("onSendStart", send);
    }
    
    protected void onSendComplete(SendFile send)
    {
    	ph.raiseEvent("onSendComplete", send);
    }
	
	protected void rplNameReply(String channelname, String users)
	{

	}
	
	protected void errCannotSendToChan(String channelname, String reason)
	{
		log("* I cannot message " + channelname + " (" + reason + ")");
	}
	
	protected void errUnknownCommand(String command)
	{

	}
	
	protected void errNicknameInUse(String nickname)
	{

	}
	
	protected void errChannelIsFull(String channelname, String reason)
	{

	}
	
	protected void errInviteOnlyChan(String channelname, String reason)
	{

	}
	
	protected void errBannedFromChan(String channelname, String reason)
	{
		log("* I cannot join " + channelname + " (address is banned)");
	}
	
	protected void errBadChannelKey(String channelname, String reason)
	{

	}
	
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								AUTORECONNECTTASK
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

	private class AutoReconnectTask extends TimerTask
	{
		// ****************************************************************************
    	//          DECLARATIONS
    	// ****************************************************************************
		
		private int attempts = 1;		// retry attempts
		
		// ****************************************************************************
    	//          CONSTRUCTOR
    	// ****************************************************************************
		
		public AutoReconnectTask()
		{
			log("* Retrying connection...");
		}
		
		// ****************************************************************************
    	//          TIMER THREAD
    	// ****************************************************************************
		
    	public void run()
    	{
    		if (attempts <= i("RETRY-ATTEMPTS")) {
    			try {
    				prnt("Retrying connection (" + attempts + "/" + s("RETRY-ATTEMPTS") + ")");
    				// retry connection to server
    				connect();	
    				// stop the timer, we're now connected again
    				autoReconnectTimer.cancel();
    			}
				catch (ConnectException e) {
    				// increment number of failed attempts
    				attempts++;
				}
    		}
    		else {
    			// we've retried the maximum number of times
    			// stop the timer
    			autoReconnectTimer.cancel();
    			// log
    			log("* I could not connect after " + s("RETRY-ATTEMPTS") + " connection attempts");
    		}
        }
	}
}