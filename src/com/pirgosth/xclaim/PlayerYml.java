package com.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class PlayerYml {
	
	public static void addClaim(String world, String node, String name, Player player) {
		main.playersYml.get(world).get().set(player.getName()+".claims."+name+".node", node);
	}
	
	public static void addClaim(String node, String name, Player player) {
		addClaim(player.getWorld().getName(), node, name, player);
	}
	
	public static String getClaim(String name, Player player) {
		return main.playersYml.get(player.getWorld().getName()).get().getString(player.getName()+".claims."+name+".node");
	}
	
	public static void delClaim(String world, String name, String player) {
		main.playersYml.get(world).get().set(player+".claims."+name, null);
	}
	
	public static void delClaim(String name, Player player) {
		delClaim(player.getWorld().getName(), name, player.getName());
	}
	
	public static List<String> getClaimsByName(Player player){
		if(main.playersYml.get(player.getWorld().getName()).get().getConfigurationSection(player.getName()+".claims") != null) {
			return new ArrayList<String>(main.playersYml.get(player.getWorld().getName()).get().getConfigurationSection(player.getName()+".claims").getKeys(false));
		}
		else {
			return new ArrayList<String>();
		}
	}
	
}
