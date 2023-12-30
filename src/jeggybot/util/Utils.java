// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								UTILS
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot.util;

import java.io.*;

import java.util.*;

import java.text.DecimalFormat;

import java.security.*; 

public class Utils
{
	private Utils()
	{
		
	}
	
	// ********************************************************************************
    //          STATIC METHODS
    // ********************************************************************************

    public static String arrayToString(String[] array)
    {
		return arrayToString(array, 0, array.length);
    }

	public static String arrayToString(String[] array, int start)
	{
		return arrayToString(array, start, array.length);
	}

	public static String arrayToString(String[] array, int start, int end)
	{
    	// build a string buffer from each array element
    	StringBuffer sb = new StringBuffer();
    	if (array.length > 0) {
    		// append the first element without a preceeding space
        	sb.append(array[start]);
        	// now append the rest of the elements, with spaces inbetween
        	for (int x = start + 1 ; x < end ; x++) {
        		if (!array[x].equals("")) {
        			sb.append(" " + array[x]);
        		}
        	}
    	}
    	// return string
    	return sb.toString();
	}

    public static int rand(int max)
    {
		return rand(0, max);
    }

    public static int rand(int min, int max)
    {
    	// generate random number
    	Random r = new Random();
		return r.nextInt(max) + min;
    }

    public static String readr(String filename)
    {
    	// open text file for reading, and add all lines to an arraylist
    	ArrayList lines = new ArrayList();
		try {
			// open file reader stream
			BufferedReader bfreader = new BufferedReader (new FileReader (filename));
			// loop through contents of file
			String line;
        	while ((line = bfreader.readLine ()) != null) {
				// add line to array list
				lines.add(line);
        	}
        	// close reader stream
        	bfreader.close();
        	// now return a random item from the array list
        	if (lines.size() > 0) {
        		return lines.get(rand(lines.size())).toString();
        	}
        	else {
        		return null;
        	}
		}
		catch (IOException e) {
			// error reading from text file
			return null;
		}
    }

    public static boolean writeIni(String filename, String item, String value)
    {
    	try {
    		// create new properties, and if ini already exists, read contents to memory
			Properties p = new Properties();
			File file = new File(filename);
			if (file.exists()) {
				FileInputStream fis = new FileInputStream(filename);
				p.load(fis);
				fis.close();	
			}
			if (!value.equals("")) {
				// add item value to properties
				p.put(item, value);
			}
			else {
				// remove item from properties list
				p.remove(item);
			}
			// write properties back to ini file
			FileOutputStream fos = new FileOutputStream(filename);
			p.store(fos, "jEggyBot - Matthew Goslett");
			fos.close();
			return true;
    	}
    	catch (IOException e) {
    		// error occured whilst reading/writing to ini file
    		return false;
    	}
    }

    public static String readIni(String filename, String item)
    {
    	try {
    		// create new properties, and load contents of ini file in memory
			Properties p = new Properties();
			FileInputStream fis = new FileInputStream(filename);
			p.load(fis);
			fis.close();
			// return item's value
			return p.getProperty(item);

    	}
    	catch (IOException e) {
    		// error occured whilst reading from ini file
    		return null;
    	}
    }

    public static String bytesToString(long bytes)
    {
    	DecimalFormat df = new DecimalFormat("0.00");
		if (bytes >= 1073741824)	return df.format((double) bytes / 1073741824) + " gb";
		else if (bytes >= 1048576)	return df.format((double) bytes / 1048576) + " mb";
		else if (bytes >= 1024)		return df.format((double) bytes / 1024) + " kb";
		else						return df.format((double) bytes) + " b";
    }
    
    public static String stripHTML(String text)
    {
        text = text.replaceAll("<.*?>", "");
        text = text.replaceAll("\\&nbsp\\;", " ");
        text = text.replaceAll("\\&.*\\;", "");
        return text;
    }
	
    public static String addSlashes(String text)
    {
		StringBuffer sb = new StringBuffer(text);
		for (int x = 0; x < sb.length(); x++) {
			char c = sb.charAt(x);
			if ((c == '\'') || (c == '"') || (c == '\\')) {
				 sb.insert(x++, '\\');
			}
		}
		return sb.toString();
    }
    
    public static String md5(String text)
    {
    	try {
	    	// convert text to bytes
	    	byte[] bytes = text.getBytes();
	    	// create new message digest
	    	MessageDigest md = MessageDigest.getInstance("MD5");
	    	// add bytes to digest
	    	md.update(bytes);
	    	// convert bytes to a md5 hash
	    	byte[] md5 = md.digest();
	    	// convert hash bytes to a string
			StringBuffer sb = new StringBuffer();
			for (int x = 0; x < md5.length; x++) {
				sb.append(Integer.toHexString(0x0100 + (md5[x] & 0x00FF)).substring(1));
			}
			return sb.toString();
    	}
    	catch (NoSuchAlgorithmException e) {
    		return null;
    	}
    }
}