// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								WEATHERREADER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.Weather;

import java.io.IOException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class WeatherReader extends DefaultHandler
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private String city;
	private int windChill;
	private int windDirection;
	private double windSpeed;
	private int humidity;
	private double visibility;
	private int pressure;
	private String condition = "";
	private int temperature;
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public WeatherReader(String id) throws SAXException, IOException
	{
		// set feed url
		String feed = "http://xml.weather.yahoo.com/forecastrss?p=" + id + "&u=c";
		// load xml sax parser
		XMLReader xmlreader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
    	xmlreader.setContentHandler(this);
    	// parse feed
    	xmlreader.parse(feed);
	}
	
	// ********************************************************************************
    //          ACCESSOR +	MUTATOR	METHODS
    // ********************************************************************************
	
	public String getCity()
	{
		return city;
	}
	
	public int getWindChill()
	{
		return windChill;
	}
	
	public int getWindDirection()
	{
		return windDirection;
	}
	
	public double getWindSpeed()
	{
		return windSpeed;
	}
	
	public int getHumidity()
	{
		return humidity;
	}
	
	public double getVisibility()
	{
		return visibility;
	}
	
	public int getPressure()
	{
		return pressure;
	}
	
	public String getCondition()
	{
		return condition;
	}
	
	public int getTemperature()
	{
		return temperature;
	}
	
	// ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
	{
		if (localName.equals("location")) {
			// location attributes
			city = atts.getValue("city");
		}
		else if (localName.equals("wind")) {
			// wind attributes
			windChill = Integer.parseInt(atts.getValue("chill"));
			windDirection = Integer.parseInt(atts.getValue("direction"));
			windSpeed = Double.parseDouble(atts.getValue("speed"));
		}
		else if (localName.equals("atmosphere")) {
			// atmosphere attributes
			humidity = Integer.parseInt(atts.getValue("humidity"));
			visibility = Double.parseDouble(atts.getValue("visibility"));
			pressure = Integer.parseInt(atts.getValue("pressure"));
		}
		else if (localName.equals("condition")) {
			// condition attributes
			condition = atts.getValue("text");
			temperature = Integer.parseInt(atts.getValue("temp"));
		}
	}
}