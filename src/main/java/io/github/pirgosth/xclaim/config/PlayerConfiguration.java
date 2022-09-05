package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.ResourceContext;
import io.github.pirgosth.liberty.core.api.i18n.YamlContextField;
import io.github.pirgosth.liberty.core.api.i18n.YamlField;
import io.github.pirgosth.liberty.core.api.i18n.YamlSerializable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerConfiguration extends YamlSerializable {
    @YamlContextField(key = "world-section")
    private WorldSection worldSection;
    @Getter
    private OfflinePlayer player;
    @Getter
    @YamlField(key = "claims")
    private List<PlayerClaimConfiguration> playerClaimConfigurations;

    public PlayerConfiguration(OfflinePlayer player) {
        this.player = player;
        this.playerClaimConfigurations = new ArrayList<>();
    }

    public PlayerConfiguration() {
        super();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        // Manual player serialization.
        map.put("player", this.player.getUniqueId().toString());
        return map;
    }

    @Override
    public void deserialize(Map<String, Object> values, @Nullable ResourceContext context) {
        super.deserialize(values, context);
        // Manual player deserialization.
        Object rawPlayer = values.get("player");
        if (!(rawPlayer instanceof String playerUUID))
            return;

        this.player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
    }

    public int getClaimCount() {
        return this.playerClaimConfigurations.size();
    }

    @NotNull
    public List<ClaimConfiguration> getClaimConfigurations() {
        List<ClaimConfiguration> claimConfigurations = new ArrayList<>();
        for (PlayerClaimConfiguration pcc : this.playerClaimConfigurations) {
            claimConfigurations.add(pcc.getClaimConfiguration());
        }
        return claimConfigurations;
    }

    @Nullable
    public ClaimConfiguration getClaimConfigurationByName(String name) {
        for (ClaimConfiguration claimConfiguration : this.getClaimConfigurations()) {
            if (claimConfiguration.name.equals(name)) return claimConfiguration;
        }
        return null;
    }

    @Nullable
    public PlayerClaimConfiguration getPlayerClaimConfiguration(ClaimConfiguration claimConfiguration) {
        for (PlayerClaimConfiguration pcc : this.playerClaimConfigurations) {
            if (pcc.getClaimConfiguration().getId().equals(claimConfiguration.getId())) return pcc;
        }
        return null;
    }

    public void addClaimConfiguration(ClaimConfiguration claimConfiguration, Location home) {
        this.playerClaimConfigurations.add(new PlayerClaimConfiguration(this.worldSection, claimConfiguration, home));
    }

    public boolean removeClaimConfiguration(ClaimConfiguration claimConfiguration) {
        return this.playerClaimConfigurations.removeIf(pcc -> pcc.getClaimConfiguration().getId().equals(claimConfiguration.getId()));
    }
}
