package com.mutinycraft.irc.plugin;

import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import com.mutinycraft.irc.ChatUtil;
import com.mutinycraft.irc.IRC;
import com.mutinycraft.irc.IRCListener;

public class GameListener extends IRCListener implements Listener {

	private String gamePrefix = "", gameSuffix = "",
					ircPrefix = "", ircSuffix = "";
	private String cmdPrefix = ".";
	private boolean ircToGameColors = false,
					gameToIrcColors = true;
	
	public GameListener(IRC irc, Plugin plugin) {
		super(irc, plugin);
		FileConfiguration cfg = plugin.getConfig();
		if(cfg.contains("game_to_irc.prefix"))
			gamePrefix = cfg.getString("game_to_irc.prefix");
		if(cfg.contains("game_to_irc.suffix"))
			gameSuffix = cfg.getString("game_to_irc.suffix");
		
		if(cfg.contains("irc_to_game.prefix"))
			ircPrefix = cfg.getString("irc_to_game.prefix");
		if(cfg.contains("irc_to_game.suffix"))
			ircSuffix = cfg.getString("irc_to_game.suffix");
		
		cmdPrefix = cfg.getString("config.command_prefix");
		ircToGameColors = cfg.getBoolean("irc_to_game.send_colors");
		gameToIrcColors = cfg.getBoolean("game_to_irc.send_colors");
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
							plist += p.getDisplayName()+", ";
						plist = plist.substring(0, plist.lastIndexOf(','));
					}
					getIRC().sendMessage(channel, plist);
				return;
			}
		}
		sendIrcMessageToGame(user, ChatUtil.stripGameColors(message));
	}
	
	@EventHandler
	public void onGameMessage(AsyncPlayerChatEvent event) {
		String sender = event.getPlayer().getDisplayName();
		sendGameMessageToIrc(sender, event.getMessage());
	}
	
	@EventHandler
	public void onGameJoin(PlayerJoinEvent event) {
		String msg = event.getJoinMessage();
		sendGameMessageToIrc(ChatUtil.stripGameColors(msg));
	}
	
	@EventHandler
	public void onGameQuit(PlayerQuitEvent event) {
		String msg = event.getQuitMessage();
		sendGameMessageToIrc(ChatUtil.stripGameColors(msg));
	}
	
	public void sendIrcMessageToGame(String sender, String message) {
		String msg = ircPrefix + sender + ircSuffix +
				(ircToGameColors ? ChatUtil.ircToGameColors(message):
					ChatUtil.stripIrcColors(message));
		for(Player p : getPlugin().getServer().getOnlinePlayers())
			p.sendMessage(msg);
	}
	
	public void sendGameMessageToIrc(String message) {
		for(String channel : getIRC().getChannels())
			getIRC().sendMessage(channel, message);
	}
	
	public void sendGameMessageToIrc(String sender, String message) {
		String p = "";
		if(sender != null)
			p = gamePrefix + sender + gameSuffix;
		String msg = p + (gameToIrcColors ? ChatUtil.gameToIrcColors(message):
							ChatUtil.stripGameColors(message));
		sendGameMessageToIrc(msg);
	}
	
}
