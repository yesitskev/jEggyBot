package modules.serverquery;

import java.util.Comparator;

public class KillComparator implements Comparator<Player>
{
	public int compare(Player player1, Player player2)
	{
		return (player2.getKills() - player1.getKills());
	}
}