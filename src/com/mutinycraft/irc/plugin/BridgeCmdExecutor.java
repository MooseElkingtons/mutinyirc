package com.mutinycraft.irc.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.*;

import com.mutinycraft.irc.*;

public class BridgeCmdExecutor implements CommandExecutor {

	private IRC irc;
	private Plugin plugin;
	
	private String cmdVoice = "", cmdDevoice = "",
				cmdKick = "", cmdBan = "";
	private String[] reloadhelp =
		{ChatColor.GOLD+"Possible arguments for reloading IRC:",
		ChatColor.WHITE+"config"+ChatColor.GOLD+" - Reloads configuration.",
		ChatColor.WHITE+"irc"+ChatColor.GOLD+" - Reloads IRC connection.",
		ChatColor.WHITE+"all"+ChatColor.GOLD+" - Reloads everything."};
	
	public BridgeCmdExecutor(IRC irc, Plugin plugin) {
		this.irc = irc;
		this.plugin = plugin;
		loadConfig();
	}
	
	public void loadConfig() {
		cmdVoice = plugin.getConfig()
				.getString("advanced.commands.command_voice");
		cmdDevoice = plugin.getConfig()
				.getString("advanced.commands.command_devoice");
		cmdKick = plugin.getConfig()
				.getString("advanced.commands.command_kick");
		cmdBan = plugin.getConfig()
				.getString("advanced.commands.command_ban");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		switch(label.toLowerCase()) {
			case "voice":
				return voice(sender, args);
			case "devoice":
				return devoice(sender, args);
			case "irckick":
				return kick(sender, args);
			case "ircban":
				return ban(sender, args);
			case "ircreload":
				return reload(sender, args);
			default:
				return false;
		}
	}

	public boolean voice(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.voice")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(ChatColor.RED+"Could not find user "+
						ChatColor.DARK_AQUA+args[0]);
				return true;
			}
			for(String c : irc.getChannels())
				irc.sendRaw(cmdVoice.replace("%channel%", c)
									.replace("%nick%", args[0]));
			return true;
		}
		return false;
	}
	
	public boolean devoice(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.devoice")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(ChatColor.RED+"Could not find user "+
						ChatColor.DARK_AQUA+args[0]);
				return true;
			}
			for(String c : irc.getChannels())
				irc.sendRaw(cmdDevoice.replace("%channel%", c)
									.replace("%nick%", args[0]));
			return true;
		}
		return false;
	}
	
	public boolean kick(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.kick")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(ChatColor.RED+"Could not find user "+
						ChatColor.DARK_AQUA+args[0]);
				return true;
			}
			String reason = "("+sender.getName()+")";
			if(args.length > 1) {
				for(int i = 1; i < args.length; i++)
					reason += " "+args[i];
			}

			for(String c : irc.getChannels())
				irc.sendRaw(cmdKick.replace("%channel%", c)
									.replace("%nick%", args[0])
									.replace("%reason%", reason));
			return true;
		}
		return false;
	}

	public boolean ban(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.ban")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(ChatColor.RED+"Could not find user "+
						ChatColor.DARK_AQUA+args[0]);
				return true;
			}
			String reason = "("+sender.getName()+")";
			if(args.length > 1) {
				for(int i = 1; i < args.length; i++)
					reason += " "+args[i];
			}
			
			String nick = args[0];
			for(String c : irc.getChannels())
				irc.sendRaw(cmdBan.replace("%channel%", c)
									.replace("%nick%", nick)
									.replace("%reason%", reason)
									.replace("%host%", irc.getUserHost(nick))
									.replace("%login%", irc.getUserLogin(nick))
									);
			return true;
		}
		return false;
	}

	public boolean reload(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.reload")) {
			if(args.length < 1) {
				sender.sendMessage(reloadhelp);
				return true;
			}
			switch(args[0].toLowerCase()) {
				case "config":
					plugin.reloadConfig();
					plugin.loadConfig();
					irc.loadConfig();
					irc.getCommandListener().setCommandPrefix(
							plugin.getConfig().getString(
									"config.command_prefix"));
					sender.sendMessage(
							ChatColor.GREEN+"Reloaded MutinyIRC config.");
					break;
				
				case "irc":
					irc.reconnect();
					break;
				
				case "all":
					irc.disconnect();
					plugin.reloadConfig();
					plugin.loadConfig();
					irc.loadConfig();
					irc.getCommandListener().setCommandPrefix(
							plugin.getConfig().getString(
									"config.command_prefix"));
					irc.reconnect();
					sender.sendMessage(
							ChatColor.GREEN+"Reloaded MutinyIRC.");
					break;
				
				default:
					sender.sendMessage(reloadhelp);
					break;
			}
			return true;
		}
		return false;
	}
}