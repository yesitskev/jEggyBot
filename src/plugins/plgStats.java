// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								STATS
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.DatabaseConnection;
import jeggybot.util.Utils;

import jirc.Channel;
import jirc.User;

import java.util.Date;

import java.sql.*;

public class plgStats extends Plugin
{
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgStats()
	{
		// set plugin information
		name = "Stats";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!st", "cmdST", ACCESS.GLOBAL, LEVEL.USER, this);
	}
	
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************

	public void cmdST(final MessageInfo m, final String[] p)
	{
		if (!m.getNickname().equalsIgnoreCase("mrg")) return;
		
	}
}