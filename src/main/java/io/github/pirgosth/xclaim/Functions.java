package io.github.pirgosth.xclaim;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Functions {
	public static void log(final String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public static void insufficientPerm(Player p, String perm) {
		p.sendMessage(ChatColor.DARK_RED+"Missing permission: -"+perm);
	}
	
	public static boolean containsIgnoringCase(List<String> l, String s) {
		for(String e: l) {
			if(e.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsIgnoringCase(Collection<? extends Player> l, String s) {
		for(Player p: l) {
			if(p.getName().equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsIgnoringCase(Set<String> l, String s) {
		for(String e: l) {
			if(e.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}
}
