package com.mutinycraft.irc.impl;

import org.bukkit.entity.Player;
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
		String pcmd = message.substring(
				getIRC().getCommandPrefix().length()).toLowerCase();
		if(pcmd.startsWith("list") || pcmd.startsWith("who") ||
				pcmd.startsWith("players"))
			return;
		
		String msg = getIRC().getGameMessage("msg")
				.replace("%user%", sender)
				.replace("%channel%", channel)
				.replace("%msg%", getIRC().toGameColor(message));
		getIRC().sendGameMessage(msg);
		
	}
	
	@Override
	public void onAction(String sender, String recipient, String action) {
		if(!getIRC().getGameRelay("me"))
			return;
		String msg = getIRC().getGameMessage("me")
				.replace("%user%", sender)
				.replace("%channel%", recipient)
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
	
	@Override
	public void onJoin(String user, String login, String host,
			String channel) {
		if(!getIRC().getGameRelay("join") || 
				user.equalsIgnoreCase(getIRC().getNick()))
			return;
		String msg = getIRC().getGameMessage("join")
				.replace("%user%", user)
				.replace("%channel%", channel);
		getIRC().sendGameMessage(msg);
	}
	
	@Override
	public void onPart(String user, String channel) {
		if(!getIRC().getGameRelay("part") || 
				user.equalsIgnoreCase(getIRC().getNick()))
			return;
		String msg = getIRC().getGameMessage("part")
				.replace("%user%", user)
				.replace("%channel%", channel);
		getIRC().sendGameMessage(msg);
	}
	
	@Override
	public void onQuit(String user, String reason) {
		onPart(user, "");
	}
	
	@Override
	public void onModeChanged(String channel, String user, String modes) {
		if(!getIRC().getGameRelay("modes") || !getIRC().isChannel(channel))
			return;
		String msg = getIRC().getGameMessage("modes")
				.replace("%user%", user)
				.replace("%channel%", channel)
				.replace("%mode%", modes);
		getIRC().sendGameMessage(msg);
	}

	
	@EventHandler(priority = EventPriority.LOW)
	public void onGameMessage(AsyncPlayerChatEvent event) {
		if(!getIRC().getIrcRelay("msg"))
			return;
		Player p = event.getPlayer();
		if(getPlugin().isFactionsEnabled()
				&& P.p.isPlayerFactionChatting(p))
			return;
		String message = event.getMessage();
		getIRC().sendIrcMessage(p, message);
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
			String m = getIRC().formatGameMessage(event.getPlayer(), "me");
			String message = cmd.substring(cmd.indexOf(" ") + 1);
			String p = m.replace("%action%", message);
			getIRC().sendIrcMessage(p);
		}
	}
	
	@EventHandler
	public void onGameJoin(PlayerJoinEvent event) {
		if(!getIRC().getIrcRelay("join"))
			return;
		String msg = getIRC().formatGameMessage(event.getPlayer(), "join");
		getIRC().sendIrcMessage(msg);
	}
	
	@EventHandler
	public void onGameQuit(PlayerQuitEvent event) {
		if(!getIRC().getIrcRelay("part"))
			return;
		String msg = getIRC().formatGameMessage(event.getPlayer(), "part");
		getIRC().sendIrcMessage(msg);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if(!getIRC().getIrcRelay("kick"))
			return;
		String reason = "No reason";
		if(event.getReason() != null)
			reason = event.getReason().replaceAll("(\r|\n)", " ");
		String msg = getIRC().formatGameMessage(event.getPlayer(), "kick");
		getIRC().sendIrcMessage(msg.replace("%reason%", reason));
	}
}
