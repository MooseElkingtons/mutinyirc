package com.mutinycraft.irc;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import com.mutinycraft.irc.io.*;
import com.mutinycraft.irc.IRCUser.*;
import com.mutinycraft.irc.plugin.*;

/**
 * RFC-2812 compliant IRC bridge.
 * 
 * @author MooseElkingtons
 */

public class IRC {
	
	public ConcurrentLinkedQueue<String> queue =
			new ConcurrentLinkedQueue<String>();
	
	private Plugin plugin;
	private List<IRCListener> listeners = new ArrayList<IRCListener>();
	private HashMap<String, IRCUser> bufferNam = new HashMap<String, IRCUser>();
	private Socket socket = null;
	private Thread input = null, output = null;
	
	private String nick = "MutinyIRC";
	private String pass = null;
	private String server = "";
	private int port = 6667;
	private int triedNicks = 0;
	private HashMap<String, List<IRCUser>> channels = new HashMap<String,
			List<IRCUser>>();
	
	public IRC(Plugin plugin) {
		this.plugin = plugin;
		this.registerIRCListener(new ControlListener(this, plugin));
		IRCCommandListener gl = new IRCCommandListener(this, plugin);
		this.registerIRCListener(gl);
		plugin.getServer().getPluginManager().registerEvents(gl, plugin);
	}
	
	/**
	 * Joins an IRC channel.
	 * 
	 * @param channel The channel to join.
	 */
	public void joinChannel(String channel) {
		if(!channel.startsWith("#") &&
			!channel.startsWith("&") &&
			!channel.startsWith("+") &&
			!channel.startsWith("!"))
			channel = "#" + channel;
		sendRaw("JOIN "+channel);
	}
	
	/**
	 * Queues an action to be sent to IRC.
	 * 
	 * @param recipient the receiver of the action.
	 * @param action The action to send.
	 */
	public void sendAction(String recipient, String action) {
		sendCTCP(recipient, "ACTION "+action);
	}
	
	/**
	 * Sends a notice to the recipient. Used for CTCP responses.
	 * 
	 * @param recipient The receiver of the notice.
	 * @param message The notice to send.
	 */
	public void sendNotice(String recipient, String message) {
		sendRaw("NOTICE "+recipient+" :"+message);
	}
	
	/**
	 * Queues a message to be sent to IRC.
	 * 
	 * @param recipient The receiver of the message.
	 * @param message The message to send.
	 */
	public void sendMessage(String recipient, String message) {
		String preCmd = "PRIVMSG "+recipient+" :";
		int clen = 420 - preCmd.length() - recipient.length();
		List<String> msgs = new ArrayList<String>();
		for(int i = 0; i < message.length(); i+=clen)
			msgs.add(message
					.substring(i, Math.min(message.length(), i + clen)));
		for(String s : msgs)
			sendRaw(preCmd+s);
	}
	
	/**
	 * Sends a raw line to IRC. Only recommended for advanced usage.
	 * 
	 * @param rawLine The raw line to send to IRC.
	 */
	public void sendRaw(String rawLine) {
		if(!isConnected())
			return;
		queue.add(rawLine);
	}
	
	/**
	 * Sends Client-to-Client Protocol (CTCP) query to specified recipient.
	 * 
	 * @param recipient The recipient of the CTCP query
	 * @param ctcp The CTCP message
	 */
	public void sendCTCP(String recipient, String ctcp) {
		sendRaw("PRIVMSG "+recipient+" :\u000001"+ctcp+"\u000001");
	}
	
	/**
	 * Disconnect/quits from IRC.
	 */
	public void disconnect() {
		if(!isConnected())
			return;
		try {
			socket.close();
			socket = null;
			input = null;
			output = null;
			for(IRCListener l : listeners)
				l.onDisconnect();
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Encountered an error while "
					+ "disconnecting from IRC.", e);
		}
	}
	
	/**
	 * 
	 * @return whether or not the IRC bridge is connected to any IRC server.
	 */
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	/**
	 * Connects the IRC bridge to specified socket.
	 * 
	 * @param socket the socket for the IRC bridge to connect to.
	 */
	public void connect(Socket socket) {
		if(isConnected())
			return;
		this.socket = socket;
		server = socket.getInetAddress().getHostAddress();
		port = socket.getPort();
		input = new Thread(new IRCInputThread(plugin, socket, this));
		output = new Thread(new IRCOutputThread(plugin, socket, this));
		input.start();
		output.start();
		for(IRCListener l : listeners)
			l.onConnect();
	}
	
	public List<String> getChannels() {
		return new ArrayList<String>(channels.keySet());
	}
	
	public CMode getUserCMode(String channel, String nick) {
		for(IRCUser u : channels.get(channel)) {
			if(u.getNick().equalsIgnoreCase(nick))
				return u.getMode();
		}
		return CMode.CMODE_NORMAL;
	}
	
	/**
	 * Obtains a List containing all of the users in the channel specified. 
	 * 
	 * @return the user list
	 */
	public List<IRCUser> getUsers(String channel) {
		return channels.get(channel);
	}

	/**
	 * Sets the IRC bridge's nickname.
	 * 
	 * @param nick
	 */
	public void setNick(String nick) {
		if(isConnected())
			queue.add("NICK "+nick);
		this.nick = nick;
	}
	
	/**
	 * Gets the name of the IRC Bridge.
	 * 
	 * @return IRC Bridge Nick used.
	 */
	public String getNick() {
		return nick;
	}
	
	/**
	 * Gets the server to connect to.
	 * 
	 * @return the server to connect to.
	 */
	public String getServer() {
		return server;
	}
	
	/**
	 * The port for the server.
	 * 
	 * @return the port.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the server to connect to. Will not modify present connections.
	 * 
	 * @param server The server to connect to.
	 */
	public void setServer(String server) {
		this.server = server;
	}
	
	/**
	 * Sets the server port. Default 6667.
	 * 
	 * @param port The port.
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Sets IRC Server Pass to be used on connection.
	 * 
	 * @param pass the pass to use.
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}
	
	/**
	 * Registers an IRCListener to the IRC bridge.
	 */
	public void registerIRCListener(IRCListener handler) {
		listeners.add(handler);
	}
	
	public List<IRCListener> getIRCListeners() {
		return listeners;
	}
	
	
	class ControlListener extends IRCListener {

		public ControlListener(IRC irc, Plugin plugin) {
			super(irc, plugin);
		}
		
		@Override
		public void onConnect() {
			String message = "";
			if(pass != null && !pass.isEmpty())
				message += "PASS "+pass+"\r\n";
			message += "NICK "+nick+"\r\nUSER "+nick+" 0 * :MutinyIRC";
			sendRaw(message);
		}
		
		@Override
		public void onPart(String channel, String user) {
			sendRaw("NAMES "+channel);
		}
		
		@Override
		public void onJoin(String channel, String user) {
			if(!channels.containsKey(channel.toLowerCase()))
				channels.put(channel.toLowerCase(), new ArrayList<IRCUser>());
			sendRaw("NAMES "+channel);
		}
		
		@Override
		public void onModeChanged(String channel, String user, String modes) {
			if(channel.startsWith("#") ||
					channel.startsWith("&") ||
					channel.startsWith("+") ||
					channel.startsWith("!"))
				sendRaw("NAMES "+channel);
		}
		
		@Override
		public void onKick(String channel, String user, String kicker) {
			onPart(channel, user);
		}
		
		@Override
		public void onPing(String response) {
			sendRaw("PONG "+response);
		}
		
		@Override
		public void onNick(String oldNick, String newNick) {
			if(oldNick.equalsIgnoreCase(nick))
				nick = newNick;
			for(String c : channels.keySet()) {
				for(IRCUser u : channels.get(c)) {
					if(u.getNick().equalsIgnoreCase(oldNick))
						u.setNick(newNick);
				}
			}
		}
		
		@Override
		public void onCTCP(String sender, String ctcp) {
			String cmd = ctcp.split(" ")[0].toUpperCase();
			switch(cmd) {
				case "VERSION":
					getIRC().sendNotice(sender, "VERSION MutinyIRC "
							+ "Bridge Plugin by MutinyCraft - http://github."
							+ "com/MooseElkingtons/MutinyIRC (Developed by M"
							+ "ooseElkingtons)");
					break;
					
				case "PING":
					String[] crps = ctcp.split(" ");
					String pres = "PING";
					if(crps.length > 1)
						pres += " "+crps[1];
					getIRC().sendNotice(sender, pres);
				break;
			}
		}

		@Override
		public void onServerResponse(int code, String response) {
			String[] res = response.split(" ");
			switch(code) {
				case ReplyConstants.RPL_ENDOFMOTD:
					String chanlist = "";
					for(String chan : plugin.getConfig()
							.getStringList("config.channels"))
						chanlist += chan + ",";
					sendRaw("JOIN "+chanlist.substring(0,
							chanlist.lastIndexOf(',')));
					break;
			
				case ReplyConstants.RPL_NAMREPLY:
					String[] xres = response.split(":")[1].split(" ");
					for(String xrs : xres) {
						String nick = xrs.substring(1);
						char xc = xrs.charAt(0);
						CMode mode = CMode.CMODE_NORMAL;
						switch(xc) {
							case '~':
								mode = CMode.CMODE_OWNER;
								break;
							case '&':
								mode = CMode.CMODE_ADMIN;
								break;
							case '@':
								mode = CMode.CMODE_OP;
								break;
							case '%':
								mode = CMode.CMODE_HOP;
								break;
							case '+':
								mode = CMode.CMODE_VOICE;
								break;
							default:
								mode = CMode.CMODE_NORMAL;
								nick = xrs;
								break;
						}
						if(!bufferNam.containsKey(nick.toLowerCase()))
							bufferNam.put(nick.toLowerCase(),
									new IRCUser(nick, mode, res[2]));
					}
					break;
				
				case ReplyConstants.RPL_ENDOFNAMES:
					String channel = res[0].toLowerCase();
					channels.remove(channel);
					channels.put(channel, new ArrayList<IRCUser>(bufferNam.values()));
					bufferNam.clear();
					break;
				
				case ReplyConstants.ERR_ERRONEOUSNICKNAME:
				case ReplyConstants.ERR_NICKCOLLISION:
				case ReplyConstants.ERR_NONICKNAMEGIVEN:
				case ReplyConstants.ERR_NICKNAMEINUSE:
					plugin.getLogger().log(Level.WARNING, response);
					if(triedNicks > 4) {
						plugin.getLogger().log(Level.SEVERE, "Tried changing"
								+ "nick more than 4 times! Quitting IRC.");
						sendRaw("QUIT");
					}
					setNick(nick+"-");
					triedNicks++;
					break;
				
				case ReplyConstants.ERR_ALREADYREGISTERED:
					plugin.getLogger().log(Level.SEVERE, "ERR_ALREADYREGISTER"
											+ "ED: "+response);
					break;
				
				case ReplyConstants.ERR_BADCHANNELKEY:
					plugin.getLogger().log(Level.SEVERE, "ERR_BADCHANNELKEY: "+
										response);
					break;
				
				case ReplyConstants.ERR_INVITEONLYCHAN:
					plugin.getLogger().log(Level.SEVERE, "ERR_INVITEONLYCHAN:"+
											" "+response);
					break;
				
			}
		}
	}
}
