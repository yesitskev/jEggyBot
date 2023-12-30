package modules.serverquery;

public class Player
{
	String name;
	int kills;
	int deaths;
	float time;
	
	public Player()
	{
		
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public int getKills()
	{
		return kills;
	}
	
	public void setKills(int kills)
	{
		this.kills = kills;
	}
	
	public int getDeaths()
	{
		return deaths;
	}
	
	public void setDeaths(int deaths)
	{
		this.deaths = deaths;
	}
	
	public float getTimeConnected()
	{
		return time;
	}
	
	public void setTimeConnected(float time)
	{
		this.time = time;
	}
}