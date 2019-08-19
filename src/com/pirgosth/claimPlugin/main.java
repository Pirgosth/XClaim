package com.pirgosth.claimPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.math.BlockVector3;

public class main extends JavaPlugin{
	FileConfiguration config = null;
	public static Config claimsYml = null;
	public static Config playersYml = null;
	public static Map<String, ClaimData> cds = new HashMap<>();
	public static void log(final String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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
	
	public static void insufficientPerm(Player p, String perm) {
		p.sendMessage(ChatColor.DARK_RED+"Missing permission: -"+perm);
	}
	
	@Override
	public void onEnable() {
		config = getConfig();
		Config.setDefaultConfig(config);
		claimsYml = new Config("claims.yml", this);
		claimsYml.save();
		playersYml = new Config("players.yml", this);
		playersYml.save();
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
		getCommand("test").setExecutor(new CommandTest());
		getCommand("claim").setExecutor(new CommandClaim());
		getCommand("claim").setTabCompleter(new ClaimTabComplete());
		//Refresh players' claimData when plugin is reloaded
		for(Player player: Bukkit.getOnlinePlayers()) {
			cds.put(player.getName(), ClaimData.getInRegion(player.getLocation()));
		}
		log("[ClaimPlugin]" + ChatColor.GREEN + "Plugin loaded !");
	}

	@Override
	public void onDisable() {
		
	}
}
