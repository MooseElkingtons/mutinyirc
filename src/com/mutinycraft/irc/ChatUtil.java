package com.mutinycraft.irc;

/**
 * A chat utility which manipulates IRC/game color.
 * 
 * @author MooseElkingtons
 */
public class ChatUtil {
	
	public static final String IRC_COLOR_WHITE  = "\u000300";
	public static final String IRC_COLOR_BLACK  = "\u000301";
	public static final String IRC_COLOR_BLUE   = "\u000302";
	public static final String IRC_COLOR_DGREEN = "\u000303";
	public static final String IRC_COLOR_RED    = "\u000304";
	public static final String IRC_COLOR_MAROON = "\u000305";
	public static final String IRC_COLOR_PURPLE = "\u000306";
	public static final String IRC_COLOR_GOLD   = "\u000307";
	public static final String IRC_COLOR_YELLOW = "\u000308";
	public static final String IRC_COLOR_GREEN  = "\u000309";
	public static final String IRC_COLOR_DAQUA  = "\u000310";
	public static final String IRC_COLOR_AQUA   = "\u000311";
	public static final String IRC_COLOR_DBLUE  = "\u000312";
	public static final String IRC_COLOR_PINK   = "\u000313";
	public static final String IRC_COLOR_DGRAY  = "\u000314";
	public static final String IRC_COLOR_GRAY   = "\u000315";
	
	public static final String GAME_COLOR_WHITE  = "§f";
	public static final String GAME_COLOR_BLACK  = "§0";
	public static final String GAME_COLOR_BLUE   = "§9";
	public static final String GAME_COLOR_DGREEN = "§2";
	public static final String GAME_COLOR_RED    = "§c";
	public static final String GAME_COLOR_MAROON = "§4";
	public static final String GAME_COLOR_PURPLE = "§5";
	public static final String GAME_COLOR_GOLD   = "§6";
	public static final String GAME_COLOR_YELLOW = "§e";
	public static final String GAME_COLOR_GREEN  = "§a";
	public static final String GAME_COLOR_DAQUA  = "§3";
	public static final String GAME_COLOR_AQUA   = "§b";
	public static final String GAME_COLOR_DBLUE  = "§1";
	public static final String GAME_COLOR_PINK   = "§d";
	public static final String GAME_COLOR_DGRAY  = "§8";
	public static final String GAME_COLOR_GRAY   = "§7";
	
	public static final String GAME_BOLD = "§l";
	public static final String GAME_UNDERLINE = "§n";
	public static final String GAME_STRIKETHROUGH = "§m";
	public static final String GAME_ITALIC = "§o";
	public static final String GAME_RESET = "§r";
	public static final String GAME_OBFUSCATED = "§k";
	
	public static final String IRC_BOLD = "\u0002";
	public static final String IRC_UNDERLINE = "\u001F";
	public static final String IRC_RESET = "\u000F";
	
	public static String ircToGameColors(String message) {
		return message
					.replaceAll("\u0003(?=[0-9])[(?=\\w)]", "\u0003")
					.replaceAll("[\u0003]\\d+\\,[\\d+]", "")
					.replaceAll("\u0003(?=\\w)", GAME_RESET)
					.replace(IRC_COLOR_WHITE, GAME_COLOR_WHITE)
					.replace(IRC_COLOR_BLACK, GAME_COLOR_BLACK)
					.replace(IRC_COLOR_BLUE, GAME_COLOR_BLUE)
					.replace(IRC_COLOR_DGREEN, GAME_COLOR_DGREEN)
					.replace(IRC_COLOR_RED, GAME_COLOR_RED)
					.replace(IRC_COLOR_MAROON, GAME_COLOR_MAROON)
					.replace(IRC_COLOR_PURPLE, GAME_COLOR_PURPLE)
					.replace(IRC_COLOR_GOLD, GAME_COLOR_GOLD)
					.replace(IRC_COLOR_YELLOW, GAME_COLOR_YELLOW)
					.replace(IRC_COLOR_GREEN, GAME_COLOR_GREEN)
					.replace(IRC_COLOR_DAQUA, GAME_COLOR_DAQUA)
					.replace(IRC_COLOR_AQUA, GAME_COLOR_AQUA)
					.replace(IRC_COLOR_DBLUE, GAME_COLOR_DBLUE)
					.replace(IRC_COLOR_PINK, GAME_COLOR_PINK)
					.replace(IRC_COLOR_DGRAY, GAME_COLOR_DGRAY)
					.replace(IRC_COLOR_GRAY, GAME_COLOR_GRAY)
					.replace(IRC_BOLD, GAME_BOLD)
					.replace(IRC_UNDERLINE, GAME_UNDERLINE)
					.replace(IRC_RESET, GAME_RESET);
	}

	public static String gameToIrcColors(String message) {
		return message.replace(GAME_COLOR_WHITE, IRC_COLOR_WHITE)
					.replace(GAME_COLOR_BLACK, IRC_COLOR_BLACK)
					.replace(GAME_COLOR_BLUE, IRC_COLOR_BLUE)
					.replace(GAME_COLOR_DGREEN, IRC_COLOR_DGREEN)
					.replace(GAME_COLOR_RED, IRC_COLOR_RED)
					.replace(GAME_COLOR_MAROON, IRC_COLOR_MAROON)
					.replace(GAME_COLOR_PURPLE, IRC_COLOR_PURPLE)
					.replace(GAME_COLOR_GOLD, IRC_COLOR_GOLD)
					.replace(GAME_COLOR_YELLOW, IRC_COLOR_YELLOW)
					.replace(GAME_COLOR_GREEN, IRC_COLOR_GREEN)
					.replace(GAME_COLOR_DAQUA, IRC_COLOR_DAQUA)
					.replace(GAME_COLOR_AQUA, IRC_COLOR_AQUA)
					.replace(GAME_COLOR_DBLUE, IRC_COLOR_DBLUE)
					.replace(GAME_COLOR_PINK, IRC_COLOR_PINK)
					.replace(GAME_COLOR_DGRAY, IRC_COLOR_DGRAY)
					.replace(GAME_COLOR_GRAY, IRC_COLOR_GRAY)
					.replace(GAME_BOLD, IRC_BOLD)
					.replace(GAME_UNDERLINE, IRC_UNDERLINE)
					.replace(GAME_RESET, IRC_RESET)
					.replace(GAME_OBFUSCATED, "")
					.replace(GAME_STRIKETHROUGH, "")
					.replace(GAME_ITALIC, "");
	}

	public static String stripIrcColors(String message) {
		return message
					.replaceAll("\u0003(?=[2-9])", "\u00030")
					.replaceAll("[\u0003]\\d+\\,[\\d][\\d]", "")
					.replaceAll("\u0003[\\d]", "")
					.replaceAll("\u0003(?=[0-1])[(?=\\w)]", "")
					.replaceAll("\u0003(?=\\w)", "")
					.replace("\u0003", "")
					.replace("\u0002", "")
					.replace("\u001F", "")
					.replace("\u000F", "");
	}
	
	public static String stripGameColors(String message) {
		return message.replaceAll("\\§[\\w]", "");
	}
	
	/**
	 * Replaces ampersands with squigglies.
	 * 
	 * @param message The message to correct
	 * @return The corrected message
	 */
	public static String correctCC(String message) {
		return message.replace("&", "§");
	}

}
