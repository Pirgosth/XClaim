package com.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class ClaimTabComplete implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return new ArrayList<String>();
		}
		
		Player player = (Player)sender;
		String world = player.getWorld().getName();
		
		if(main.worlds.get(world)) {
		
			if(args.length == 1){
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[0], Arrays.asList("addOwner", "addMember", "create", "delOwner", "delMember", "info", "leave", "list", "remove", "reload", "home", "sethome"), completions);
				return completions;
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("remove")) {
				if(main.playersYml.get(world).get().getConfigurationSection(sender.getName()+".claims") == null) {
					return new ArrayList<String>();
				}
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[1], new ArrayList<String>(main.playersYml.get(world).get().getConfigurationSection(sender.getName()+".claims").getKeys(false)), completions);
				return completions;
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("home")) {
				if(main.playersYml.get(world).get().getConfigurationSection(sender.getName()+".claims") == null) {
					return new ArrayList<String>();
				}
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[1], new ArrayList<String>(main.playersYml.get(world).get().getConfigurationSection(sender.getName()+".claims").getKeys(false)), completions);
				return completions;
			}
			else if(args.length == 2 && (args[0].equalsIgnoreCase("addOwner") || args[0].equalsIgnoreCase("addMember"))) {
				final Collection<? extends Player> online = Bukkit.getOnlinePlayers();
				ArrayList<String> players = new ArrayList<String>();
				for(Player p: online) {
					players.add(p.getName());
				}
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[1], players, completions);
				return completions;
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("delOwner")) {
				if(main.cds.get(sender.getName()) == null || !Functions.containsIgnoringCase(main.cds.get(sender.getName()).getOwners(), sender.getName())) {
					return new ArrayList<String>();
				}
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[1], main.cds.get(sender.getName()).getOwners(), completions);
				return completions;
			}
			else if(args.length == 2 && args[0].equalsIgnoreCase("delMember")) {
				if(main.cds.get(sender.getName()) == null || !Functions.containsIgnoringCase(main.cds.get(sender.getName()).getOwners(), sender.getName())) {
					return new ArrayList<String>();
				}
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[1], main.cds.get(sender.getName()).getMembers(), completions);
				return completions;
			}
			else {
				return new ArrayList<String>();
			}
		}
		else {
			if(args.length == 1){
				final List<String> completions = new ArrayList<>();
				StringUtil.copyPartialMatches(args[0], Arrays.asList("reload"), completions);
				return completions;
			}
		}
		return new ArrayList<String>();
	}

}
