package io.github.pirgosth.xclaim;

import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Messages {
	public static String getRestriction(String restriction) {
		return XClaim.messagesYml.get().getString("restrictions."+restriction) != null ? 
				XClaim.messagesYml.get().getString("restrictions."+restriction): ChatColor.DARK_RED+"No restriction: "+ChatColor.GRAY+restriction+ChatColor.DARK_RED+" found";
	}
	
	public static String getInformation(String information) {
		return XClaim.messagesYml.get().getString("informations."+information) != null ? 
				XClaim.messagesYml.get().getString("informations."+information): ChatColor.DARK_RED+"No information: "+ChatColor.GRAY+information+ChatColor.DARK_RED+" found";
	}

	public static void sendRestriction(String restriction, ConsoleCommandSender console) {
		String message = getRestriction(restriction);
		console.sendMessage(message);
	}
	
	public static void sendInformation(String information, Player player) {
		String message = getInformation(information);
		message = message.replace("%p", player.getName());
		ClaimConfiguration claimConfiguration = XClaimConfig.getConfiguration().getWorldSection(player.getWorld()).getClaimConfigurationByLocation(player.getLocation());
		if(claimConfiguration != null) {
			message = message.replace("%c", claimConfiguration.getName());
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
		ClaimConfiguration claimConfiguration = XClaimConfig.getConfiguration().getWorldSection(player.getWorld()).getClaimConfigurationByLocation(player.getLocation());
		if(claimConfiguration != null) {
			message = message.replace("%c", claimConfiguration.getName());
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
		ClaimConfiguration claimConfiguration = XClaimConfig.getConfiguration().getWorldSection(player.getWorld()).getClaimConfigurationByLocation(player.getLocation());
		if(claimConfiguration != null) {
			message = message.replace("%c", claimConfiguration.getName());
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
