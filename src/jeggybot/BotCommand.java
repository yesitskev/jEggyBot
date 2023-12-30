// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								BOTCOMMAND
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

public class BotCommand
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String trigger;				// trigger
	private String method;				// method to invoke upon trigger
	private int access;					// allowed method of access
	private int level;					// required user access level
	private Plugin plugin;				// host plugin
	private boolean enabled = true;		// status of command
	private boolean visible = true;		// command visibility
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	protected BotCommand()
	{
		
	}
	
	protected BotCommand(String trigger, String method, int access, int level, Plugin plugin, boolean visible)
	{
		// store command information
		this.trigger = trigger;
		this.method = method;
		this.access = access;
		this.level = level;
		this.plugin = plugin;
		this.visible = visible;
	}
	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************

	public void setTrigger(String trigger)
	{
		this.trigger = trigger;
	}

	public String getTrigger()
	{
		return trigger;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getMethod()
	{
		return method;
	}

	public void setAccess(int access)
	{
		this.access = access;
	}

	public int getAccess()
	{
		return access;
	}

	public void setUserLevel(int level)
	{
		this.level = level;
	}

	public int getUserLevel()
	{
		return level;
	}

	public void setPlugin(Plugin plugin)
	{
		this.plugin = plugin;
	}

	public Plugin getPlugin()
	{
		return plugin;
	}

	public void enable()
	{
		this.enabled = true;
	}

	public void disable()
	{
		this.enabled = false;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void show()
	{
		this.visible = true;
	}
	
	public void hide()
	{
		this.visible = false;
	}
	
	public boolean isVisible()
	{
		return visible;
	}
}