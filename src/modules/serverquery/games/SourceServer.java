package modules.serverquery.games;

import modules.serverquery.*;

import java.util.Collections;
import java.util.ArrayList;

import java.io.IOException;

public class SourceServer extends GameServer
{
	public static ServerDetails getServerDetails(String address, int port)
	{
		try {
			// get server details
			// (A2S_INFO) FF FF FF FF 54 53 6F 75 72 63 65 20 45 6E 67 69 6E 65 20 51 75 65 72 79 00
			byte[] query = new byte[]{-1, -1, -1, -1, 84, 83, 111, 117, 114, 99, 101, 32, 69, 110, 103, 105, 110, 101, 32, 81, 117, 101, 114, 121, 0};
			byte[] buffer = getQueryResponse(address, port, query);
			// parse response
			if ((buffer != null) && (buffer.length > 0)) {
				ServerDetails sd = new ServerDetails();
				int p = 0;
				// determine if packet is valid
				// 4 bytes must be equal to 0xFF
				for (int x = 0; x < 4; x++) {
					if (buffer[p++] != (byte) 0xFF) return null;
				}
				// 1 byte equal to 0x49
				if (buffer[p++] != (byte) 0x49) return null;
				// steam version (byte)
				p++;
				// server name (string)
				StringBuffer name = new StringBuffer();
				while (buffer[p] != (byte) 0x00) {
					name.append((char) buffer[p++]);
				}
				sd.setServerName(name.toString());
				p++;
				// map (string)
				StringBuffer map = new StringBuffer();
				while (buffer[p] != (byte) 0x00) {
					map.append((char) buffer[p++]);
				}
				sd.setMap(map.toString());
				p++;
				// game directory (string)
				while (buffer[p] != (byte) 0x00) { p++; }
				p++;
				// game name (string)
				StringBuffer game = new StringBuffer();
				while (buffer[p] != (byte) 0x00) {
					game.append((char) buffer[p++]);
				}
				sd.setGameName(game.toString());
				p++;
				// application id (short)
				p++;
				p++;
				// number of players (byte)
				int nPlayers = (int) (buffer[p++] & 0xFF);
				sd.setPlayerCount(nPlayers);
				// maximum players (byte)
				int nMaxPlayers = (int) (buffer[p++] & 0xFF);
				sd.setMaxPlayers(nMaxPlayers);
				// number of bots (byte)
				// dedicated (byte)
				// os (byte)
				// password (byte)
				// secure (byte)
				// game version (string)
				return sd;
			}
		}
		catch (IOException e) {
		}
		return null;
	}
	
	private static byte[] getChallenge(String address, int port)
	{
		try {
			// get challenge number
			// (A2S_SERVERQUERY_GETCHALLENGE) FF FF FF FF 57
			byte[] query = new byte[]{-1, -1, -1, -1, 87};
			byte[] buffer = getQueryResponse(address, port, query);
			// parse response
			if ((buffer != null) && (buffer.length > 0)) {
				int p = 0;
				// determine if packet is valid
				// 4 bytes must be equal to 0xFF
				for (int x = 0; x < 4; x++) {
					if (buffer[p++] != (byte) 0xFF) return null;
				}
				// 1 byte equal to 0x41
				if (buffer[p++] != (byte) 0x41) return null;
				// challenge (long)
				byte[] challenge = new byte[4];
				for (int x = 0; x < 4; x++) {
					challenge[x] = buffer[p++];
				}
				return challenge;
			}
		}
		catch (IOException e) {
		}
		return null;
	}
	
	public static ArrayList getPlayerList(String address, int port)
	{
		// get query challenge
		byte[] challenge = getChallenge(address, port);
		if (challenge != null) {
			try {
				// get player details
				// (A2S_PLAYER) FF FF FF FF 55 challenge (4 bytes)
				byte[] query = new byte[]{-1, -1, -1, -1, 85, challenge[0], challenge[1], challenge[2], challenge[3]};
				byte[] buffer = getQueryResponse(address, port, query);
				// parse response
				if ((buffer != null) && (buffer.length > 0)) {
					int p = 0;
					// determine if packet is valid
					// 4 bytes must be equal to 0xFF
					for (int x = 0; x < 4; x++) {
						if (buffer[p++] != (byte) 0xFF) return null;
					}
					// 1 byte equal to 0x44
					if (buffer[p++] != (byte) 0x44) return null;
					// number of players (byte)
					int nPlayers = (int) (buffer[p++] & 0xFF);
					// create new player list
					ArrayList players = new ArrayList();
					// parse list of players
					for (int x = 0; x < nPlayers; x++) {
						// create new player and add player to player list
						Player player = new Player();
						players.add(player);
						// index (byte)
						p++;
						// name (string)
						StringBuffer sName = new StringBuffer();
						while (buffer[p] != (byte) 0x00) {
							sName.append((char) buffer[p++]);
						}
						player.setName(sName.toString());
						p++;
						// kills (long)
						int kills = 0;
						for (int y = 0; y < 4; y++) {
						    int shift = (4 - y - y) * 8;
						    kills += (buffer[p++] & 0xFF) << shift;
						}
						player.setKills(kills);
						// time connected (float)
						int ttime = 0;
						for (int y = 0; y < 4; y++) {
						    int shift = (4 - y - y) * 8;
						    ttime += (buffer[p++] & 0xFF) << shift;
						}
						float time = Float.intBitsToFloat(ttime);
						player.setTimeConnected(time);
					}
					return players;
				}
			}
			catch (IOException e) {
			}
		}
		return null;
	}
	
	public static long getPing(String address, int port)
	{
		// calculate time between sending and receiving a query challenge
		long start = System.currentTimeMillis();
		getChallenge(address, port);
		long end = System.currentTimeMillis();
		return (end - start);
	}
}