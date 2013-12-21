package com.mutinycraft.irc.plugin;

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
			case "irckick":
				return kick(sender, args);
			case "ircban":
				return ban(sender, args);
			default:
				return false;
		}
	}

	public boolean voice(CommandSender sender, String[] args) {
		if(sender.hasPermission("mutinyirc.cmd.voice")) {
			if(args.length < 1)
				return false;
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

}
