// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								MESSAGEINFO
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import jirc.User;

 public class MessageInfo
 {
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
 	
 	private User user;				// user nickname, ident and hostname
 	private String nickname;		// nickname
 	private String ident;			// ident
 	private String hostname;		// hostname
 	private String target;			// target
 	private String text;			// messages text
 	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

 	public MessageInfo(User user, String target, String text)
 	{
 		// store message information
		this.user = user;
 		this.target = target;
 		this.text = text;
 	}
 	
	// ********************************************************************************
    //          ACCESSOR + MUTATOR METHODS
    // ********************************************************************************

	public User getUser()
	{
		return user;
	}

	public String getNickname()
	{
		return user.getNickname();
	}

	public String getIdent()
	{
		return user.getIdent();
	}

	public String getHostname()
	{
		return user.getHostname();
	}

	public String getTarget()
	{
		return target;
	}

	public String getText()
	{
		return text;
	}
 }