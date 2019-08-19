package com.pirgosth.claimPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class ClaimTabComplete implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if(args.length == 1){
			final List<String> completions = new ArrayList<>();
			StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "info", "remove"), completions);
			return completions;
		}
		else if(args.length == 3 && args[0].equalsIgnoreCase("create")) {
			final List<String> completions = new ArrayList<>();
			StringUtil.copyPartialMatches(args[2], Arrays.asList("cuboid", "selection"), completions);
			return completions;
		}
		else {
			return new ArrayList<String>();
		}
	}

}
