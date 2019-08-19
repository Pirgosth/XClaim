package com.pirgosth.claimPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTest implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			sender.sendMessage(ChatColor.GREEN + "Bonjour maitre !");
			return true;
		}
		sender.sendMessage(ChatColor.YELLOW + "Personne ne verra que je suis une console.");
		return true;
	}

}
