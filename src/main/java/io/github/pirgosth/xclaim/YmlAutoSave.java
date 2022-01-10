package io.github.pirgosth.xclaim;

import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.scheduler.BukkitRunnable;

public class YmlAutoSave extends BukkitRunnable{

	@Override
	public void run() {
		XClaimConfig.getConfiguration().save();
	}
	
}
