package com.mutinycraft.irc.impl;

import org.bukkit.event.*;
import org.bukkit.event.player.*;

import com.massivecraft.factions.P;

import com.mutinycraft.irc.*;
import com.mutinycraft.irc.plugin.Plugin;

public class DefaultChatHandler extends IRCListener implements Listener {

	
	public DefaultChatHandler(IRC irc, Plugin plugin) {
		super(irc, plugin);
	}
	
	@Override
	public void onMessage(String sender, String channel, String message) {
		if(!getIRC().getGameRelay("msg"))
			return;
		String pcmd = message.split(" ")[0].substring(
				getIRC().getCommandPrefix().length());
		if(pcmd.equalsIgnoreCase("list") || pcmd.equalsIgnoreCase("who") ||
				pcmd.equalsIgnoreCase("players"))
			return;
		
		String msg = getIRC().getGameMessage("msg")
				.replace("%user%", sender)
				.replace("%recipient%", channel)
				.replace("%msg%", getIRC().toGameColor(message));
		getIRC().sendGameMessage(msg);
		
	}
	
	@Override
	public void onAction(String sender, String recipient, String action) {
		if(!getIRC().getGameRelay("me"))
			return;
		String msg = getIRC().getGameMessage("me")
				.replace("%user%", sender)
				.replace("%recipient%", recipient)
				.replace("%action%", getIRC().toGameColor(action.trim()));
		getIRC().sendGameMessage(msg);
	}
	
	@Override
	public void onNick(String oldNick, String newNick) {
		if(!getIRC().getGameRelay("nick"))
			return;
		String msg = getIRC().getGameMessage("nick")
				.replace("%oldnick%", oldNick)
				.replace("%newnick%", newNick);
		getIRC().sendGameMessage(msg);
	}
	
	@Override
	public void onKick(String channel, String user, String kicker) {
		if(!getIRC().getGameRelay("kick"))
			return;
		String msg = getIRC().getGameMessage("kick")
				.replace("%user%", user)
				.replace("%kicker%", kicker)
				.replace("%channel%", channel);
		getIRC().sendGameMessage(msg);
	}

	@EventHandler
	public void onGameMessage(AsyncPlayerChatEvent event) {
		if(!getIRC().getIrcRelay("msg"))
			return;
		if(getPlugin().isFactionsEnabled()
				&& P.p.isPlayerFactionChatting(event.getPlayer()))
			return;
		String sender = getIRC().formatPlayerName(event.getPlayer(), "msg");
		String message = event.getMessage();
		String msg = sender.replace("%msg%", message);
		getIRC().sendIrcMessage(msg);		
	}
	
	@EventHandler
	public void onGameMe(PlayerCommandPreprocessEvent event) {
		if(!getIRC().getIrcRelay("me"))
			return;
		if(getPlugin().isFactionsEnabled()
				&& P.p.isPlayerFactionChatting(event.getPlayer()))
			return;
		String cmd = event.getMessage().trim();
		String[] split = cmd.split(" ");
		if(split.length > 1 && 
				(split[0].equalsIgnoreCase("/me") ||
						split[0].equalsIgnoreCase("/mchatme"))) {
			String m = getIRC().formatPlayerName(event.getPlayer(), "me");
			String message = cmd.substring(cmd.indexOf(" ") + 1);
			String p = m.replace("%action%", message);
			getIRC().sendIrcMessage(p);
		}
	}
	
	@EventHandler
	public void onGameJoin(PlayerJoinEvent event) {
		if(!getIRC().getIrcRelay("join"))
			return;
		String msg = getIRC().formatPlayerName(event.getPlayer(), "join");
		getIRC().sendIrcMessage(msg);
	}
	
	@EventHandler
	public void onGameQuit(PlayerQuitEvent event) {
		if(!getIRC().getIrcRelay("part"))
			return;
		String msg = getIRC().formatPlayerName(event.getPlayer(), "part");
		getIRC().sendIrcMessage(msg);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if(!getIRC().getIrcRelay("kick"))
			return;
		String reason = "No reason";
		if(event.getReason() != null)
			reason = event.getReason();
		String msg = getIRC().formatPlayerName(event.getPlayer(), "kick");
		getIRC().sendIrcMessage(msg.replace("%reason%", reason));
	}
}
