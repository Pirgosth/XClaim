package io.github.pirgosth.xclaim.tabcompleter;

import io.github.pirgosth.liberty.core.api.commands.ICustomTabCompleter;
import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import io.github.pirgosth.xclaim.config.WorldSection;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaimNonMemberPlayersTabCompleter implements ICustomTabCompleter {

    @Override
    public List<String> complete(CommandSender commandSender) {
        List<String> result = new ArrayList<>();

        Player player = (Player) commandSender;
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));
        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(player.getLocation());

        if (claimConfiguration == null) return result;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!claimConfiguration.isMember(onlinePlayer)) result.add(onlinePlayer.getName());
        }

        return result;
    }
}
