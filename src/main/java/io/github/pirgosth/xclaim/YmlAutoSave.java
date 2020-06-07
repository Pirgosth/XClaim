package io.github.pirgosth.xclaim;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class YmlAutoSave extends BukkitRunnable{

	private JavaPlugin plugin = null;
	
	public YmlAutoSave(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		Data.save(plugin);
	}
	
}
