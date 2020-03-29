package com.pirgosth.xclaim;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Messages {
	public static String getRestriction(String restriction) {
		return main.messagesYml.get().getString("restrictions."+restriction) != null ? 
				main.messagesYml.get().getString("restrictions."+restriction): ChatColor.DARK_RED+"No restriction: "+ChatColor.GRAY+restriction+ChatColor.DARK_RED+" found";
	}
	
	public static String getInformation(String information) {
		return main.messagesYml.get().getString("informations."+information) != null ? 
				main.messagesYml.get().getString("informations."+information): ChatColor.DARK_RED+"No information: "+ChatColor.GRAY+information+ChatColor.DARK_RED+" found";
	}
	
	public static void sendRestriction(String restriction, Player player) {
		String message = getRestriction(restriction);
		message = message.replace("%p", player.getName());
		ClaimData cd = ClaimData.getPlayerRegion(player);
		if(cd != null) {
			message = message.replace("%c", cd.getName());
		}
		else {
			message = message.replace("%c", "wild");
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public static void sendRestriction(String restriction, ConsoleCommandSender console) {
		String message = getRestriction(restriction);
		console.sendMessage(message);
	}
	
	public static void sendInformation(String information, Player player) {
		String message = getInformation(information);
		message = message.replace("%p", player.getName());
		ClaimData cd = ClaimData.getPlayerRegion(player);
		if(cd != null) {
			message = message.replace("%c", cd.getName());
		}
		else {
			message = message.replace("%c", "wild");
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public static void sendInformation(String information, Player player, String extra) {
		String message = getInformation(information);
		message = message.replace("%p", player.getName());
		message = message.replace("%s", extra);
		ClaimData cd = ClaimData.getPlayerRegion(player);
		if(cd != null) {
			message = message.replace("%c", cd.getName());
		}
		else {
			message = message.replace("%c", "wild");
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public static void sendInformation(String information, Player player, String extra, String from) {
		String message = getInformation(information);
		message = message.replace("%p", player.getName());
		message = message.replace("%s", extra);
		message = message.replace("%f", from);
		ClaimData cd = ClaimData.getPlayerRegion(player);
		if(cd != null) {
			message = message.replace("%c", cd.getName());
		}
		else {
			message = message.replace("%c", "wild");
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public static void sendInformation(String information, ConsoleCommandSender console) {
		String message = getInformation(information);
		console.sendMessage(message);
	}
}
