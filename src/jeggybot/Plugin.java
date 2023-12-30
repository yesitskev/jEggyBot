// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PLUGIN
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

public class Plugin
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	protected jEggyBot bot;					// bot client
	protected PluginHandler ph;				// plugin handler
	protected String name = "Plugin Name";	// name
	protected String author = "Author";		// author
	protected String version = "0";			// version
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	public Plugin()
	{
		// get instances of bot client and plugin handler
		bot = jEggyBot.getBotClient();
		ph = bot.getPluginHandler();
	}
	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************

	public String getName()
	{
		return name;
	}

	public String getAuthor()
	{
		return author;
	}

	public String getVersion()
	{
		return version;
	}
}