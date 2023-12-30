package plugins.ServerQuery;

public class PROTOCOL
{
	public static final int GOLDSRC = 0;
	public static final int SRC = 1;
	public static final int Q3 = 2;
	public static final int NONE = -1;
	
	public static int getProtocol(String game)
	{
		game = game.toUpperCase();
		if (game.equals("HL")) {
			return GOLDSRC;
		}
		else if (game.equals("SRC")) {
			return SRC;
		}
		else if (game.equals("Q3")) {
			return Q3;
		}
		else {
			return NONE;
		}
	}
}