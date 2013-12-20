package com.mutinycraft.irc.io;

import java.io.*;
import java.net.*;
import java.util.logging.Level;

import com.mutinycraft.irc.*;
import com.mutinycraft.irc.plugin.*;

/**
 * Handles outgoing data to IRC.
 * 
 * @author MooseElkingtons
 */
public class IRCOutputThread implements Runnable {

	private Plugin plugin;
	private BufferedWriter out;
	private IRC irc;
	private int queueInterval = 750;
	
	public IRCOutputThread(Plugin plugin, Socket socket, IRC irc) {
		this.plugin = plugin;
		try {
			this.out = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
		} catch(IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error getting Output"
					+ "Stream from IRC socket.", e);
		}
		this.irc = irc;
		queueInterval = plugin.getConfig().getInt("config.message_interval");
	}
	
	@Override
	public void run() {
		while(irc.isConnected()) {
			try {
				String o = irc.queue.poll();
				if(o != null && !o.isEmpty()) {
					if(plugin.isEVerbose())
						plugin.getLogger().log(Level.INFO, ">>> "+o);
					out.write(o+"\r\n");
					out.flush();
					Thread.sleep(queueInterval);
				}
			} catch(Exception e) {
				plugin.getLogger().log(Level.SEVERE, null, e);
			}
		}
	}
}
