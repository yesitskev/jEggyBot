package modules.serverquery;

public class ServerDetails
{
	private String name;
	private String map;
	private String game;
	private int players;
	private int maxPlayers;
	
	public ServerDetails()
	{
		
	}
	
	public String getServerName()
	{
		return name;
	}
	
	public void setServerName(String name)
	{
		this.name = name;
	}
	
	public String getMap()
	{
		return map;
	}
	
	public void setMap(String map)
	{
		this.map = map;
	}
	
	public String getGameName()
	{
		return game;
	}
	
	public void setGameName(String game)
	{
		this.game = game;
	}
	
	public int getPlayerCount()
	{
		return players;
	}
	
	public void setPlayerCount(int players)
	{
		this.players = players;
	}
	
	public int getMaxPlayers()
	{
		return maxPlayers;
	}
	
	public void setMaxPlayers(int maxPlayers)
	{
		this.maxPlayers = maxPlayers;
	}
}