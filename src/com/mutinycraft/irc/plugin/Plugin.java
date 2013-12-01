package com.mutinycraft.irc.plugin;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import org.bukkit.plugin.java.*;

import com.mutinycraft.irc.IRC;
import com.mutinycraft.irc.IRCListener;

public class Plugin extends JavaPlugin {
	
	private Socket socket;
	private BufferedWriter out;
	private BufferedReader in;
	
	private int queueInterval = 750;

	private IRC irc;
	
	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Initializing MutinyIRC.");
		irc = new IRC(this);
		getLogger().log(Level.INFO, "Loading configuration.");
		loadConfig();
		getLogger().log(Level.INFO, "Starting IRC connection.");
		try {
			socket = new Socket("irc.esper.net", 6667);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
		} catch(IOException e) {
			getLogger().log(Level.SEVERE, "Error initiating IRC connection", e);
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(socket.isConnected()) {
					try {
						String i;
						while((i = in.readLine()) != null) {
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
								String newnick = split[2];
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
								for(IRCListener l : irc.getIRCListeners())
									l.onMessage(sender, recipient, message);
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
					} catch(IOException e) {
						getLogger().log(Level.SEVERE, null, e);
					}
				}
			}
			
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(IRCListener l : irc.getIRCListeners())
					l.onConnect();
				while(socket.isConnected()) {
					try {
						String o = irc.queue.poll();
						if(o != null && !o.isEmpty()) {
							out.write(o+"\r\n");
							out.flush();
						}
						Thread.sleep(queueInterval);
					} catch(Exception e) {
						getLogger().log(Level.SEVERE, null, e);
					}
				}
				for(IRCListener l : irc.getIRCListeners())
					l.onDisconnect();
			}
		}).start();
		getLogger().log(Level.INFO, "MutinyIRC Plugin Enabled.");
	}
	
	private void loadConfig() {
		saveDefaultConfig();
		irc.setNick(getConfig().getString("config.nick"));
		irc.setPass(getConfig().getString("config.pass"));
		queueInterval = getConfig().getInt("config.message_interval");
	}
}
