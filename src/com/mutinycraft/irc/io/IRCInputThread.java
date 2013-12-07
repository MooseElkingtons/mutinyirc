package com.mutinycraft.irc.io;

import java.io.*;
import java.net.*;
import java.util.logging.Level;

import com.mutinycraft.irc.*;
import com.mutinycraft.irc.plugin.*;

/**
 * Handles Incoming data from IRC.
 * 
 * @author MooseElkingtons
 */

public class IRCInputThread implements Runnable {

	private Plugin plugin;
	private BufferedReader in;
	private IRC irc;
	
	public IRCInputThread(Plugin plugin, Socket socket, IRC irc) {
		this.plugin = plugin;
		try {
			this.in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
		} catch(IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error getting Input"
					+ "Stream from IRC socket.", e);
		}
		this.irc = irc;
	}
	
	@Override
	public void run() {
		try {
			String i;
			while(irc.isConnected() && (i = in.readLine()) != null) {
				String[] split = i.split(" ");
				String response = i.substring(i.indexOf(" ", 2) + 1);
				if(split[1].matches("\\d+"))
					for(IRCListener l : irc.getIRCListeners())
						l.onServerResponse(Integer.parseInt(split[1]),
								response);

				if(split[0].equalsIgnoreCase("PING"))
					for(IRCListener l : irc.getIRCListeners())
						l.onPing(response);
				
				if(split[1].equalsIgnoreCase("NICK")) {
					String oldnick = i.substring(1, i.indexOf("!"));
					String newnick = split[2].substring(1);
					for(IRCListener l : irc.getIRCListeners())
						l.onNick(oldnick, newnick);
				}
				
				if(split[1].equalsIgnoreCase("QUIT")) {
					String user = i.substring(1, i.indexOf("!"));
					String reason = i.substring(i.indexOf(':', 2) + 1);
					for(IRCListener l : irc.getIRCListeners())
						l.onQuit(user, reason);
				}
				
				if(split[1].equalsIgnoreCase("JOIN")) {
					String user = i.substring(1, i.indexOf("!"));
					for(IRCListener l : irc.getIRCListeners())
						l.onJoin(split[2], user);
				}
				
				if(split[1].equalsIgnoreCase("KICK")) {
					String kicker = i.substring(1, i.indexOf("!"));
					for(IRCListener l : irc.getIRCListeners())
						l.onKick(split[2], split[3], kicker);
				}
				
				if(split[1].equalsIgnoreCase("PART")) {
					String user = i.substring(1, i.indexOf("!"));
					for(IRCListener l : irc.getIRCListeners())
						l.onPart(user, split[2]);
				}
				
				if(split[1].equalsIgnoreCase("PRIVMSG")) {
					String sender = i.substring(1, i.indexOf("!"));
					String recipient = split[2];
					String message = i.substring(i.indexOf(':', 2) + 1);
					if(recipient.startsWith("#") ||
							recipient.startsWith("&") ||
							recipient.startsWith("+") ||
							recipient.startsWith("!")) {
						if(message.startsWith("\u0001")) {
							String ctcp = message.substring(1,
									message.lastIndexOf("\u0001"));
							switch(ctcp.split(" ")[0].toUpperCase()) {
								case "ACTION":
									for(IRCListener l : irc.getIRCListeners())
										l.onAction(sender, recipient, ctcp
												.substring(
													ctcp.indexOf(" ") + 1));
									break;
									
								default:
									for(IRCListener l : irc.getIRCListeners())
										l.onCTCP(sender, ctcp);
							}
						} else
							for(IRCListener l : irc.getIRCListeners())
								l.onMessage(sender, recipient, message);
					}
				}
				
				if(split[1].equalsIgnoreCase("MODE")) {
					String user = "";
					if(split[0].contains("!"))
						user = i.substring(1, i.indexOf("!"));
					String channel = split[2];
					String modes = i.substring(i.indexOf(" ", 2) + 1);
					for(IRCListener l : irc.getIRCListeners())
						l.onModeChanged(channel, user, modes);
				}
				
				if(split[1].equalsIgnoreCase("NOTICE")) {
					String sender = split[0].substring(1);
					if(split[0].contains("!"))
						sender = i.substring(1, i.indexOf("!"));
					String recipient = split[2];
					String notice = i.substring(i.indexOf(':', 2) + 1);
					for(IRCListener l : irc.getIRCListeners())
						l.onNotice(sender, recipient, notice);
				}
			}
		} catch(SocketException ex) { } catch(IOException e) {
			plugin.getLogger().log(Level.SEVERE, null, e);
		}
	}

}
