// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								WGET
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.Wget;

import jeggybot.*;

import jeggybot.util.Utils;

import java.util.HashMap;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;

public class plgWget extends Plugin
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private HashMap<String, FileDownload> downloads;			// active file downloads
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgWget()
	{
		// initialisation
		downloads = new HashMap<String, FileDownload>();
		// set plugin information
		name = "Wget";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!wget", "cmdWget", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
	}

	// ********************************************************************************
	//			PRIVATE METHODS
	// ********************************************************************************

	private boolean isDownloading(String filename)
	{
		return downloads.containsKey(filename.toLowerCase());
	}

	private FileDownload getDownload(String filename)
	{
		return downloads.get(filename.toLowerCase());
	}

	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
	
    public void cmdWget(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String type = p[0].toUpperCase();
    		if (type.equals("GET"))			cmdWgetGet(m, p);
    		else if (type.equals("INFO"))	cmdWgetInfo(m, p);
    		else if (type.equals("CANCEL"))	cmdWgetCancel(m, p);
			else if (type.equals("FETCH"))	cmdWgetFetch(m, p);
    		else if (type.equals("LIST"))	cmdWgetList(m, p);    		
    		else {
    			cmdWgetHelp(m, p);
    		}
    	}
    	else {
    		cmdWgetHelp(m, p);
    	}
    }
    
    private void cmdWgetGet(final MessageInfo m, final String[] p)
    {
		if (p.length == 2) {
			final String address = p[1];
			if (address.matches("(http(s)?|ftp)://(.+)/(.+\\.(.+))")) {
				// extract filename from address
				String filename = address.substring(address.lastIndexOf("/") + 1, address.length());
				filename = filename.replaceAll("%20", "_");
				// check if a file with this name already exists
				// if so, append a number starting from 2
				File file = new File(bot.s("WGET-PATH") + "/" + filename);
				int cnt = 1;
				while (file.exists()) {
					file = new File(bot.s("WGET-PATH") + "/" + filename + cnt++);
				}
				filename = file.getName();
				// create new file downloader
				FileDownload fd = new FileDownload(m.getNickname(), address, filename, bot.s("WGET-PATH"));
				// add file download to list of current downloads
				downloads.put(filename.toLowerCase(), fd);
				// log
				bot.log(m.getNickname() + " is attempting to download " + address);
				try {
					// begin file download
					bot.notice(m.getNickname(), "Attempting to download " + filename + "");
					bot.notice(m.getNickname(), "Type !wget info " + filename + " to track the progress of this file");
					fd.download();
					// download complete
					bot.notice(m.getNickname(), "" + filename + " has finished downloading");
				}
				catch (MalformedURLException e) {
					bot.notice(m.getNickname(), "You have entered an invalid URL");
				}
				catch (IOException e) {
					bot.notice(m.getNickname(), "An error occured whilst attempting to download " + address);
				}
				// remove file download from download list
				downloads.remove(filename);
			}
			else {
				bot.notice(m.getNickname(), "You may only download a file in the format of (http(s):ftp)://domain/filename.abc");
			}
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !wget get (url)");
		}
    }
    
    private void cmdWgetInfo(final MessageInfo m, final String[] p)
    {
		if (p.length == 2) {
			String filename = p[1];
			if (isDownloading(filename)) {
				// get file download information
				FileDownload fd = getDownload(filename);
				bot.notice(m.getNickname(), "" + fd.getFilename() + " is " + fd.getPercentComplete() + "% complete and is downloading at " + Utils.bytesToString(fd.getTransferRate()) + "/s");
			}
			else {
				bot.notice(m.getNickname(), "The file which you specified is not downloading");
			}
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !wget info (filename)");
		}
    }
    
    private void cmdWgetCancel(final MessageInfo m, final String[] p)
    {
    	if (p.length == 2) {
			String filename = p[1];
			if (isDownloading(filename)) {
				// cancel download
				FileDownload fd = getDownload(filename);
				fd.cancel();
				// notice user
				bot.notice(m.getNickname(), "" + fd.getFilename() + " has stopped downloading");
			}
			else {
				bot.notice(m.getNickname(), "The file which you specified is not downloading");
			}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !wget cancel (filename)");
    	}
    }
    
    private void cmdWgetFetch(final MessageInfo m, final String[] p)
    {
    	if (p.length == 2) {
    		String filename = p[1];
    		if (!isDownloading(filename)) {
    			File file = new File(bot.s("WGET-PATH") + "/" + filename);
    			if (file.exists()) {
    				// send file to user
    				bot.notice(m.getNickname(), "sending you " + filename + "");
    				bot.dccSend(m.getNickname(), file.getPath());
    				// log
    				bot.log(m.getNickname() + " is wget fetching " + filename);
    			}
    			else {
    				bot.notice(m.getNickname(), "" + filename + " does not exist");
    			}
    		}
    		else {
    			bot.notice(m.getNickname(), "" + filename + " is still busy downloading");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !wget fetch (filename)");
    	}
    }
    
    private void cmdWgetList(final MessageInfo m, final String[] p)
    {
    	if (downloads.size() > 0) {
    		// send user a list of active file downloads
    		bot.notice(m.getNickname(), "Active Downloads");
			Iterator it = (downloads.values()).iterator();
			while (it.hasNext()) {
	            FileDownload fd = (FileDownload) it.next();
	            bot.notice(m.getNickname(), fd.getFilename() + " (" + fd.getPercentComplete() + "%) @ " + Utils.bytesToString(fd.getTransferRate()) + "/s - " + fd.getDownloader());
	    	}
	    }
    	else {
    		bot.notice(m.getNickname(), "no active wget downloads");
    	}
    }
    
    private void cmdWgetHelp(final MessageInfo m, final String[] p)
    {
    	bot.notice(m.getNickname(), "download a file - !wget get (url)");
    	bot.notice(m.getNickname(), "check download progress - !wget info (filename)");
    	bot.notice(m.getNickname(), "cancel download - !wget cancel (filename)");
    	bot.notice(m.getNickname(), "fetch a download - !wget fetch (filename)");
    	bot.notice(m.getNickname(), "list active downloads - !wget list");
    }
}