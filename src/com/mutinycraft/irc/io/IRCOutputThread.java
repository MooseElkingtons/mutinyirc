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
	
	public IRCOutputThread(Plugin plugin, Socket socket, IRC irc,
			int interval) {
		this.plugin = plugin;
		try {
			this.out = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
		} catch(IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error getting Output"
					+ "Stream from IRC socket.", e);
		}
		this.irc = irc;
		queueInterval = interval;
	}
	
	@Override
	public void run() {
		try {
			while(irc.isQueueBlocked()) {
				// You could probably replace this by notifications as well,
				// but it's not causing much trouble as it stands.
				Thread.sleep(1000);
			}
			while(irc.isConnected()) {
				// assume irc's queue is still thread-safe
				final String o = irc.queue.poll();
				if(o == null)
					synchronized(irc.queueSignal) {
						irc.queueSignal.wait();
						continue;
					}
				if(!o.isEmpty()) {
					if(plugin.isEVerbose())
						plugin.getLogger().log(Level.INFO, ">>> "+o);
					out.write(o+"\r\n");
					if(!irc.getSocket().isClosed())
						out.flush();
					Thread.sleep(queueInterval);
				}
			}
		} catch(final InterruptedException ix) {
			// respect interruption request: Exit thread
			Thread.currentThread().interrupt();
		} catch(final IOException iox) {
			// I don't believe there's much we can try after receiving an
			// IOException, let's just exit.
			plugin.getLogger().log(Level.SEVERE, "{0}", iox);
		}
		// I don't know what the intention behind catch-alls was, but I
		// omitted them since the checked exceptions should cover all
		// reasonable cases.
		finally
		{
			irc.outEnded();
		}
	}
}

