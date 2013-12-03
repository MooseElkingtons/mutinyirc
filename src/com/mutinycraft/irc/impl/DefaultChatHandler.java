package com.mutinycraft.irc.impl;

import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import com.mutinycraft.irc.*;
import com.mutinycraft.irc.plugin.Plugin;

public class DefaultChatHandler extends IRCListener implements Listener {

	private String gameMsgPrefix = "", gameMsgSuffix ="",
					gameMePrefix = "", gameMeSuffix = "";
	private String ircMsgPrefix= "", ircMsgSuffix = "",
					ircMePrefix = "", ircMeSuffix = "";
	private boolean ircToGameColors = true, gameToIrcColors = true,
			relayMeToIrc = true, relayMeToGame = true;
	private String cmdPrefix = ".";
	
	public DefaultChatHandler(IRC irc, Plugin plugin) {
		super(irc, plugin);
		FileConfiguration cfg = plugin.getConfig();
		gameMsgPrefix = cfg.getString("game_to_irc.prefix.message");
		gameMsgSuffix = cfg.getString("game_to_irc.suffix.message");
		gameMePrefix = cfg.getString("game_to_irc.prefix.action");
		gameMeSuffix = cfg.getString("game_to_irc.suffix.action");
		
		ircMsgPrefix = cfg.getString("irc_to_game.prefix.message");
		ircMsgSuffix = cfg.getString("irc_to_game.suffix.message");
		ircMePrefix  = cfg.getString("irc_to_game.prefix.action");
		ircMeSuffix  = cfg.getString("irc_to_game.suffix.action");
		
		ircToGameColors = cfg.getBoolean("irc_to_game.send_colors");
		gameToIrcColors = cfg.getBoolean("game_to_irc.send_colors");
		
		relayMeToIrc = cfg.getBoolean("game_to_irc.relay_actions");
		relayMeToGame = cfg.getBoolean("irc_to_game.relay_actions");
		
		cmdPrefix = plugin.getConfig().getString("config.command_prefix");
	}
	
	@Override
	public void onMessage(String sender, String recipient, String message) {
		String pcmd = message.substring(cmdPrefix.length());
		if(!pcmd.equalsIgnoreCase("list") && !pcmd.equalsIgnoreCase("who") &&
				!pcmd.equalsIgnoreCase("players"))
			sendIrcMessageToGame(sender, message);
	}
	
	@Override
	public void onAction(String sender, String recipient, String action) {
		if(!relayMeToGame)
			return;
		String p = ircMePrefix+sender+ircMeSuffix;
		String msg = action.trim();
		sendIrcMessageToGame(p+" "+(ircToGameColors ?
				ChatUtil.ircToGameColors(msg):
				ChatUtil.stripIrcColors(msg)));
	}

	@EventHandler
	public void onGameMessage(AsyncPlayerChatEvent event) {
		String sender = event.getPlayer().getDisplayName();
		sendGameMessageToIrc(sender, event.getMessage());
	}
	
	@EventHandler
	public void onGameMe(PlayerCommandPreprocessEvent event) {
		if(!relayMeToIrc)
			return;
		String cmd = event.getMessage().trim();
		String[] split = cmd.split(" ");
		if(split.length > 1 && split[0].equalsIgnoreCase("/me")) {
			String p = gameMePrefix+event.getPlayer().getDisplayName()
					+gameMeSuffix;
			String message = cmd.substring(cmd.indexOf(" "));
			String msg = p + (gameToIrcColors ? ChatUtil.gameToIrcColors(message):
				ChatUtil.stripGameColors(message));
			for(String c : getIRC().getChannels())
				getIRC().sendMessage(c, msg);
		}
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
	
	public void sendIrcMessageToGame(String message) {
		for(Player p : getPlugin().getServer().getOnlinePlayers())
			p.sendMessage(message);
		if(getPlugin().isVerbose())
			getPlugin().getServer().getConsoleSender().sendMessage(message);
	}
	
	public void sendIrcMessageToGame(String sender, String message) {
		String msg = ircMsgPrefix + sender + ircMsgSuffix +
				(ircToGameColors ? ChatUtil.ircToGameColors(message):
					ChatUtil.stripIrcColors(message));
		sendIrcMessageToGame(msg);
	}
	
	public void sendGameMessageToIrc(String message) {
		for(String channel : getIRC().getChannels())
			getIRC().sendMessage(channel, message);
	}
	
	public void sendGameMessageToIrc(String sender, String message) {
		String p = "";
		if(sender != null)
			p = gameMsgPrefix + sender + gameMsgSuffix;
		String msg = p + (gameToIrcColors ? ChatUtil.gameToIrcColors(message):
							ChatUtil.stripGameColors(message));
		sendGameMessageToIrc(msg);
	}

}
