// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								WEATHER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.Weather;

import jeggybot.*;

import jeggybot.util.CachedResult;
import jeggybot.util.Utils;

import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;

import org.xml.sax.SAXException;

public class plgWeather extends Plugin
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private static HashMap<String, String> cities;		// list of cities for weather
	private HashMap<Object, CachedResult> results;		// cached results
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgWeather()
	{
		// initialisation
		results = new HashMap<Object, CachedResult>();
		// set plugin information
		name = "Weather";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!weather", "cmdWeather", ACCESS.CHANNEL, LEVEL.USER, this);
	}

    static
    {
		// initialisation
		cities = new HashMap<String, String>();
		// load cities into weather city hashmap
		cities.put("alexander bay", "SFXX0001");
		cities.put("bellville", "SFXX0005");
		cities.put("benoni", "SFXX0006");
		cities.put("bloemfontein", "SFXX0008");
		cities.put("cape point", "SFXX0068");
		cities.put("cape town", "SFXX0010");
		cities.put("durban", "SFXX0011");
		cities.put("east london", "SFXX0012");
		cities.put("george", "SFXX0066");
		cities.put("hermanus", "SFXX0019");
		cities.put("jeffrey's bay", "SFXX0022");
		cities.put("johannesburg", "SFXX0023");
		cities.put("kempton Park", "SFXX0027");
		cities.put("kimberley", "SFXX0028");
		cities.put("malmesbury", "SFXX0034");
		cities.put("mphakane", "SFXX0035");
		cities.put("paarl", "SFXX0036");
		cities.put("parow", "SFXX0037");
		cities.put("pietermaritzburg", "SFXX0070");
		cities.put("port elizabeth", "SFXX0041");
		cities.put("pretoria", "SFXX0044");
		cities.put("strand", "SFXX0051");
		cities.put("upington", "SFXX0056");
		cities.put("welkom", "SFXX0059");
		cities.put("witbank", "SFXX0060");
    }

	// ********************************************************************************
	//			PRIVATE METHODS
	// ********************************************************************************
    
    private WeatherReader getWeatherFeed(String city)
    {
    	// define result key
    	Object key = city.toLowerCase();
    	// determine if result is in cache list
    	if (results.containsKey(key)) {
    		// check if result is older than 30 minutes
    		CachedResult cr = results.get(key);
    		if (cr.getTimeDifference() < 1800) {
    			// return cached result
    			return (WeatherReader) cr.getResult();
    		}
    		else {
    			// remove result from cache list
    			results.remove(key);
    		}
    	}
		// fetch new result
		// get weather id of city
		String id = cities.get(key);
		try {
			// get xml weather feed
			WeatherReader wr = new WeatherReader(id);
			// store weather feed for this city as a cached result
			results.put(key, new CachedResult(key, wr));
			return wr;
		}
		catch (SAXException e) {
			return null;
		}
		catch (IOException e) {
			return null;
		}
    }
    
	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
	
    public void cmdWeather(final MessageInfo m, final String[] p)
    {
		new Thread() {
			public void run() {
		    	if (p.length >= 1) {
		    		String city = Utils.arrayToString(p);
					if (cities.containsKey(city.toLowerCase())) {
						// get weather feed of city
						WeatherReader wr = getWeatherFeed(city);
						if (wr != null) {
							// display response to user
							bot.msg(m.getTarget(), "14" + wr.getCity() + " Weather Forecast");
							bot.msg(m.getTarget(), "14Temperature: " + wr.getTemperature() + "°    Condition: " + wr.getCondition() + "    Humidity: " + wr.getHumidity() + "%    Visibility: " + (wr.getVisibility() / 100) + "km    Wind Speed: " + wr.getWindSpeed() + "kph");
						}
						else {
							bot.msg(m.getTarget(), "14I could not retrieve the weather for " + city + "");
						}
					}
					else {
						bot.notice(m.getNickname(), "" + city + " is an unknown city to me");
					}
		    	}
		    	else {
					bot.notice(m.getNickname(), "Syntax: !weather (city)");
		    	}
			}
		}.start();
    }
}