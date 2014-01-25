package com.mutinycraft.irc.plugin;

import org.bukkit.entity.*;
import org.bukkit.event.*;

import com.mutinycraft.irc.ChatUtil;
import com.mutinycraft.irc.IRC;
import com.mutinycraft.irc.IRCListener;

/**
 * IRC Command Listener, mainly for listing players.
 * 
 * @author MooseElkingtons
 */

public class IRCCommandListener extends IRCListener implements Listener {

	private String cmdPrefix = ".";
	
	public IRCCommandListener(IRC irc, Plugin plugin, String prefix) {
		super(irc, plugin);
		cmdPrefix = prefix;
	}
	
	@Override
	public void onMessage(String user, String channel, String message) {
		if(message.startsWith(cmdPrefix)) {
			String cmd = message.split(" ")[0].substring(cmdPrefix.length());
			switch(cmd) {
				case "list":
				case "players":
				case "who":
					Player[] players = getPlugin().getServer()
						.getOnlinePlayers();
					String plist = String.format(
							"Online players (%s/%s): ",
							players.length,
							getPlugin().getServer().getMaxPlayers());
					if(players.length < 1)
						plist += "No players online.";
					else {
						for(Player p : players)
							plist+=ChatUtil.gameToIrcColors(
								p.getDisplayName())+ChatUtil.IRC_RESET+", ";
						plist = plist.substring(0, plist.lastIndexOf(','));
					}
					getIRC().sendMessage(channel, plist);
				break;
			}
		}
	}
	
	public void setCommandPrefix(String prefix) {
		cmdPrefix = prefix;
	}
}
