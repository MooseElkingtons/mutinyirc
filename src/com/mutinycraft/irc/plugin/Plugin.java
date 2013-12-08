package com.mutinycraft.irc.plugin;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.*;

import com.mutinycraft.irc.*;
import com.mutinycraft.irc.impl.*;

public class Plugin extends JavaPlugin {
	
	private IRC irc;
	private String server = "";
	private int port = 6667;
	private boolean verbose = false,
			isMchatEnabled = false, isFactionsEnabled = false;
	private List<String> listeners = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		detectFactions();
		isMchatEnabled = getServer().getPluginManager()
				.isPluginEnabled("MChat");
		
		irc = new IRC(this);
		loadConfig();
		loadHandlers();
		getLogger().log(Level.INFO, "Starting IRC connection.");
		try {
			Socket socket = new Socket(server, port);
			irc.connect(socket);
		} catch(IOException e) {
			getLogger().log(Level.SEVERE, "Error initiating IRC connection", e);
		}

		getLogger().log(Level.INFO, "MutinyIRC Plugin Enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Disconnecting from IRC.");
		irc.disconnect();
		getLogger().log(Level.INFO, "MutinyIRC Plugin Disabled.");
	}
	
	private void loadConfig() {
		saveDefaultConfig();
		irc.setNick(getConfig().getString("config.nick"));
		if(getConfig().contains("config.pass"))
			irc.setPass(getConfig().getString("config.pass"));
		server = getConfig().getString("config.server");
		irc.setServer(server);
		port = getConfig().getInt("config.port");
		irc.setPort(port);
		verbose = getConfig().getBoolean("config.verbose");
	}
	
	private void detectFactions() {
		org.bukkit.plugin.Plugin p = getServer().getPluginManager().
				getPlugin("mcore");
		boolean isMcoreEnabled = p != null && p.isEnabled();
		isFactionsEnabled = getServer().getPluginManager()
				.isPluginEnabled("Factions") && !isMcoreEnabled;
		if(isMcoreEnabled) {
			getLogger().log(Level.SEVERE, "MCore conflicts with "
					+ "MutinyIRC; disabling MutinyIRC.");
			getLogger().log(Level.SEVERE, "If you are using Factions, it "+
					"is recommended you use version 1.6.9.4.");
			getPluginLoader().disablePlugin(this);
		}
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public boolean isMchatEnabled() {
		return isMchatEnabled;
	}
	
	public boolean isFactionsEnabled() {
		return isFactionsEnabled;
	}
	
	public void loadHandlers() throws IllegalArgumentException {
		FileConfiguration cfg = getConfig();
		listeners.addAll(cfg.getStringList("advanced.handlers"));
		for(String s : listeners) {
			switch(s) {
				case "DefaultChatHandler":
					getLogger().log(Level.INFO, "Registered "
							+ "Default Chat Handler");
					DefaultChatHandler dch = new DefaultChatHandler(irc, this);
					irc.registerIRCListener(dch);
					getServer().getPluginManager().registerEvents(dch, this);
					break;
				
				default:
					throw new IllegalArgumentException("Unknown Extension "+s);
			}
		}
	}
}
