package com.pirgosth.xclaim;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	
	private String path = "";
	private JavaPlugin plugin = null;
	private File file = null;
	private FileConfiguration fileConfig = null;
	
	public Config(String path, JavaPlugin plugin) {
		this.plugin = plugin;
		this.path = path;
		reload();
	}
	
	public Config(String path, JavaPlugin plugin, Consumer<FileConfiguration> foo) {
		this.plugin = plugin;
		this.path = path;
		reload();
		setDefaultConfig(foo);
	}
	
	private void setDefaultConfig(Consumer<FileConfiguration> foo) {
		foo.accept(fileConfig);
	}
	
	public FileConfiguration get() {
		return fileConfig;
	}
	
	public void reload(){
		//For default config file in project directory
		if(plugin.getResource(path) != null) {
			plugin.saveResource(path, false);
		}
		file = new File(plugin.getDataFolder(), path);
		fileConfig = YamlConfiguration.loadConfiguration(file);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
