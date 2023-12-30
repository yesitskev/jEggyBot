// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PLUGINEVENTLISTENER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import jirc.*;

import java.util.EventListener;

public interface PluginEventListener extends EventListener
{
	void onRehash();
	
	void onConnect();
	
	void onDisconnect();
	
	void onError(String error);
	
	void onMessage(User user, String target, String message);
	
	void onNotice(User user, String target, String message);
	
	void onAction(String nickname, String target, String action);
	
	void onJoin(User user, Channel channel);
	
	void onPart(User user, Channel channel);
	
	void onNickChange(User user, String newnickname);
	
	void onKick(User user, String target, Channel channel, String reason);
	
	void onChannelMode(User user, Channel channel, char mode, char operation, String parameter);
	
	void onUserMode(User user, String target, char mode, char operation);
	
	void onOp(User user, Channel channel, String target);
	
	void onDeop(User user, Channel channel, String target);
	
	void onVoice(User user, Channel channel, String target);
	
	void onDevoice(User user, Channel channel, String target);
	
	void onOtherStatus(User user, Channel channel, String target, char mode, char operation);
	
	void onBan(User user, Channel channel, String hostmask);
	
	void onUnban(User user, Channel channel, String hostmask);
	
	void onTopicChange(User user, Channel channel, String topic);
	
	void onInvite(User user, String target, String channel);
	
	void onQuit(User user, String reason);

	void onCtcp(User user, String ctcp);
	
	void onChatStart(ChatSession chat);
	
	void onIncomingFile(ReceiveFile receive);
	
	void onReceiveStart(ReceiveFile receive);
	
	void onReceiveComplete(ReceiveFile receive);
	
	void onSendStart(SendFile send);
	
	void onSendComplete(SendFile send);
	
	void onCommandFlood(User user, String command);
}