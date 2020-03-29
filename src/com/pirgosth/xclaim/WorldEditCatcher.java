package com.pirgosth.xclaim;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class WorldEditCatcher {
	public static WorldEditPlugin getWorldEdit() throws MissingPluginException{
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if(!(p instanceof WorldEditPlugin)) {
			throw new MissingPluginException("WorldEdit is required for this plugin to work !");
		}
		return (WorldEditPlugin) p; 
	}
}
