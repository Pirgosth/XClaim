package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import io.github.pirgosth.xclaim.Config;
import io.github.pirgosth.xclaim.XClaim;
import io.github.pirgosth.xclaim.math.CuboidRegion;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldSection {

    private static final String baseYamlConfigPath = "worlds";

    @Getter
    private final World world;
    private final Config claimYamlConfig;
    private final Config playerYamlConfig;
    private List<ClaimConfiguration> claimConfigurations;
    private List<PlayerConfiguration> playerConfigurations;

    private void deserialize() {
        Map<String, Object> claimMap = claimYamlConfig.get().getValues(true);
        Object rawClaims = claimMap.get("claims");
        claimConfigurations = (rawClaims instanceof List) ? SerializationUtils.safeListCast(ClaimConfiguration.class, (List<?>)rawClaims) : new ArrayList<>();

        Map<String, Object> playerMap = playerYamlConfig.get().getValues(true);
        Object rawPlayers = playerMap.get("players");
        if(!(rawPlayers instanceof List)) throw new IllegalArgumentException("PlayerConfiguration is not valid");

        List<PlayerConfiguration> deserializedPlayerConfigurations = new ArrayList<>();
        for (Object rawPlayerConfig : (List<?>)rawPlayers) {
            if (rawPlayerConfig instanceof Map<?, ?>) {
                deserializedPlayerConfigurations.add(new PlayerConfiguration(this, SerializationUtils.safeMapSerialize((Map<?, ?>) rawPlayerConfig)));
            }
        }

        playerConfigurations = deserializedPlayerConfigurations;
    }

    public WorldSection(String worldName) {
        this.world = Bukkit.getWorld(worldName);
        claimYamlConfig = new Config(String.format("%s/%s/claims.yml", baseYamlConfigPath, worldName), XClaim.getInstance());
        playerYamlConfig = new Config(String.format("%s/%s/players.yml", baseYamlConfigPath, worldName), XClaim.getInstance());

        this.deserialize();
    }

    public boolean isLandAvailable(CuboidRegion region) {
        //TODO: implement collision algorithm.
        return true;
    }

    @NotNull
    public PlayerConfiguration getPlayerConfiguration(@NotNull OfflinePlayer player) {
        PlayerConfiguration playerConfiguration = null;
        for (PlayerConfiguration config : this.playerConfigurations) {
            if (config.getPlayer().equals(player)) playerConfiguration = config;
        }

        if(playerConfiguration == null) {
            playerConfiguration = new PlayerConfiguration(this, player);
            this.playerConfigurations.add(playerConfiguration);
        }

        return playerConfiguration;
    }

    private ClaimConfiguration createClaimConfiguration(Player player, String name, CuboidRegion region) {
        ClaimConfiguration claimConfiguration = new ClaimConfiguration(player, name, region);
        this.claimConfigurations.add(claimConfiguration);
        return claimConfiguration;
    }

    private void addClaimToPlayerConfiguration(Player player, ClaimConfiguration claimConfiguration) {
        PlayerConfiguration playerConfiguration = this.getPlayerConfiguration(player);
        playerConfiguration.addClaimConfiguration(claimConfiguration, player.getLocation());
    }

    public void createClaim(Player player, String name, CuboidRegion region) {
        ClaimConfiguration claimConfiguration = this.createClaimConfiguration(player, name, region);
        this.addClaimToPlayerConfiguration(player, claimConfiguration);
    }

    @Nullable
    public ClaimConfiguration getClaimConfigurationById(UUID claimId) {
        for (ClaimConfiguration config : this.claimConfigurations) {
            if (config.getId().equals(claimId)) return config;
        }
        return null;
    }

    @Nullable
    public ClaimConfiguration getClaimConfigurationByName(String name) {
        for (ClaimConfiguration config : this.claimConfigurations) {
            if (config.getName().equals(name)) return config;
        }
        return null;
    }

    @Nullable
    public ClaimConfiguration getClaimConfigurationByLocation(Location location) {
        for (ClaimConfiguration config : this.claimConfigurations) {
            if (config.getRegion().contains(location)) return config;
        }
        return null;
    }

    public boolean removeClaim(ClaimConfiguration claimConfiguration) {
        return this.claimConfigurations.remove(claimConfiguration);
    }

    public void save() {
        List<Map<String, Object>> serializedClaims = new ArrayList<>();

        for(ClaimConfiguration claim : this.claimConfigurations) {
            serializedClaims.add(claim.serialize());
        }

        List<Map<String, Object>> serializedPlayers = new ArrayList<>();

        for(PlayerConfiguration player : this.playerConfigurations) {
            serializedPlayers.add(player.serialize());
        }

        claimYamlConfig.get().set("claims", serializedClaims);
        playerYamlConfig.get().set("players", serializedPlayers);

        claimYamlConfig.save();
        playerYamlConfig.save();
    }
}
