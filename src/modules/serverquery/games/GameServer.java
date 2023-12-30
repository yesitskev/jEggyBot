package modules.serverquery.games;

import modules.serverquery.*;

import java.util.ArrayList;

import java.io.*;
import java.net.*;

public class GameServer
{
	protected static byte[] getQueryResponse(String address, int port, byte[] query) throws IOException
	{
		// resolve server address to ip
		InetAddress ip = InetAddress.getByName(address);
		// connect to server
		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(5000);
		// create and send query packet
		//byte[] bytes = query.getBytes("ISO-8859-1");
		DatagramPacket pckOut = new DatagramPacket(query, query.length, ip, port);
		socket.send(pckOut);
		// allocate buffer space for response packet
		byte[] buffer = new byte[2048];
		// wait for response
		DatagramPacket pckIn = new DatagramPacket(buffer, buffer.length);
		socket.receive(pckIn);
		// close socket
		socket.close();
		return buffer;
	}
	
	public static ServerDetails getServerDetails(String address, int port)
	{
		return null;
	}
	
	public static ArrayList getPlayerList(String address, int port)
	{
		return null;
	}
	
	public static long getPing(String address, int port)
	{
		return -1;
	}
}