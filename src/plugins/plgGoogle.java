// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								GOOGLE
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.Utils;

import com.google.soap.search.*;

public class plgGoogle extends Plugin
{

	// ********************************************************************************
	//			CONSTRUCTOR
	// ********************************************************************************

	public plgGoogle()
	{
		// set plugin information
		name = "Google";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!google", "cmdGoogle", ACCESS.CHANNEL, LEVEL.USER, this);
		// register aliases
		ph.registerAlias("!g", "!google");
	}

	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
	
    public void cmdGoogle(final MessageInfo m, final String[] p)
    {
		new Thread() {
			public void run() {
				if (p.length >= 1) {
					String query = Utils.arrayToString(p);
					try {
						// create new google search
						GoogleSearch search = new GoogleSearch();
						search.setKey(bot.s("API-KEY"));
						search.setMaxResults(1);
						search.setQueryString(query);
						// get search results
						GoogleSearchResult result = search.doSearch();
						GoogleSearchResultElement results[] = result.getResultElements();
						if(results.length > 0) {
							// format result
							String title = Utils.stripHTML(results[0].getTitle());
							String snippet = Utils.stripHTML(results[0].getSnippet());
							String format =	bot.s("SEARCH-RESULT");
							format = format.replace("$query", query);
							format = format.replace("$title", title);
							format = format.replace("$url",	results[0].getURL());
							format = format.replace("$snippet",	snippet);
							// display result in channel
							bot.msg(m.getTarget(), format);
						}
						else {
							bot.msg(m.getTarget(), "14No result(s) found for *" + query + "*");
						}
					}
					catch(GoogleSearchFault	fault) {
						bot.msg(m.getTarget(), "14An error occured whilst querying google");
					}
				}
				else {
					bot.notice(m.getNickname(), "Syntax: !google (query)");
				}
			}
		}.start();
	}
}