package io.github.pirgosth.xclaim;

import io.github.pirgosth.liberty.core.LibertyCore;
import io.github.pirgosth.xclaim.commands.AdminCommands;
import io.github.pirgosth.xclaim.commands.ClaimCommands;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class XClaim extends JavaPlugin{
	@Getter
	private static XClaim instance;

	public static FileConfiguration mainConfig;

	@Override
	public void onEnable() {
		XClaim.instance = this;

		mainConfig = this.getConfig();
		mainConfig.options().copyDefaults(true);

//		getServer().getPluginManager().registerEvents(new EventListener(this), this);

		//Refresh players' claimData when plugin is reloaded
//		for(Player player: Bukkit.getOnlinePlayers()) {
//			ClaimData.updatePlayerRegion(player);
//		}

		LibertyCore.getInstance().getCommandRegister().register(this, new ClaimCommands());
		LibertyCore.getInstance().getCommandRegister().register(this, new AdminCommands());

		new YmlAutoSave().runTaskTimer(this, 200, 36000);//Auto-save configuration files
		Functions.log("[XClaim]" + ChatColor.GREEN + " Plugin loaded !");
	}

	@Override
	public void onDisable() {
		XClaimConfig.getConfiguration().save();
	}
}
