package io.github.pirgosth.xclaim.tabcompleter;

import io.github.pirgosth.liberty.core.api.commands.ICustomTabCompleter;
import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import io.github.pirgosth.xclaim.config.PlayerConfiguration;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaimNamesTabCompleter implements ICustomTabCompleter {

    @Override
    public List<String> complete(CommandSender commandSender) {
        Player player = (Player) commandSender;
        @NotNull World world = player.getWorld();
        if (!XClaimConfig.getConfiguration().isWorldEnabled(world))
            return new ArrayList<>();
        PlayerConfiguration playerConfiguration = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(world)).getPlayerConfiguration(player);
        List<String> claimNames = new ArrayList<>();
        for (ClaimConfiguration claimConfiguration : playerConfiguration.getClaimConfigurations()) claimNames.add(claimConfiguration.name);
        return claimNames;
    }
}
