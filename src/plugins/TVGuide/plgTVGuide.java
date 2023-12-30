// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								TVGUIDE
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.TVGuide;

import jeggybot.*;

import jeggybot.util.CachedResult;
import jeggybot.util.Utils;

import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;

import java.net.MalformedURLException;

public class plgTVGuide extends Plugin
{	
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************

	private HashMap<Object, CachedResult> results;		// cached results

	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgTVGuide()
	{
		// initialisation
		results = new HashMap<Object, CachedResult>();
		// set plugin information
		name = "TV Guide";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!epinfo", "cmdEPInfo", ACCESS.CHANNEL, LEVEL.USER, this);
	}
    
	// ********************************************************************************
    //          PRIVATE METHODS
    // ********************************************************************************
    
    private ShowInfo getShowInfo(String show)
    {
    	// define result key
    	String key = (show.replace(' ', '+')).toLowerCase();
    	// determine if result is in cache list
    	if (results.containsKey(key)) {
    		// check if result is older than 40 minutes
    		CachedResult cr = results.get(key);
    		if (cr.getTimeDifference() < 2400) {
    			// return cached result
    			return (ShowInfo) cr.getResult();
    		}
    		else {
    			// remove result from cache list
    			results.remove(key);
    		}
    	}
		// fetch new result
		try {
			// attempt to retrieve information about show
			// from episodeworld.com
			ShowInfo si = new ShowInfo(show);
			// cache result
			results.put(key, new CachedResult(key, si));
			return si;
		}
		catch (MalformedURLException e) {
			return null;
		}
		catch (IOException e) {
			return null;
		}
    }
    
	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
    
    public void cmdEPInfo(final MessageInfo m, final String p[])
    {
		new Thread() {
			public void run() {
		    	if (p.length >= 1) {
		    		String show = Utils.arrayToString(p);
					ShowInfo si = getShowInfo(show);
					if (si != null) {
						if (si.isValid()) {
							// get previous and next episode arrays
							String[] previous = si.getPreviousEpisode();
							String[] next = si.getNextEpisode();
							// display results
		    				if (next == null) {
		    					// next episode information not available
		    					bot.msg(m.getTarget(), "10(14Show:7 " + si.getName() + "10) 10(14Next: 7no information available10) 10(14Previous:7 " + previous[0] + " - " + previous[1] + " aired " + previous[2] + "10)");
		    				}
		    				else if (next[2].equals("0000-00-00")) {
		    					// next air-date is unknown
		    					bot.msg(m.getTarget(), "10(14Show:7 " + si.getName() + "10) 10(14Next:7 " + next[0] + " - " + next[1] + " air date unknown10) 10(14Previous:7 " + previous[0] + " - " + previous[1] + " aired " + previous[2] + "10)");
		    				}
		    				else {
		    					// display episode information
		    					bot.msg(m.getTarget(), "10(14Show:7 " + si.getName() + "10) 10(14Next:7 " + next[0] + " - " + next[1] + " airs " + next[2] + "10) 10(14Previous:7 " + previous[0] + " - " + previous[1] + " aired " + previous[2] + "10)");
		    				}
		    				bot.msg(m.getTarget(), "14" + si.getShowURL() + "");
						}
						else {
							bot.notice(m.getNickname(), "You have entered an invalid show name");
						}
					}
					else {
						bot.notice(m.getNickname(), "I was unable to retrieve tv guide information for this show");
					}
		    	}
		    	else {
		    		bot.notice(m.getNickname(), "Syntax: !epinfo (tv show)");
		    	}
			}
		}.start();
    }
}