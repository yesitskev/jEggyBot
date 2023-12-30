package modules.serverquery.games;

import modules.serverquery.*;

import java.util.*;

import java.io.IOException;

public class Quake3Server extends GameServer
{
	public static ServerDetails getServerDetails(String address, int port)
	{
		try {
			// get server details
			// FF FF FF FF 67 65 74 73 74 61 74 75 73 0A
			byte[] query = new byte[]{-1, -1, -1, -1, 103, 101, 116, 115, 116, 97, 116, 117, 115, 10};
			byte[] buffer = getQueryResponse(address, port, query);
			// parse response
			if ((buffer != null) && (buffer.length > 0)) {
				ServerDetails sd = new ServerDetails();
				int p = 0;
				// 4 bytes must be equal to 0xFF
				for (int x = 0; x < 4; x++) {
					if (buffer[p++] != (byte) 0xFF) return null;
				}
				// convert bytes to a string
				String data = new String(buffer, 4, buffer.length - 4);
				// trim off white spaces and split string by new lines
				data = data.trim();
				String tmp[] = data.split("\n");
				// tmp should contain a minimum of 3 parts
				// 1 - status response, 2 - server details, 3 - player details
				if (tmp.length < 3) return null;
				// status response
				if (!tmp[0].equals("statusResponse")) return null;
				// server details
				// setting and values are separated by \'s
				// store settings and values in a hashmap
				HashMap<String, String> cvars = new HashMap<String, String>();
				tmp[1] = tmp[1].substring(1);
				String[] val = tmp[1].split("\\\\");
				for (int x = 0; x < val.length; x += 2) {
					cvars.put(val[x], val[x + 1]);
				}
				// get specific server cvars and set in serverdetails
				sd.setServerName((cvars.get("sv_hostname")).trim());
				sd.setMap(cvars.get("mapname"));
				String game = cvars.get("gamename");
				if (game.equalsIgnoreCase("OSP"))			game = "Quake 3 OSP";
				else if (game.equalsIgnoreCase("ARENA"))	game = "Quake 3 Arena";
				else if (game.matches("^(Q3UT)[0-9]$"))		game = "Quake 3 Urban Terror";
				else										game = "Quake 3";
				sd.setGameName(game);
				sd.setPlayerCount(tmp.length - 2);
				sd.setMaxPlayers(Integer.parseInt(cvars.get("sv_maxclients")));
				return sd;
			}
		}
		catch (IOException e) {
		}
		return null;
	}

	public static ArrayList getPlayerList(String address, int port)
	{
		try {
			// get player list
			// FF FF FF FF 67 65 74 73 74 61 74 75 73 0A
			byte[] query = new byte[]{-1, -1, -1, -1, 103, 101, 116, 115, 116, 97, 116, 117, 115, 10};
			byte[] buffer = getQueryResponse(address, port, query);
			// parse response
			if ((buffer != null) && (buffer.length > 0)) {
				ServerDetails sd = new ServerDetails();
				int p = 0;
				// 4 bytes must be equal to 0xFF
				for (int x = 0; x < 4; x++) {
					if (buffer[p++] != (byte) 0xFF) return null;
				}
				// convert bytes to a string
				String data = new String(buffer, 4, buffer.length - 4);
				// trim off white spaces and split string by new lines
				data = data.trim();
				String tmp[] = data.split("\n");
				// tmp should contain a minimum of 3 parts
				// 1 - status response, 2 - server details, 3 - player details
				if (tmp.length < 3) return null;
				// status response
				if (!tmp[0].equals("statusResponse")) return null;
				// player details
				// create new player list
				ArrayList players = new ArrayList();
				for (int x = 2; x < tmp.length; x++) {
					// create new player and add player to player list
					Player player = new Player();
					players.add(player);
					// player details are in the format of score ping "name"
					String[] ply = tmp[x].split(" ");
					// strip quotes and colour codes from player name
					// eg: ^, ^1, ^A, x112233
					String name = ply[2].substring(1, ply[2].length() - 1);
					name = name.replaceAll("(?i)\\^(x.{6}|.)", "");
					int score = Integer.parseInt(ply[0]);
					player.setName(name);
					player.setKills(score);
				}
				return players;
			}
		}
		catch (IOException e) {
		}
		return null;
	}
	
	public static long getPing(String address, int port)
	{
		// calculate time between sending and receiving a info request
		long start = System.currentTimeMillis();
		getServerDetails(address, port);
		long end = System.currentTimeMillis();
		return (end - start);
	}
}