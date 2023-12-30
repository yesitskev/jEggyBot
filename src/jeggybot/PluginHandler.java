// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PLUGINHANDLER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import java.util.*;

import java.lang.reflect.*;

public class PluginHandler
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private jEggyBot bot;											// bot client
	private HashMap<String, Plugin> plugins;						// loaded plugins
	private HashMap<Plugin, PluginEventListener> eventListeners;	// plugin event listeners
	private HashMap<String, BotCommand> commands;					// registered bot commands
	private HashMap<String, BotCommand> aliases;					// aliases of commands
	
    // ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public PluginHandler()
	{
		// initialisation
		bot = jEggyBot.getBotClient();
		plugins = new HashMap<String, Plugin>();
		eventListeners = new HashMap<Plugin, PluginEventListener>();
		commands = new HashMap<String, BotCommand>();
		aliases = new HashMap<String, BotCommand>();
	}
	
    // ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************
	
	public boolean loadPlugin(String className)
	{
		try {
			// create a new plugin loader and attempt to load class
			PluginLoader pl = new PluginLoader();
			Class cls = pl.loadClass("plugins." + className);
			// prepare arguments to pass to constructor of plugin
	    	Constructor cstr = cls.getConstructor(new Class[]{});
	    	// create new instance of this plugin
	    	Plugin plugin = (Plugin) cstr.newInstance(new Object[]{});
			// add plugin to plugin list
			plugins.put(className.toLowerCase(), plugin);
			return true;	
		}
		catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	public void unloadPlugin(String className)
	{
		// determine instance of plugin with specified class name
		Plugin plugin = getPlugin(className);
		// drop all aliases that are linked to commands in this plugin
		HashMap a = (HashMap) aliases.clone();
		Iterator it1 = (a.keySet()).iterator();
		while (it1.hasNext()) {
			String alias = (String) it1.next();
			BotCommand cmd = getAlias(alias);
			if (cmd.getPlugin().equals(plugin)) {
				aliases.remove(alias.toLowerCase());
			}
		}
		// drop all commands that are registered with this plugin
		HashMap c = (HashMap) commands.clone();
		Iterator it2 = (c.values()).iterator();
		while (it2.hasNext()) {
			BotCommand cmd = (BotCommand) it2.next();
    		if (cmd.getPlugin().equals(plugin)) {
    			commands.remove(cmd.getTrigger().toLowerCase());
    		}
		}
		// remove plugin event listener
		eventListeners.remove(plugin);
		// remove plugin name from plugin list
		plugins.remove(className.toLowerCase());
	}

	public Plugin getPlugin(String className)
	{
		return plugins.get(className.toLowerCase());
	}

	public boolean isPlugin(String className)
	{
		return plugins.containsKey(className.toLowerCase());
	}

	public HashMap getPluginList()
	{
		return plugins;
	}
	
	public int getPluginCount()
	{
		return plugins.size();
	}
	
	public synchronized void addEventListener(Plugin plugin, PluginEventListener listener)
	{
		if (!eventListeners.containsKey(plugin)) {
			eventListeners.put(plugin, listener);
		}
	}

	public void register(String command, String method, int access, int level, Plugin plugin)
	{
		register(command, method, access, level, plugin, true);
	}

	public void register(String trigger, String method, int access, int level, Plugin plugin, boolean visible)
	{
		if (!isCommand(trigger)) {
			// create a new bot command
			BotCommand cmd = new BotCommand(trigger, method, access, level, plugin, visible);
			// add command to command list
			commands.put(trigger.toLowerCase(), cmd);
		}
		else {
			bot.log(plugin.getName() + " tried to register duplicate command '" + trigger + "'");
		}
	}

	public BotCommand getCommand(String command)
	{
		return commands.get(command.toLowerCase());
	}

	public boolean isCommand(String command)
	{
		return commands.containsKey(command.toLowerCase());
	}

	public HashMap getCommandList()
	{
		return commands;
	}

	public int getCommandCount()
	{
		return commands.size();
	}

	public void registerAlias(String alias, String command)
	{
		if (!isAlias(alias)) {
			if (isCommand(command)) {
				// link alias to command
				aliases.put(alias.toLowerCase(), getCommand(command));
			}
			else {
				bot.log("The alias " + alias + " can only be linked to a registered command");
			}
		}
		else {
			bot.log("The alias " + alias + " is already linked to the command " + getAlias(alias).getTrigger());
		}
	}

	public BotCommand getAlias(String alias)
	{
		return aliases.get(alias.toLowerCase());
	}

	public boolean isAlias(String alias)
	{
		return aliases.containsKey(alias.toLowerCase());
	}
	
	public int getAliasCount()
	{
		return aliases.size();
	}
	
	public void raiseEvent(String event)
	{
		raiseEvent(event, new Object[]{});
	}
	
	public void raiseEvent(String event, Object arg)
	{
		raiseEvent(event, new Object[]{arg});
	}

	public void raiseEvent(String event, Object[] arglist)
	{
		// loop through each argument, and determine what type of class it is
		ArrayList a = new ArrayList();
		for (Object object: arglist) {
			a.add(object.getClass());
		}
		// build array of class types
		Class[] parmtypes = (Class[]) a.toArray(new Class[a.size()]);
		// fire event on each event listener
		Iterator it = (eventListeners.values()).iterator();
		while (it.hasNext()) {
            PluginEventListener listener = (PluginEventListener) it.next();
			try {
				// invoke event by method name
				Class cls = PluginEventListener.class;
				Method method = cls.getMethod(event, parmtypes);
				method.invoke(listener, arglist);
			}
			catch (NoSuchMethodException e) {
			}
			catch (IllegalAccessException e) {
			}
			catch (InvocationTargetException e) {
				throw new PluginException(e.getCause());
			}
		}
	}

	public void invokeCommand(BotCommand cmd, MessageInfo m, String[] p)
	{
		// execute command
		try {
			// determine class object of plugin instance
			Class cls = cmd.getPlugin().getClass();
			// identify method within class
			Method method = cls.getMethod(cmd.getMethod(), new Class[]{MessageInfo.class, String[].class});
			// invoke method by name
			method.invoke(cmd.getPlugin(), new Object[]{m, p});
		}
		catch (NoSuchMethodException e) {
			throw new CommandException("The plugin " + (cmd.getPlugin()).getName() + " is missing the method " + cmd.getMethod() + "() for the command " + cmd.getTrigger());
		}
		catch (IllegalAccessException e) {
			throw new CommandException("The method " + cmd.getMethod() + " for command " + cmd.getTrigger() + " must be made public in the plugin " + (cmd.getPlugin()).getName());
		}
		catch (InvocationTargetException e) {
			throw new PluginException(e.getCause());
		}
	}
}