package com.mutinycraft.irc.impl;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import com.mutinycraft.irc.IRC;
import com.mutinycraft.irc.IRCListener;
import com.mutinycraft.irc.plugin.Plugin;

/**
 * Listens for server (and IRC?) broadcast messages to relay, this isn't quite regular chat.
 * @author Hellenion
 */
public class BroadcastHandler extends IRCListener implements Listener {

	/**
	 * Initialises the BroadcastHandler
	 * @param irc The IRC bridge to bridge over.
	 * @param plugin Provides the logger for error reporting.
	 */
	public BroadcastHandler(final IRC irc, final Plugin plugin) {
		super(irc, plugin);
	}

	/**
	 * Listens for server commands, if it is the `say` command, which broadcasts a message to the server, it will relay it to IRC.
	 * @param event Container for relevant data to this event, 
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGameBroadcast(final ServerCommandEvent event)
	{
		if(event==null)throw new IllegalArgumentException("event was null");
		if(!event.getCommand().toLowerCase().startsWith("say"))return;
		if(!getIRC().getIrcRelay("say"))return;
		
		getIRC().sendIrcMessage(getIRC().formatGameMessage("say",event.getSender().getName(),event.getCommand().substring(4)));
	}
}
