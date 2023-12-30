// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								FEEDITEM
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot.util.rssfeed;

public class FeedItem
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String title;
	private String link;
	private String description;
	private String author;
	private String date;
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	protected FeedItem()
	{
		
	}
	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************
	
	protected void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	protected void setLink(String link)
	{
		this.link = link;
	}
	
	public String getLink()
	{
		return link;
	}
	
	protected void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	protected void setAuthor(String author)
	{
		this.author = author;
	}
	
	public String getAuthor()
	{
		return author;
	}
	
	protected void setDate(String date)
	{
		this.date = date;
	}
	
	public String getDate()
	{
		return date;
	}
}