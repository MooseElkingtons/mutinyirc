package com.mutinycraft.irc;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * Miscellaneous utility for network-related tasks.
 * 
 * @author MooseElkingtons
 */
public class MiscUtil {

	public static byte[] toRawAddress(String addr) {
		byte[] address = new byte[4];
		String[] s = addr.split("\\.");
		for(int i = 0; i < 4; i++) {
			address[i] = (byte) Integer.parseInt(s[i]);
		}
		return address;
	}
	
	public static String getHostName(String addr) {
		if(!isRawAddress(addr))
			return addr;
		try {
			return InetAddress.getByAddress(toRawAddress(addr))
					.getHostName();
		} catch (UnknownHostException e) {
			return addr;
		}
	}
	
	public static boolean isRawAddress(String addr) {
		return addr.matches("(\\d+).(\\d+).(\\d+).(\\d+)");
	}
}
