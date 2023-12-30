// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								NEWS
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins;

import jeggybot.*;

import jeggybot.util.DatabaseConnection;
import jeggybot.util.Utils;

import jirc.Channel;
import jirc.User;

import java.util.Date;

import java.sql.*;

public class plgNews extends Plugin
{
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgNews()
	{
		// set plugin information
		name = "Channel News";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!news", "cmdNews", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!addnews", "cmdAddNews", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!delnews", "cmdDelNews", ACCESS.GLOBAL, LEVEL.USER, this);
		// register plugin events
		ph.addEventListener(this, new PluginEventAdapter() {
			public void onJoin(User user, Channel channel) {
				onJoinEvent(user, channel);
			}
		});
	}
	
	// ********************************************************************************
    //          IRC EVENTS
    // ********************************************************************************
    
    private void onJoinEvent(User user, Channel channel)
    {
    	if (!bot.isMe(user)) {
			// display latest news item to user
			try {
				// connect to database
				DatabaseConnection db = bot.getDatabaseConnection();
				// retrieve latest news item
				PreparedStatement ps = db.prepareStatement("SELECT * FROM " + bot.s("TABLE-PREFIX") + "news WHERE channel=? ORDER BY date DESC LIMIT 1");
				ps.setString(1, channel.getName());
				ResultSet rs = ps.executeQuery();
				if (db.getRowCount(rs) > 0) {
					// get columns from last row
					rs.next();
					int id = rs.getInt("id");
					Timestamp ts = rs.getTimestamp("date");
					String text = rs.getString("text");
					String poster = rs.getString("poster");
					// send latest news item
					bot.notice(user.getNickname(), "Latest News: " + bot.getFormattedTime(ts, "dd/MM/yyyy") + " * " + text + " (" + poster + ", #" + id + ")");
					bot.notice(user.getNickname(), "For more " + channel.getName() + " channel news, type !news");
				}
				// close database connection
				db.close();
			}
			catch (SQLException e) {
			}
    	}
    }
	
	// ********************************************************************************
    //          BOT COMMANDS
    // ********************************************************************************

	private void showNews(String nickname, String channel, int items)
	{
		if ((items >= 1) && (items <= 10)) {
			try {
				// connect to database
				DatabaseConnection db = bot.getDatabaseConnection();
				// retrieve news for this channel
				PreparedStatement ps = db.prepareStatement("SELECT * FROM " + bot.s("TABLE-PREFIX") + "news WHERE channel=? ORDER BY date DESC LIMIT ?");
				ps.setString(1, channel);
				ps.setInt(2, items);
				ResultSet rs = ps.executeQuery();
				if (db.getRowCount(rs) > 0) {
					// display news items
					bot.notice(nickname, "" + channel + " news");
					while (rs.next()) {
						int id = rs.getInt("id");
						Timestamp ts = rs.getTimestamp("date");
						String text = rs.getString("text");
						String poster = rs.getString("poster");
						// send news items to user
						bot.notice(nickname, "o " + bot.getFormattedTime(ts, "dd/MM/yyyy") + " * " + text + " (" + poster + ", #" + id + ")");
					}
				}
				else {
					bot.notice(nickname, "I do not have any news for channel " + channel);
				}
				// close database connection
				db.close();
			}
			catch (SQLException e) {
				bot.notice(nickname, "An error occured whilst retrieving news from database");
			}
		}
		else {
			bot.notice(nickname, "You must enter a valid number between 1 and 10");
		}
	}

	public void cmdNews(final MessageInfo m, final String[] p)
	{
		if ((p.length == 0) && (m.getTarget().startsWith("#"))) {
			showNews(m.getNickname(), m.getTarget(), bot.i("MAX-NEWS-ITEMS"));
		}
		else if (p.length >= 1) {
			String channel = p[0];
			int items;
			// set number of news items to show
			if (p.length > 1) {
				try {
					items = Integer.parseInt(p[1]);
				}
				catch (NumberFormatException e) {
					bot.notice(m.getNickname(), "You must enter a valid number between 1 and 10");
					return;
				}
			}
			else {
				items = bot.i("MAX-NEWS-ITEMS");
			}
			showNews(m.getNickname(), channel, items);
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !news (#channel) [number of items]");
		}
	}
	
	public void cmdAddNews(final MessageInfo m, final String[] p)
	{
		if (p.length >= 2) {
			String channel = p[0];
			String text = Utils.arrayToString(p, 1);
			if (bot.isLevel(m.getNickname(), channel, LEVEL.SOP)) {
				// add news item to database
				try {
					// connect to database
					DatabaseConnection db = bot.getDatabaseConnection();
					// add news to database
					PreparedStatement ps = db.prepareStatement("INSERT INTO " + bot.s("TABLE-PREFIX") + "news (channel, date, text, poster) VALUES (?, ?, ?, ?)");
					ps.setString(1, channel);
					ps.setTimestamp(2, new Timestamp(new Date().getTime()));
					ps.setString(3, text);
					ps.setString(4, m.getNickname());
					ps.executeUpdate();
					// close database connection
					db.close();
					// notice user
					bot.notice(m.getNickname(), "Added news item for channel " + channel);
					// log
					bot.log(m.getNickname() + " added news for channel " + channel);
				}
				catch (SQLException e) {
					bot.notice(m.getNickname(), "An error occured whilst adding news to database");
				}
			}
			else {
				bot.notice(m.getNickname(), "You do not have access to add news for " + channel);
			}
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !addnews (#channel) (text)");
		}
	}
	
	public void cmdDelNews(final MessageInfo m, final String[] p)
	{
		if (p.length == 2) {
			String channel = p[0];
			int id;
			if (bot.isLevel(m.getNickname(), channel, LEVEL.SOP)) {
				if (p[1].matches("\\d+")) {
					id = Integer.parseInt(p[1]);
					// remove news item from database
					try {
						// connect to database
						DatabaseConnection db = bot.getDatabaseConnection();
						// determine if specified news id exists
						PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + bot.s("TABLE-PREFIX") + "news WHERE channel=? AND id=?");
						ps.setString(1, channel);
						ps.setInt(2, id);
						ResultSet rs = ps.executeQuery();
						rs.next();
						int rows = rs.getInt(1);
						if (rows > 0) {
							// delete the news item
							ps = db.prepareStatement("DELETE FROM " + bot.s("TABLE-PREFIX") + "news WHERE channel=? AND id=?");
							ps.setString(1, channel);
							ps.setInt(2, id);
							ps.executeUpdate();
							// notice user
							bot.notice(m.getNickname(), "Removed news item #" + id + " from channel " + channel);
							// log
							bot.log(m.getNickname() + " removed news item " + id + " from channel " + channel);
						}
						else {
							bot.notice(m.getNickname(), "The news item which you've specified does not exist");
						}
						// close database connection
						db.close();
					}
					catch (SQLException e) {
						bot.notice(m.getNickname(), "An error occured whilst removing news from database");
					}
				}
				else {
					bot.notice(m.getNickname(), "You must enter a valid news item number");
				}
			}
			else {
				bot.notice(m.getNickname(), "You do not have access to remove news from " + channel);
			}
		}
		else {
			bot.notice(m.getNickname(), "Syntax: !delnews (#channel) (id)");
		}
	}
}