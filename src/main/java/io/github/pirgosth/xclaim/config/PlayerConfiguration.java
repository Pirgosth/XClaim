package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerConfiguration implements ConfigurationSerializable {
    @NotNull
    private final WorldSection worldSection;
    @Getter
    private final OfflinePlayer player;
    @Getter
    private final List<PlayerClaimConfiguration> playerClaimConfigurations;

    public PlayerConfiguration(@NotNull WorldSection worldSection, OfflinePlayer player) {
        this.worldSection = worldSection;
        this.player = player;
        this.playerClaimConfigurations = new ArrayList<>();
    }

    public PlayerConfiguration(@NotNull WorldSection worldSection, Map<String, Object> map) {
        this.worldSection = worldSection;
        Object rawPlayer = map.get("player");
        Object rawPlayerClaimConfigurations = map.get("claims");

        this.player = (rawPlayer instanceof String) ? Bukkit.getOfflinePlayer(UUID.fromString((String) rawPlayer)) : null;

        List<PlayerClaimConfiguration> deserializedPlayerClaimConfigurations = new ArrayList<>();
        for (Object rawPlayerClaimConfig : (List<?>)rawPlayerClaimConfigurations) {
            if (rawPlayerClaimConfig instanceof Map<?, ?>) {
                deserializedPlayerClaimConfigurations.add(new PlayerClaimConfiguration(this.worldSection, SerializationUtils.safeMapSerialize((Map<?, ?>) rawPlayerClaimConfig)));
            }
        }

        this.playerClaimConfigurations = deserializedPlayerClaimConfigurations;
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
            if (claimConfiguration.getName().equals(name)) return claimConfiguration;
        }
        return null;
    }

    @Nullable
    public PlayerClaimConfiguration getPlayerClaimConfiguration(ClaimConfiguration claimConfiguration) {
        for (PlayerClaimConfiguration pcc : this.playerClaimConfigurations) {
            if(pcc.getClaimConfiguration().getId().equals(claimConfiguration.getId())) return pcc;
        }
        return null;
    }

    public void addClaimConfiguration(ClaimConfiguration claimConfiguration, Location home) {
        this.playerClaimConfigurations.add(new PlayerClaimConfiguration(this.worldSection, claimConfiguration, home));
    }

    public boolean removeClaimConfiguration(ClaimConfiguration claimConfiguration) {
        return this.playerClaimConfigurations.removeIf(pcc -> pcc.getClaimConfiguration().getId().equals(claimConfiguration.getId()));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        List<Map<String, Object>> serializedClaims = new ArrayList<>();
        for (PlayerClaimConfiguration pcc : this.playerClaimConfigurations) serializedClaims.add(pcc.serialize());
        Map<String, Object> map = new HashMap<>();
        map.put("player", this.player.getUniqueId().toString());
        map.put("claims", serializedClaims);
        return map;
    }
}
