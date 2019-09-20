package com.pirgosth.xclaim;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.math.BlockVector3;

public class main extends JavaPlugin{
	FileConfiguration config = null;
	public static Config claimsYml = null;
	public static Config playersYml = null;
	public static Config messagesYml = null;
	public static Map<String, ClaimData> cds = new HashMap<>();
	public static void log(final String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	public static void saveData(ConsoleCommandSender console) {
		claimsYml.save();
		playersYml.save();
		console.sendMessage("[XClaim] Data saved");
	}
	
	public static BlockVector3 Location2Vector(Location l) {
		return BlockVector3.at(l.getBlockX(), l.getBlockY(), l.getBlockZ());
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
	
	public static void insufficientPerm(Player p, String perm) {
		p.sendMessage(ChatColor.DARK_RED+"Missing permission: -"+perm);
	}
	
	public static void updatePlayerRegion(Player player) {
		cds.put(player.getName(), ClaimData.getInRegion(player.getLocation()));
	}
	
	public static ClaimData getPlayerRegion(Player player) {
		return cds.get(player.getName());
	}
	
	public static void setPlayerRegion(Player player, ClaimData cd) {
		main.cds.put(player.getName(), cd);
	}
	
	@Override
	public void onEnable() {
		config = getConfig();
		Config.setDefaultConfig(config);
		claimsYml = new Config("claims.yml", this);
		claimsYml.save();
		playersYml = new Config("players.yml", this);
		playersYml.save();
		messagesYml = new Config("messages.yml", this);
		messagesYml.save();
		saveConfig();
		
		try {
			WorldEditCatcher.getWorldEdit();
		}
		catch(MissingPluginException t){
			log("[ERROR]: "+ChatColor.RED + t.getMessage());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		getCommand("claim").setExecutor(new CommandClaim());
		getCommand("claim").setTabCompleter(new ClaimTabComplete());
		//Refresh players' claimData when plugin is reloaded
		for(Player player: Bukkit.getOnlinePlayers()) {
			updatePlayerRegion(player);
		}
		new YmlAutoSave(this).runTaskTimer(this, 200, 36000);
		log("[XClaim]" + ChatColor.GREEN + "Plugin loaded !");
	}

	@Override
	public void onDisable() {
		saveData(getServer().getConsoleSender());
	}
}
