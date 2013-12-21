package com.mutinycraft.irc.plugin;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.*;

import com.mutinycraft.irc.*;
import com.mutinycraft.irc.impl.*;

public class Plugin extends JavaPlugin {
	
	private Chat chat;
	private IRC irc;
	private String server = "";
	private int port = 6667;
	private boolean verbose = false, everbose = false,
			isVaultEnabled = false, isFactionsEnabled = false,
			isMchatEnabled = false;
	private List<String> listeners = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		detectFactions();
		
		irc = new IRC(this);
		loadConfig();
		loadSoftDependencies();
		loadHandlers();
		getLogger().log(Level.INFO, "Starting IRC connection to "+server+".");
		try {
			Socket socket = new Socket(server, port);
			irc.connect(socket);
		} catch(IOException e) {
			getLogger().log(Level.SEVERE, "Error initiating IRC connection", e);
		}
		bindCommands();
		getLogger().log(Level.INFO, "MutinyIRC Plugin Enabled.");
	}
	
	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Disconnecting from IRC.");
		irc.disconnect();
		getLogger().log(Level.INFO, "MutinyIRC Plugin Disabled.");
	}
	
	private void bindCommands() {
		BridgeCmdExecutor ex = new BridgeCmdExecutor(irc, this);
		getCommand("voice").setExecutor(ex);
		getCommand("devoice").setExecutor(ex);
		getCommand("irckick").setExecutor(ex);
		getCommand("ircban").setExecutor(ex);
		getCommand("ircreload").setExecutor(ex);
	}
	
	public void loadConfig() {
		saveDefaultConfig();
		irc.setNick(getConfig().getString("config.nick"));
		if(getConfig().contains("config.pass"))
			irc.setPass(getConfig().getString("config.pass"));
		server = getConfig().getString("config.server");
		irc.setServer(server);
		port = getConfig().getInt("config.port");
		irc.setPort(port);
		verbose = getConfig().getBoolean("config.verbose");
		if(getConfig().contains("config.extra_verbose"))
			everbose = getConfig().getBoolean("config.extra_verbose");
	}
	
	private void loadSoftDependencies() {
		isVaultEnabled = getServer().getPluginManager().isPluginEnabled("Vault");
        if(isVaultEnabled){
            isVaultEnabled = getConfig().getBoolean("advanced.vault_support", true);
        }
		if(isVaultEnabled) {
			RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager()
					.getRegistration(Chat.class);
			if (chatProvider != null)
				chat = chatProvider.getProvider();
			if(chat == null)
				isVaultEnabled = false;
		}
		isMchatEnabled = getServer().getPluginManager()
				.isPluginEnabled("MChat") ||
				getServer().getPluginManager().isPluginEnabled("mChatSuite");
        if(isMchatEnabled){
            isMchatEnabled = getConfig().getBoolean("advanced.mchat_support", true);
        }
		
		if(isVaultEnabled)
			getLogger().log(Level.INFO, "Vault detected. Will accept Vault "
					+ "vars.");
		else
			getLogger().log(Level.WARNING, "Vault not enabled.");
		if(isFactionsEnabled)
			getLogger().log(Level.INFO, "Factions detected. Will accept "
					+ "faction vars.");
		else
			getLogger().log(Level.WARNING, "Factions not enabled.");
		if(isMchatEnabled)
			getLogger().log(Level.INFO, "MChat detected. Will accept "
					+ "MChat vars.");
		else
			getLogger().log(Level.WARNING, "MChat not Enabled.");

	}
	
	private void detectFactions() {
		org.bukkit.plugin.Plugin p = getServer().getPluginManager().
				getPlugin("mcore");
		boolean isMcoreEnabled = p != null && p.isEnabled();
		isFactionsEnabled = getServer().getPluginManager()
				.isPluginEnabled("Factions") && !isMcoreEnabled;
        if(isFactionsEnabled) {
            isFactionsEnabled = getConfig().getBoolean("advanced.factions_support", true);
        }
		if(isMcoreEnabled) {
			getLogger().log(Level.WARNING, "Your version of Factions does not "
					+ "support MutinyIRC.");
			getLogger().log(Level.WARNING, "If you are using Factions, it "+
					"is recommended you disable it or use version 1.6.9.4.");
		}
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public boolean isEVerbose() {
		return everbose;
	}
	
	public boolean isMChatEnabled() {
		return isMchatEnabled;
	}
	
	public boolean isVaultEnabled() {
		return isVaultEnabled;
	}
	
	public boolean isFactionsEnabled() {
		return isFactionsEnabled;
	}
	
	public Chat getVaultChat() {
		return chat;
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
