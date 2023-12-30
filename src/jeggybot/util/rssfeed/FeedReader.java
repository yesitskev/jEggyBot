// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								FEEDREADER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot.util.rssfeed;

import java.io.IOException;

import java.util.Vector;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
    
public class FeedReader extends DefaultHandler
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String feed;
	private String title;
	private String link;
	private String description;
	private String language;
	private Vector items;
	private FeedItem cFeedItem;
	private String cValue;
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public FeedReader(String feed) throws FeedException
	{
		// initialisation
		items = new Vector<FeedItem>();
		// set feed url
		this.feed = feed;
		try {
	    	// load xml sax parser
			XMLReader xmlreader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
	        xmlreader.setContentHandler(this);
	        // parse feed
	        xmlreader.parse(feed);
		}
		catch (SAXException e) {
			throw new FeedException(e.getMessage());
		}
		catch (IOException e) {
			throw new FeedException(e.getMessage());
		}
	}
	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************
	
	public String getFeed()
	{
		return feed;
	}

	public String getTitle()
	{
		return title;
	}

	public String getLink()
	{
		return link;
	}

	public String getDescription()
	{
		return description;
	}

	public String getLanguage()
	{
		return language;
	}

	public Vector getFeedItems()
	{
		return items;
	}
	
	// ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************
	
	public void startElement(String uri, String name, String qName, Attributes atts)
	{
		// reset temporarily stored value
		cValue = null;
		if (name.equals("item")) {
			// create new feed item
			cFeedItem = new FeedItem();
		}
	}
	
    public void characters (char ch[], int start, int length)
    {
    	// temporarily store this value which is between the
    	// starting and ending elements
    	if (cValue == null) {
    		cValue = new String(ch, start, length);
    	}
    	else {
    		// data was broken up, therefore append rest of string
    		cValue += new String(ch, start, length);
    	}
    }
	
    public void endElement (String uri, String name, String qName)
    {
    	// determine whether this element belongs to the rss feed or a
    	// news item within the feed
    	if (cFeedItem != null) {
    		// feed item properties
    		if (name.equals("title")) {
    			cFeedItem.setTitle(cValue);
    		}
    		else if (name.equals("link")) {
    			cFeedItem.setLink(cValue);
    		}
    		else if (name.equals("description")) {
    			cFeedItem.setDescription(cValue);
    		}
    		else if (name.equals("author")) {
    			cFeedItem.setAuthor(cValue);
    		}
    		else if (name.equals("pubDate")) {
    			cFeedItem.setDate(cValue);
    		}
			else if (name.equals("item")) {
				// add item to feed items
				items.add(cFeedItem);
				// reset temporary feed item
				cFeedItem = null;
			}
    	}
    	else {
    		// rss feed properties
    		if (name.equals("title")) {
    			title = cValue;
    		}
    		else if (name.equals("link")) {
    			link = cValue;
    		}
    		else if (name.equals("description")) {
    			description = cValue;
    		}
    		else if (name.equals("language")) {
    			language = cValue;
    		}
    	}
    }
}