// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								NICKSERV
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jirc.User;

public class plgNickServ extends Plugin
{

	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgNickServ()
	{
		// set plugin information
		name = "NickServ";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onNotice(User user, String target, String message) {
				onNoticeEvent(user, target, message);
			}
		});
	}
	
	// ********************************************************************************
    //          IRC EVENTS
    // ********************************************************************************
    
	private void onNoticeEvent(User user, String target, String message)
	{
		if ((user.getNickname()).equalsIgnoreCase(bot.s("SERVICES-NICK"))) {
			if (message.matches(".*This nickname is registered and protected.*")) {
				// identify with nickserv
				bot.msg(bot.s("SERVICES-NICK"), "IDENTIFY " + bot.s("NICKNAME-PASSWORD"));
			}
			else if (message.matches(".*Password accepted.*")) {
				// join our home channel
				bot.join(bot.s("CHANNEL"));
				// auto-join other channels specified in config
				// this list of channels is seperated by commas
				for (String item: bot.s("AUTO-JOIN").split(",")) {
					bot.join(item);
				}
			}
		}
	}
}