package com.mutinycraft.irc.plugin;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import org.bukkit.plugin.java.*;

import com.mutinycraft.irc.ExtensionLoader;
import com.mutinycraft.irc.IRC;

public class Plugin extends JavaPlugin {
	
	private IRC irc;
	private String server = "";
	private int port = 6667;
	private boolean verbose = false;
	
	@Override
	public void onEnable() {
		irc = new IRC(this);
		loadConfig();
		ExtensionLoader eload = new ExtensionLoader(irc, this);
		eload.load();
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
}
