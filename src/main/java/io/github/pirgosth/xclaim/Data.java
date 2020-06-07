package io.github.pirgosth.xclaim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Data {
	
	public static void initWorlds(JavaPlugin plugin, List<String> worldsList) {
		XClaim.worldsYml = new Config("worlds.yml", plugin,  cfg -> {
			for(String world: worldsList) {
				cfg.addDefault(world+".enabled", true);
			}
		});
	}
	
	public static void initDefaultConfig() {
		XClaim.config.addDefault("claims.range", 200);
		XClaim.config.addDefault("claims.count", 3);
	}
	
	public static void loadClaims(JavaPlugin plugin, List<String> worldsList) {
		for(String world : worldsList) {
			XClaim.claimsYml.put(world, new Config("worlds/" + world + "/claims.yml", plugin));
		}
	}
	
	public static void loadPlayers(JavaPlugin plugin, List<String> worldsList) {
		for(String world : worldsList) {
			XClaim.playersYml.put(world, new Config("worlds/" + world + "/players.yml", plugin));
		}
	}
	
	public static List<String> getWorldsList() {
//		try (Stream<Path> walk = Files.walk(Paths.get("claims"))) {
//
//			List<String> result = walk.map(x -> x.toString())
//					.filter(f -> f.endsWith(".java")).collect(Collectors.toList());
//
//			result.forEach(System.out::println);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		List<String> worlds = new ArrayList<String>();
		for(World world : Bukkit.getServer().getWorlds()) {
			worlds.add(world.getName());
		}
		return worlds;
	}
	
	public static void loadWorlds(List<String> worldsList) {
		for(String world : worldsList){
			XClaim.worlds.put(world, XClaim.worldsYml.get().getBoolean(world+".enabled"));//Dynamically load worlds state
			File file = new File("plugins/XClaim/worlds/" + world);
			if(file.mkdirs()) {//Create corresponding directories (./worlds/<worldname>/)
				//Functions.log("Dir created successfuly !");
			}
			else {
				//Functions.log("Can't create dir: " + file.toString());
			}
		}
	}
	
	public static void load(JavaPlugin plugin) {
		initDefaultConfig();
		XClaim.messagesYml = new Config("messages.yml", plugin);
		List<String> worldsList = Data.getWorldsList();
		Data.initWorlds(plugin, worldsList);
		loadWorlds(worldsList);
		Data.loadClaims(plugin, worldsList);
		Data.loadPlayers(plugin, worldsList);
	}
	
	public static void reload(JavaPlugin plugin) {
		XClaim.claimsYml.forEach((k, v)-> v.save());
		XClaim.playersYml.forEach((k, v)-> v.save());
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		plugin.reloadConfig();
		XClaim.config = plugin.getConfig();
		XClaim.messagesYml = new Config("messages.yml", plugin);
		List<String> worldsList = Data.getWorldsList();
		initWorlds(plugin, worldsList);
		loadWorlds(worldsList);
		Data.loadClaims(plugin, worldsList);
		Data.loadPlayers(plugin, worldsList);
		sender.sendMessage("[XClaim]: " + ChatColor.DARK_GREEN + "Data reloaded");
	}
	
	public static void save(JavaPlugin plugin) {
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		XClaim.claimsYml.forEach((k, v)-> v.save());
		XClaim.playersYml.forEach((k, v)-> v.save());
		plugin.saveConfig();
		XClaim.messagesYml.save();
		XClaim.worldsYml.save();
		sender.sendMessage("[XClaim]: " + ChatColor.DARK_GREEN + "Data saved");
	}
}
