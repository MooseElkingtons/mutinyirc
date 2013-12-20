package com.mutinycraft.irc;

import com.mutinycraft.irc.plugin.Plugin;

/**
 * IRC Event listener.
 * 
 * @author MooseElkingtons
 */

public abstract class IRCListener {
	
	private IRC irc;
	private Plugin plugin;
	
	public IRCListener(IRC irc, Plugin plugin) {
		this.irc = irc;
		this.plugin = plugin;
	}
	
	/**
	 * Invoked when the IRC bridge connects.
	 * 
	 */
	public void onConnect() {
		
	}
	
	/**
	 * Invoked when the IRC bridge disconnect/quits.
	 * 
	 */
	public void onDisconnect() {
		
	}
	
	/**
	 * listens for users who join the channel.
	 * 
	 * @param user The user who joined the channel.
	 * @param channel The channel the user joined.
	 */
	public void onJoin(String user, String login, String host, 
			String channel) {
		
	}
	
	/**
	 * listens for users who leave the channel.
	 * 
	 * @param user The user who left the channel.
	 * @param channel The channel the user parted from.
	 */
	public void onPart(String user, String channel) {
		
	}
	
	/**
	 * listens for users who disconnect from IRC.
	 * 
	 * @param user The user who disconnected from IRC.
	 * @param reason The reason why they disconnected.
	 */
	public void onQuit(String user, String reason) {
		
	}
	
	/**
	 * listens for actions/"me"s.
	 * 
	 * @param sender The user which sent the action.
	 * @param recipient The channel/user the action was relayed to.
	 * @param action The action sent by the user.
	 */
	public void onAction(String sender, String recipient, String action) {
		
	}
	
	/**
	 * listens for messages.
	 * 
	 * @param sender The user which sent the message.
	 * @param channel The channel the message was relayed to.
	 * @param message The message sent by the user.
	 */
	public void onMessage(String sender, String channel, String message) {
		
	}
	
	/**
	 * listens for notices.
	 * 
	 * @param sender The user which sent the notice.
	 * @param recipient The channel/user the message was relayed to.
	 * @param notice The notice sent by the user.
	 */
	public void onNotice(String sender, String recipient, String notice) {
		
	}
	
	/**
	 * listens for channel modes changed.
	 * 
	 * @param channel The channel
	 * @param user The user who changed the modes.
	 * @param modes Mode(s) added or removed.
	 */
	public void onModeChanged(String channel, String user, String modes) {
		
	}
	
	/**
	 * listens for server-to-client responses.
	 * 
	 * @param code Server-to-Client reply. See {@link ReplyConstants}.
	 * @param response The String Response included with the reply code.
	 */
	public void onServerResponse(int code, String response) {
		
	}
	
	/**
	 * Listens for Client-to-Client protocol (CTCP) queries.
	 * 
	 * @param sender The sender of the CTCP query
	 * @param ctcp The query string.
	 */
	public void onCTCP(String sender, String ctcp) {
		
	}
	
	/**
	 * listens for a ping sent from the IRC server.
	 * 
	 * @param response PING parameters.
	 */
	public void onPing(String response) {
		
	}
	
	/**
	 * listens for users kicked in a channel, even the IRC bridge.
	 * 
	 * @param channel The channel the kick happened.
	 * @param user The user kicked.
	 * @param kicker The user who kicked.
	 */
	public void onKick(String channel, String user, String kicker) {
		
	}
	
	/**
	 * listens for nicks changing.
	 * 
	 * @param oldNick old Nick
	 * @param newNick new Nick
	 */
	public void onNick(String oldNick, String newNick) {
		
	}
	
	/**
	 *
	 * @return MutinyIRC Plugin.
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * 
	 * @return IRC Bridge.
	 */
	public IRC getIRC() {
		return irc;
	}
}