// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PISG
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.DatabaseConnection;

import java.io.*;

import java.util.*;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.net.ftp.*;

public class plgPisg extends Plugin
{	
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************

	private Timer generatorTimer;			// stats generator timer

	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgPisg()
	{
		// set plugin information
		name = "pisg";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!addstatschan", "cmdAddStatsChan", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!delstatschan", "cmdDelStatsChan", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!liststatschans", "cmdListStatsChans", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		ph.register("!updatestatschans", "cmdUpdateStatsChans", ACCESS.GLOBAL, LEVEL.FOUNDER, this);
		// get pisg-run-time setting from config and split it into hours & minutes
		String runTime = bot.s("PISG-RUN-TIME");
		int hour = Integer.parseInt(runTime.substring(0, runTime.indexOf(":")));
		int minute = Integer.parseInt(runTime.substring(runTime.indexOf(":") + 1));
		// determine if the generate time has already passed or is yet to come
		// so that we can set the initial timer date
		TimeZone tz = TimeZone.getTimeZone("GMT" + bot.s("GMT"));
		Calendar calendar = new GregorianCalendar(tz);
		// set calendar time to the time that we wish to run this task
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		// create new calendar with the current time
		Calendar now = new GregorianCalendar(tz);
		// compare the 2 sets of times
		if (calendar.compareTo(now) <= 0) {
			// the specified time has passed
			// timer will start tomorrow at specified time, therefore shift day in calendar to tomorrow
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		// schedule timer to start once per day at requested time
		generatorTimer = new Timer();
		generatorTimer.scheduleAtFixedRate(new StatsGeneratorTask(), calendar.getTime(), 1000 * 60 * 60 * 24 * 7);
	}
	
	// ********************************************************************************
    //          PRIVATE METHODS
    // ********************************************************************************
	
	private boolean isStatsChannel(String channel)
	{
		try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// check if stats channel exists in database
			PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "statschannels WHERE channel=?");
			ps.setString(1, channel);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int rows = rs.getInt(1);
			// close database connection
			db.close();
			return (rows > 0);
		}
		catch (SQLException e) {
			return false;
		}
	}
	
	private boolean generateStats(String channel)
	{
		// set pisg path and parameters
		String[] cmd = {
			bot.s("PISG-PERL-PATH"),
			bot.s("PISG-SCRIPT"),
			"--configfile=" + bot.s("PISG-CONFIG"),
			"--channel=" + channel,
			"--outfile=" + channel.substring(1) + ".html",
			"--maintainer=" + bot.s("PISG-MAINTAINER"),
			"--format=mIRC6",
			"--network=" + bot.s("PISG-NETWORK"),
			"--dir=" + bot.s("PISG-LOG-DIRECTORY"),
			"--prefix=" + channel + "."
		};
		// execute perl script and wait for process to complete
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			try {
				process.waitFor();
			}
			catch (InterruptedException e) {
			}
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	private void updateStatsChannels()
	{
		new Thread() {
			public void run() {
				// generate channel statistics for all channels listed in database
				try {
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// retrieve list of stats channels
					Vector<String> files = new Vector<String>();
					ResultSet rs = db.query("SELECT * FROM " + bot.s("TABLE-PREFIX") + "statschannels");
					while (rs.next()) {
						String channel = rs.getString("channel");
						if (generateStats(channel)) {
							// add file to list of files to be uploaded
							files.add(channel.substring(1) + ".html");
							// log
							bot.log("pisg generated statistics for " + channel);
						}
						else {
							bot.log("pisg failed to create statistics for " + channel);
						}
					}
					// close database connection
					db.close();
					if (files.size() > 0) {
						if (bot.s("PISG-FTP").equalsIgnoreCase("TRUE")) {
							// ftp files to remote server
							try {
								// connect to ftp server
								FTPClient ftp = new FTPClient();
								ftp.connect(bot.s("PISG-FTP-ADDRESS"));
								if (ftp.login(bot.s("PISG-FTP-USERNAME"), bot.s("PISG-FTP-PASSWORD"))) {
									// check that we've connected successfully
									int reply = ftp.getReplyCode();
									if (FTPReply.isPositiveCompletion(reply)) {
										// connection + login successfull
										// change to upload directory
										if (ftp.changeWorkingDirectory(bot.s("PISG-FTP-DIRECTORY"))) {
											ftp.setFileType(ftp.BINARY_FILE_TYPE);
											// upload all generated stats files to ftp server
											for (String filename: files) {
												// create input stream to file to upload
												File file = new File(filename);
												InputStream input = new FileInputStream(file);
												// attempt to store file
												if (ftp.storeFile(filename, input)) {
													bot.log("pisg uploaded " + filename + " to ftp server");
												}
												else {
													bot.log("pisg failed to upload " + filename + " to ftp server");
												}
												// close file input stream
												input.close();
												// delete local temporary stats file
												file.delete();
											}
										}
										else {
											bot.s("pisg failed to change directory on ftp server");
										}
									}
									else {
										bot.s("pisg failed to login to ftp server");
									}
								}
								else {
									bot.s("pisg failed to login to ftp server");
								}
								// log out of server
								ftp.logout();
								// disconnect
								ftp.disconnect();
							}
							catch (IOException e) {
								System.out.println(e.toString());
								bot.log("pisg failed due to an ftp error");
							}
						}
						else {
							// copy files to local directory
							for (String filename: files) {
								File file = new File(filename);
								File destination = new File(bot.s("PISG-OUTPUT-DIRECTORY"));
								file.renameTo(new File(destination, file.getName()));
							}
						}
					}
					else {
						bot.log("pisg failed to generate any channel statistics");
					}
				}
				catch (SQLException e) {
					bot.log("An error occured whilst retrieving stats channels from database");
				}
			}
		}.start();
	}
	
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************

	public void cmdAddStatsChan(final MessageInfo m, final String[] p)
	{
		if (p.length == 1) {
			String channel = p[0];
			if (!isStatsChannel(channel)) {
				try {
					// attempt to add stats channel to database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// insert channel in statschannels table
					PreparedStatement ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "statschannels VALUES (?)");
					ps.setString(1, channel);
					ps.executeUpdate();
					// close database connection
					db.close();
					// notice user
					bot.notice(m.getNickname(), "" + channel + " added to my stats channel list");
					// log
					bot.log(m.getNickname() + " added " + channel + " to the stats channel list");
				}
				catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst adding channel to database");
				}
			}
			else {
				bot.notice(m.getNickname(), "" + channel + " is already in my stats channel list");
			}
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !addstatschan (channel)");
		}
	}
	
	public void cmdDelStatsChan(final MessageInfo m, final String[] p)
	{
    	if (p.length == 1) {
    		String channel = p[0];
	    	if (isStatsChannel(channel)) {
				try {
					// attempt to remove stats channel from database
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// delete stats channel from database
					PreparedStatement ps = db.prepareStatement("DELETE FROM " + bot.s("TABLE-PREFIX") + "statschannels WHERE channel=?");
					ps.setString(1, channel);
					ps.executeUpdate();
					// close database connection
					db.close();
					// notice user
					bot.notice(m.getNickname(), "" + channel + " removed from my stats channel list");
					// log
					bot.log(m.getNickname() + " removed " + channel + " from the stats channel list");
				}
				catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst removing news from database");
				}
	    	}
	    	else {
	    		bot.notice(m.getNickname(), "" + channel + " is not in my stats channel list");
	    	}
    	}
    	else {
			bot.notice(m.getNickname(), "Syntax: !delstatschan (channel)");
    	}
	}
	
	public void cmdListStatsChans(final MessageInfo m, final String[] p)
	{
    	// build single list of stats channels
    	String list = "";
    	try {
			// connect to database
			DatabaseConnection db = bot.getDatabaseConnection();
			// retrieve list of stats channels
			ResultSet rs = db.query("SELECT * FROM " + bot.s("TABLE-PREFIX") + "statschannels");
			while (rs.next()) {
				String channel = rs.getString("channel");
				list += channel + ", ";
			}
			// close database connection
			db.close();
			// send list of stats channel
			bot.notice(m.getNickname(), "Stats Channel List");
			bot.notice(m.getNickname(), list);
    	}
    	catch (SQLException e) {
    		bot.notice(m.getNickname(), "An error occured whilst retrieving the stats channel list");
    	}
	}
	
	public void cmdUpdateStatsChans(final MessageInfo m, final String[] p)
	{
		// notice user
		bot.notice(m.getNickname(), "pisg will now generate channel statistics");
		// log
		bot.log(m.getNickname() + " is manually generating channel statistics");
		// update stats channels
		updateStatsChannels();
	}
	
	private class StatsGeneratorTask extends TimerTask
	{
		public StatsGeneratorTask()
		{
			
		}
		
		public void run()
		{
			// log
			bot.log("pisg will now automatically generate channel statistics");
			// update stats channels
			updateStatsChannels();
		}
	}
}