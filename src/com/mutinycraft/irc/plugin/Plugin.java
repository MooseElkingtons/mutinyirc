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
	private boolean verbose = false;
	private List<String> listeners = new ArrayList<String>();
	
	@Override
	public void onEnable() {
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
		irc.setPass(getConfig().getString("config.pass"));
		server = getConfig().getString("config.server");
		port = getConfig().getInt("config.port");
		verbose = getConfig().getBoolean("config.verbose");
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public void loadHandlers() throws IllegalArgumentException {
		FileConfiguration cfg = getConfig();
		listeners.addAll(cfg.getStringList("advanced.listeners"));
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
