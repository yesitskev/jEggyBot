// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								COMMANDEXCEPTION
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

public class CommandException extends RuntimeException
{
    // ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
	
	public CommandException(String error)
	{
		super(error);
	}
}