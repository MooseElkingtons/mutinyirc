package com.mutinycraft.irc;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.miraclem4n.mchat.api.Reader;
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
	
	private HashMap<String, String>
		ircMsgs = new HashMap<String, String>(),
		gameMsgs = new HashMap<String, String>();
	private HashMap<String, Boolean>
		ircRelays = new HashMap<String, Boolean>(),
		gameRelays = new HashMap<String, Boolean>();
	private String cmdPrefix = ".", ircPrefix = "",
					gamePrefix = "", nameFormat = "";
	private boolean isMchatEnabled;

	
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
	private List<String> loadship = new ArrayList<String>();
	
	public IRC(Plugin plugin) {
		this.plugin = plugin;
		loadStartupCommands();
		loadConfig();
		this.registerIRCListener(new ControlListener(this, plugin));
		IRCCommandListener gl = new IRCCommandListener(this, plugin);
		this.registerIRCListener(gl);
		plugin.getServer().getPluginManager().registerEvents(gl, plugin);

	}
	
	public void loadStartupCommands() {
		loadship.clear();
		FileConfiguration cfg = plugin.getConfig();
		if(cfg.contains("config.startup_commands"))
			loadship.addAll(cfg.getStringList("config.startup_commands"));
	}
	
	public void loadConfig() {
		gameMsgs.clear();
		ircMsgs.clear();
		gameRelays.clear();
		ircRelays.clear();
		FileConfiguration cfg = plugin.getConfig();
		String imsg = "irc_to_game.messages.";
		gameMsgs.put("join",
				ChatUtil.ircToGameColors(ChatUtil.correctCC(
						cfg.getString(imsg+"join"))));
		gameMsgs.put("part",
				ChatUtil.ircToGameColors(ChatUtil.correctCC(
						cfg.getString(imsg+"part"))));
		gameMsgs.put("kick",
				ChatUtil.ircToGameColors(ChatUtil.correctCC(
						cfg.getString(imsg+"kick"))));
		gameMsgs.put("msg",
				ChatUtil.ircToGameColors(ChatUtil.correctCC(
						cfg.getString(imsg+"msg"))));
		gameMsgs.put("nick",
				ChatUtil.ircToGameColors(ChatUtil.correctCC(
						cfg.getString(imsg+"nick"))));
		gameMsgs.put("me",
				ChatUtil.ircToGameColors(ChatUtil.correctCC(
						cfg.getString(imsg+"me"))));
		
		String gmsg = "game_to_irc.messages.";
		ircMsgs.put("join", ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString(gmsg+"join"))));
		ircMsgs.put("part", ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString(gmsg+"part"))));
		ircMsgs.put("kick", ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString(gmsg+"kick"))));
		ircMsgs.put("msg", ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString(gmsg+"msg"))));
		ircMsgs.put("me", ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString(gmsg+"me"))));

		String irel = "irc_to_game.relay.";
		gameRelays.put("join", cfg.getBoolean(irel+"join"));
		gameRelays.put("part", cfg.getBoolean(irel+"part"));
		gameRelays.put("kick", cfg.getBoolean(irel+"kick"));
		gameRelays.put("msg", cfg.getBoolean(irel+"msg"));
		gameRelays.put("nick", cfg.getBoolean(irel+"nick"));
		gameRelays.put("me", cfg.getBoolean(irel+"me"));
		gameRelays.put("color", cfg.getBoolean(irel+"color"));
		
		String grel = "game_to_irc.relay.";
		ircRelays.put("join", cfg.getBoolean(grel+"join"));
		ircRelays.put("part", cfg.getBoolean(grel+"part"));
		ircRelays.put("kick", cfg.getBoolean(grel+"kick"));
		ircRelays.put("msg", cfg.getBoolean(grel+"msg"));
		ircRelays.put("me", cfg.getBoolean(grel+"me"));
		ircRelays.put("color", cfg.getBoolean(grel+"color"));
		
		cmdPrefix = cfg.getString("config.command_prefix");
		ircPrefix = ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString("game_to_irc.prefix")));
		gamePrefix = ChatUtil.correctCC(cfg.getString("irc_to_game.prefix"));
		nameFormat = ChatUtil.gameToIrcColors(
				ChatUtil.correctCC(cfg.getString("game_to_irc.name_format")));
		
		isMchatEnabled = plugin.getServer().getPluginManager()
				.isPluginEnabled("MChat");
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
	public void registerIRCListener(IRCListener listener) {
		listeners.add(listener);
	}
	
	public List<IRCListener> getIRCListeners() {
		return listeners;
	}
	
	/**
	 * Sends Game message to IRC.
	 * 
	 * @param message the string to send to IRC.
	 */
	public void sendIrcMessage(String message) {
		String m = ircPrefix + 
			(ircRelays.get("color") ? ChatUtil.gameToIrcColors(message)
			: ChatUtil.stripGameColors(message));
		for(String channel : channels.keySet())
			sendMessage(channel, m);
	}
	
	/**
	 * Sends IRC message to game.
	 * 
	 * @param message the string to send to game.
	 */
	public void sendGameMessage(String message) {
		String m = gamePrefix + (
				gameRelays.get("color") ? ChatUtil.ircToGameColors(message)
				: ChatUtil.stripIrcColors(message));
		for(Player p : plugin.getServer().getOnlinePlayers())
			p.sendMessage(m);
		if(plugin.isVerbose())
			plugin.getServer().getConsoleSender().sendMessage(m);
	}
	
	public String getIrcMessage(String key) {
		return ircMsgs.get(key);
	}
	
	public String getGameMessage(String key) {
		return gameMsgs.get(key);
	}
	
	public boolean getIrcRelay(String key) {
		return ircRelays.get(key);
	}
	
	public boolean getGameRelay(String key) {
		return gameRelays.get(key);
	}
	
	public String getCommandPrefix() {
		return cmdPrefix;
	}
	
	public String getIrcMsgPrefix() {
		return ircPrefix;
	}
	
	public String getGameMsgPrefix() {
		return gamePrefix;
	}
	
	public boolean isMchatEnabled() {
		return isMchatEnabled;
	}
	
	public String formatPlayerName(Player player, String type) {
		String name = player.getName();
		World world = player.getWorld();
		
		String fname = getIrcMessage(type)
				.replace("%nf%", nameFormat)
				.replace("%name%", name)
				.replace("%dname%", player.getDisplayName())
				.replace("%world%", world.getName());
		if(isMchatEnabled) {
			String group = Reader.getGroup(name, world.getName());
			fname = fname
				.replace("%mname%", Reader.getMName(name))
				.replace("%mgroup%", group)
				.replace("%mgname%", Reader.getGroupName(group));
		}
		return ChatUtil.correctCC(fname);
	}
	
	public String toGameColor(String message) {
		return gameRelays.get("color") ? ChatUtil.ircToGameColors(message) :
				ChatUtil.stripIrcColors(message);
	}
	
	public String toIrcColor(String message) {
		return ircRelays.get("color") ? ChatUtil.gameToIrcColors(message) :
				ChatUtil.stripGameColors(message);
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
					
				case ReplyConstants.RPL_WELCOME:
					for(String scm : loadship)
						sendRaw(scm);
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
											+ "ED "+response);
					break;
				
				case ReplyConstants.ERR_BADCHANNELKEY:
					plugin.getLogger().log(Level.SEVERE, "ERR_BADCHANNELKEY "+
										response);
					break;
				
				case ReplyConstants.ERR_INVITEONLYCHAN:
					plugin.getLogger().log(Level.SEVERE, "ERR_INVITEONLYCHAN "
										+ response);
					break;
				
			}
		}
	}
}
