package com.pirgosth.xclaim;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin{
	public static FileConfiguration config = null;
	public static Config messagesYml = null;
	public static Config worldsYml = null;
	public static Map<String, Config> claimsYml = new HashMap<>();;
	public static Map<String, Config> playersYml = new HashMap<>();;
	public static Map<String, Boolean> worlds = new HashMap<>();
	public static Map<String, ClaimData> cds = new HashMap<>();
	
	@Override
	public void onEnable() {
		config = this.getConfig();
		config.options().copyDefaults(true);
		Data.load(this);
		Data.save(this);
		try {
			WorldEditCatcher.getWorldEdit();
		}
		catch(MissingPluginException t){
			Functions.log("[ERROR]: "+ChatColor.RED + t.getMessage());
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		getCommand("claim").setExecutor(new CommandClaim());
		getCommand("claim").setTabCompleter(new ClaimTabComplete());
		//Refresh players' claimData when plugin is reloaded
		for(Player player: Bukkit.getOnlinePlayers()) {
			ClaimData.updatePlayerRegion(player);
		}
		new YmlAutoSave(this).runTaskTimer(this, 200, 36000);//Auto-save configuration files
		Functions.log("[XClaim]" + ChatColor.GREEN + " Plugin loaded !");
	}

	@Override
	public void onDisable() {
		Data.save(this);
	}
}
