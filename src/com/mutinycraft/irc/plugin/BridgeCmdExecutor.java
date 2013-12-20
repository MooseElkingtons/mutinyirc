package com.mutinycraft.irc.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.*;

import com.mutinycraft.irc.*;

public class BridgeCmdExecutor implements CommandExecutor {

	private IRC irc;
	
	private String cmdVoice = "", cmdDevoice = "",
				cmdKick = "", cmdBan = "";
	
	public BridgeCmdExecutor(IRC irc, Plugin plugin) {
		this.irc = irc;
		
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
			case "kick":
				return kick(sender, args);
			case "ban":
				return ban(sender, args);
			default:
				return false;
		}
	}

	public boolean voice(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.voice")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(
						ChatColor.GOLD+"Could not find "+args[0]+" in IRC.");
				return true;
			}
			for(String c : irc.getChannels())
				irc.sendRaw(cmdVoice.replace("%channel%", c)
									.replace("%nick%", args[0]));
			sender.sendMessage(ChatColor.GREEN+"Voiced "+args[0]+".");
			return true;
		}
		return false;
	}
	
	public boolean devoice(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.devoice")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(
						ChatColor.GOLD+"Could not find "+args[0]+" in IRC.");
				return true;
			}
			for(String c : irc.getChannels())
				irc.sendRaw(cmdDevoice.replace("%channel%", c)
									.replace("%nick%", args[0]));
			sender.sendMessage(ChatColor.GREEN+"Devoiced "+args[0]+".");
			return true;
		}
		return false;
	}
	
	public boolean kick(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.kick")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(
						ChatColor.GOLD+"Could not find "+args[0]+" in IRC.");
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
			sender.sendMessage(ChatColor.GREEN+"Kicked "+args[0]+" from IRC.");
			return true;
		}
		return false;
	}

	public boolean ban(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.ban")) {
			if(args.length < 1)
				return false;
			if(!irc.isUserVisible(args[0])) {
				sender.sendMessage(
						ChatColor.GOLD+"Could not find "+args[0]+" in IRC.");
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
			sender.sendMessage(ChatColor.GREEN+"Banned "+nick+" from IRC.");
			return true;
		}
		return false;
	}

}
