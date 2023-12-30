// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PLUGIN MANAGEMENT
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import java.util.Iterator;

public class plgPluginManagement extends Plugin
{	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgPluginManagement()
	{
		// set plugin information
		name = "Plugin Management";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!loadplugin", "cmdLoadPlugin", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!unloadplugin", "cmdUnloadPlugin", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!reloadplugin", "cmdReloadPlugin", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!plugins", "cmdPlugins", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!enablecommand", "cmdEnableCommand", ACCESS.GLOBAL, LEVEL.SOP, this);
		ph.register("!disablecommand", "cmdDisableCommand", ACCESS.GLOBAL, LEVEL.SOP, this);
	}
	
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************

    public void cmdLoadPlugin(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String classname = p[0];
    		if (!ph.isPlugin(classname)) {
    			// load plugin
    			if (ph.loadPlugin(classname)) {
    				bot.notice(m.getNickname(), "" + classname + " has been loaded");
    				// log
    				bot.log(m.getNickname() + " loaded plugin " + classname);
    			}
    			else {
    				bot.notice(m.getNickname(), "I could not load plugin, " + classname + "");	
    			}
    		}
    		else {
    			bot.notice(m.getNickname(), "" + classname + " is already loaded");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !loadplugin (class name)");
    	}
    }
    
    public void cmdUnloadPlugin(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String classname = p[0];
    		if (ph.isPlugin(classname)) {
    			// unload plugin
    			ph.unloadPlugin(classname);
    			bot.notice(m.getNickname(), "" + classname + " has been un-loaded");
    			// log
    			bot.log(m.getNickname() + " un-loaded plugin " + classname);
    		}
    		else {
    			bot.notice(m.getNickname(), "" + classname + " is not a loaded plugin");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !unloadplugin (class name)");
    	}
    }
    
    public void cmdReloadPlugin(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String classname = p[0];
    		if (ph.isPlugin(classname)) {
    			// unload plugin
    			ph.unloadPlugin(classname);
    			// re-load plugin
    			if (ph.loadPlugin(classname)) {
    				bot.notice(m.getNickname(), "" + classname + " has been re-loaded");
    				// log
    				bot.log(m.getNickname() + " re-loaded plugin " + classname);
    			}
    			else {
    				bot.notice(m.getNickname(), "" + classname + " failed to re-load");	
    			}
    		}
    		else {
    			bot.notice(m.getNickname(), "" + classname + " is not a loaded plugin");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !reloadplugin (class name)");
    	}
    }
    
    public void cmdPlugins(final MessageInfo m, final String[] p)
    {
    	bot.notice(m.getNickname(), "Plugin List");
		String list = "";
		Iterator it = (ph.getPluginList().values()).iterator();
		while (it.hasNext()) {
			Plugin plugin = (Plugin) it.next();
			list += plugin.getName() + " " + plugin.getVersion() + ", ";
		}
		bot.notice(m.getNickname(), list);
    }
    
    public void cmdEnableCommand(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
    		String text = p[0];
			if (ph.isCommand(text)) {
				BotCommand cmd = ph.getCommand(text);
				if (!cmd.isEnabled()) {
					// enable this command
					cmd.setEnabled(true);
					bot.notice(m.getNickname(), "" + text + " is now enabled");
					// log
					bot.log(m.getNickname() + " enabled '" + text + "' command");
				}
				else {
					bot.notice(m.getNickname(), "" + text + " is already enabled");
				}
			}
			else {
				bot.notice(m.getNickname(), "" + text + " is not one of my commands");
			}	
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !enablecommand (command)");
    	}
    }
    
    public void cmdDisableCommand(final MessageInfo m, final String[] p)
    {
    	if (p.length == 1) {
	    	String text = p[0];
			if (ph.isCommand(text)) {
				if ((!text.equalsIgnoreCase("!ENABLECOMMAND")) && (!text.equalsIgnoreCase("!DISABLECOMMAND"))) {
					BotCommand cmd = ph.getCommand(text);
					if (cmd.isEnabled()) {
						// disable this command
						cmd.setEnabled(false);
						bot.notice(m.getNickname(), "" + text + " is now disabled");
						// log
						bot.log(m.getNickname() + " disabled '" + text + "' command");
					}
					else {
						bot.notice(m.getNickname(), "" + text + " is already disabled");
					}
				}
				else {
					bot.notice(m.getNickname(), "This command cannot be disabled");
				}
			}
			else {
				bot.notice(m.getNickname(), "" + text + " is not one of my commands");
			}	
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !disablecommand (command)");
    	}
    }
}