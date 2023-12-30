// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								SHOWINFO
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.TVGuide;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ShowInfo
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String show;
	private boolean valid = true;
	private String name;
	private String[] previous;
	private String[] next;
	private String showURL;
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public ShowInfo(String show) throws MalformedURLException, IOException
	{
		// store show search string
		this.show = show;
		// retrieve show information
		// create new url connection
		URL url = new URL("http://episodeworld.com/botsearch/" + show);
		// read data
		BufferedReader bfr = new BufferedReader(new InputStreamReader(url.openStream()));
		String data;
		Pattern pattern;
		Matcher matcher;
		int line = 1;
		while ((data = bfr.readLine()) != null) {
			// parse line
			if (!data.equalsIgnoreCase("no results found")) {
				switch (line) {
					case 1:
						// episode name
						// eg: Prison Break<br>
						name = data.substring(0, data.length() - 4);
						break;
					case 2:
						// previous episode information
						// eg: Previous: 2x13 Name: The Killing Box Date: 2006-11-27<br>
						pattern = Pattern.compile("Previous: (.+) Name: (.+) Date: (.+)<br>");
						matcher = pattern.matcher(data);
						if (matcher.matches()) {
							// store previous episode in string array of format 
							// [0] = episode number, [1] = episode name, [2] = air date
							previous = new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
						}
						break;
					case 3:
						// next episode information
						// eg: Next: Special Name: Season Recap Date: 2007-01-22<br>
						pattern = Pattern.compile("Next: (.+) Name: (.+) Date: (.+)<br>");
						matcher = pattern.matcher(data);
						if (matcher.matches()) {
							// store previous episode in string array of format 
							// [0] = episode number, [1] = episode name, [2] = air date
							next = new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
						}
						break;
					case 4:
						// episodeworld.com show url
						// eg: http://episodeworld.com/show/Prison_Break<br>
						showURL = data.substring(0, data.length() - 4);
						break;
				}
				line++;
			}
			else {
				// invalid show name
				valid = false;
				// don't continue parsing data
				break;
			}
		}
		// close stream
		bfr.close();
	}
	
	// ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************
	
	public boolean isValid()
	{
		return valid;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String[] getPreviousEpisode()
	{
		return previous;
	}
	
	public String[] getNextEpisode()
	{
		return next;
	}
	
	public String getShowURL()
	{
		return showURL;
	}
}