package com.pirgosth.claimPlugin;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	public static void setDefaultConfig(FileConfiguration config) {
		config.addDefault("test", false);
		config.options().copyDefaults(true);
	}
	
	private String path = "";
	private JavaPlugin plugin = null;
	private File file = null;
	private FileConfiguration fileConfig = null;
	
	public Config(String path, JavaPlugin plugin) {
		this.plugin = plugin;
		this.path = path;
		reload();
	}
	
	public FileConfiguration get() {
		return fileConfig;
	}
	
	public void reload(){
		file = new File(plugin.getDataFolder(), path);
		fileConfig = YamlConfiguration.loadConfiguration(file);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			plugin.saveResource(path, false); //Use this to create yml from existing file.
		}
	}
	
	public void save() {
		get().options().copyDefaults(true);
		try {
			get().save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
