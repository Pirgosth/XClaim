package io.github.pirgosth.xclaim.commands;

import io.github.pirgosth.liberty.core.api.commands.ICommandListener;
import io.github.pirgosth.liberty.core.api.commands.annotations.LibertyCommand;
import io.github.pirgosth.liberty.core.api.commands.annotations.LibertyCommandPermission;
import io.github.pirgosth.liberty.core.commands.CommandParameters;
import io.github.pirgosth.xclaim.XClaim;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class AdminCommands implements ICommandListener {

    @LibertyCommand(command = "claim.reload")
    @LibertyCommandPermission(permission = "xclaim.admin.commands.reload")
    public boolean reload(CommandParameters params) {
        XClaim.getInstance().reloadConfig();
        XClaimConfig.reloadConfiguration();
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Plugin reloaded !");
        return true;
    }

    @LibertyCommand(command = "claim.save")
    @LibertyCommandPermission(permission = "xclaim.admin.commands.save")
    public boolean save(CommandParameters params) {
        XClaimConfig.getConfiguration().save();
        XClaim.messagesYml.save();
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Plugin configuration saved on the disk!");
        return true;
    }

}
