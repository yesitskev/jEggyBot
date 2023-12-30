// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								CACHEDRESULT
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot.util;

public class CachedResult
{
	// ****************************************************************************
	//		 DECLARATIONS
	// ****************************************************************************    	

	private Object key;				// object key
	private Object result;			// cached object
	private long time;				// time object was stored
	
	// ****************************************************************************
	//		 CONSTRUCTOR
	// ****************************************************************************
	
	public CachedResult(Object result)
	{
		this(null, result);
	}
	
	public CachedResult(Object key, Object result)
	{
		// store key, result & current time
		this.key = key;
		this.result = result;
		this.time = System.currentTimeMillis();
	}
	
	// ****************************************************************************
	//		 ACCESSOR +	MUTATOR	METHODS
	// ****************************************************************************
	
	public Object getKey()
	{
		return key;
	}
	
	public Object getResult()
	{
		return result;
	}
	
	public long getTime()
	{
		return time;
	}
	
	// ****************************************************************************
	//		 PUBLIC METHODS
	// ****************************************************************************
	
	public long getTimeDifference()
	{
		long now = System.currentTimeMillis();
		return ((now - time) / 1000);
	}
}