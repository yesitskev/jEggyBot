// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								PLUGINEVENTADAPTER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot;

import jirc.*;

public class PluginEventAdapter implements PluginEventListener {

	public void onRehash() {
		
	}

    public void onConnect() {
    			
    }
    
    public void onDisconnect() {
    	
    }
    
    public void onError(String error) {
    	
    }
    
    public void onMessage(User user, String target, String message) {
    	
    }
    
    public void onNotice(User user, String target, String message) {
    	
    }
    
    public void onAction(String nickname, String target, String action) {
    	
    }
    
    public void onJoin(User user, Channel channel) {
    	
    }
    
    public void onPart(User user, Channel channel) {
    	
    }
    
    public void onNickChange(User user, String newnickname) {
    	
    }
    
    public void onKick(User user, String target, Channel channel, String reason) {
    	
    }
    
    public void onChannelMode(User user, Channel channel, char mode, char operation, String parameter) {
    	
    }
    
    public void onUserMode(User user, String target, char mode, char operation) {
    	
    }
    
    public void onOp(User user, Channel channel, String target) {
    	
    }
    
    public void onDeop(User user, Channel channel, String target) {
    	
    }
    
    public void onVoice(User user, Channel channel, String target) {
    	
    }
    
    public void onDevoice(User user, Channel channel, String target) {
    	
    }
    
    public void onOtherStatus(User user, Channel channel, String target, char mode, char operation) {
    	
    }
    
    public void onBan(User user, Channel channel, String hostmask) {
    	
    }
    
    public void onUnban(User user, Channel channel, String hostmask) {
    	
    }
    
    public void onTopicChange(User user, Channel channel, String topic) {
    	
    }
    
    public void onInvite(User user, String target, String channel) {
    	
    }
    
    public void onQuit(User user, String reason) {
    	
    }
    
    public void onCtcp(User user, String ctcp) {
    	
    }
    
    public void onChatStart(ChatSession chat) {
    	
    }
    
    public void onIncomingFile(ReceiveFile receive) {
    	
    }
    
    public void onReceiveStart(ReceiveFile receive) {
    	
    }
    
    public void onReceiveComplete(ReceiveFile receive) {
    	
    }
    
    public void onSendStart(SendFile send) {
    	
    }
    
    public void onSendComplete(SendFile send) {
    	
    }
    
    public void onCommandFlood(User user, String command) {
    	
    }
}