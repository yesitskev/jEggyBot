// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								CONFIG
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Config extends HashMap
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String name;		// config name
	private boolean loaded;		// status of config
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	public Config()
	{

	}
	
	public Config(String name)
	{
		// store config name
		this.name = name;
	}
	
	public Config(String name, String filename)
	{
		// store config name
		this.name = name;
		// load file
		loadFile(filename);
	}
	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************

    public String s(String setting)
    {
        return (String) get(setting.toLowerCase());
    }

    public int i(String setting)
    {
        return Integer.parseInt(s(setting));
    }

    public boolean b(String setting)
    {
        return Boolean.parseBoolean(s(setting));
    }

    public void setValue(String setting, String value)
    {
        put(setting.toLowerCase(), value);
    }

    public void setValue(String setting, int value)
    {
        setValue(setting, String.valueOf(value));
    }
    
    public String getName()
    {
    	return name;
    }
    
	// ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************
    
    public boolean isSetting(String setting)
    {
        return containsKey(setting.toLowerCase());
    }
    
    public boolean isLoaded()
    {
    	return loaded;
    }
    
	public boolean loadFile(String filename)
	{
		// clear previous config settings
		clear();
		try {
			// open file reader stream
			BufferedReader bfr = new BufferedReader(new FileReader(filename));
			// loop through contents of config file
			String line;
        	while ((line = bfr.readLine ()) != null) {
        		// remove all tabs and extra spaces from line
        		line = line.trim();
				// parse the current line
				// we won't really be dealing with any lines, except the setting/value ones
				if (line.matches(".+ \\{")) {
					// plugin settings start
				}
				else if (line.matches("#.*")) {
					// comment
				}
				else if (line.matches("\\}")) {
					// plugin settings end
				}
				else {
					// our last check is to see if it is a setting/value line
					Pattern pattern = Pattern.compile("(.+) = '(.*)'");
					Matcher matcher = pattern.matcher(line);
					if (matcher.matches()) {
						// setting/value line
						// add setting + value to config hashmap
						put(matcher.group(1).toLowerCase(), matcher.group(2));
					}
					else {
						// error on current line
					}
				}
        	}
        	// close reader stream
        	bfr.close();
        	loaded = true;
        	return true;
		}
		catch (IOException e) {
			loaded = false;
			return false;
		}
	}
}