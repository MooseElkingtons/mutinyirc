package com.mutinycraft.irc;

import java.util.*;

import org.bukkit.configuration.file.*;

import com.mutinycraft.irc.impl.*;
import com.mutinycraft.irc.plugin.*;

/**
 * Used for loading different Listeners/handlers to allow
 * plugins such as MChat to control the chat.
 * 
 * @author MooseElkingtons
 */

public class ExtensionLoader {

	private Plugin plugin;
	private IRC irc;
	private List<String> listeners = new ArrayList<String>();
	
	public ExtensionLoader(IRC irc, Plugin plugin) {
		this.plugin = plugin;
		this.irc = irc;
	}
	
	public void load() throws IllegalArgumentException {
		FileConfiguration cfg = plugin.getConfig();
		listeners.addAll(cfg.getStringList("advanced.listeners"));
		for(String s : listeners) {
			switch(s) {
				case "DefaultChatHandler":
					irc.registerIRCListener(new DefaultChatHandler(irc, plugin));
					break;
				
				default:
					throw new IllegalArgumentException("Unknown Extension "+s);
			}
		}
	}
}
