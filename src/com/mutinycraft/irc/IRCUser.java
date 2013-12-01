package com.mutinycraft.irc;

/**
 * an IRC user in a channel.
 * 
 * @author MooseElkingtons
 */

public class IRCUser {

	/**
	 * Channel Modes
	 */
	public enum CMode {
		CMODE_NORMAL,
		CMODE_VOICE,
		CMODE_HOP,
		CMODE_OP,
		CMODE_ADMIN,
		CMODE_OWNER
	}
	
	private String nick;
	private String channel;
	private CMode mode;
	
	public IRCUser(String nick, CMode mode, String channel) {
		this.nick = nick;
		this.mode = mode;
		this.channel = channel;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getNick() {
		return nick;
	}
	
	public void setNick(String nick) {
		this.nick = nick;
	}
	
	public CMode getMode() {
		return mode;
	}
	
	public void setMode(CMode mode) {
		this.mode = mode;
	}
}
