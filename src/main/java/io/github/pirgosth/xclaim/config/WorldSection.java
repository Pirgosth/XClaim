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

import java.util.*;

public class WorldSection {

    private static final String baseYamlConfigPath = "worlds";

    @Getter
    private final World world;
    private final Config claimYamlConfig;
    private final Config playerYamlConfig;
    private ArrayList<ClaimConfiguration> claimConfigurations;
    private ArrayList<PlayerConfiguration> playerConfigurations;

    private void deserialize() {
        Map<String, Object> claimMap = claimYamlConfig.get().getValues(true);
        Object rawClaims = claimMap.get("claims");

        if (rawClaims instanceof List) {
            ArrayList<ClaimConfiguration> sortedClaimConfigurations = new ArrayList<>(SerializationUtils.safeListCast(ClaimConfiguration.class, (List<?>) rawClaims));
            Collections.sort(sortedClaimConfigurations);
            this.claimConfigurations = sortedClaimConfigurations;
        } else {
            claimConfigurations = new ArrayList<>();
        }

        Map<String, Object> playerMap = playerYamlConfig.get().getValues(true);
        Object rawPlayers = playerMap.get("players");
        if (!(rawPlayers instanceof List)) throw new IllegalArgumentException("PlayerConfiguration is not valid");

        ArrayList<PlayerConfiguration> deserializedPlayerConfigurations = new ArrayList<>();
        for (Object rawPlayerConfig : (List<?>) rawPlayers) {
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
            if (config.getPlayer().getUniqueId().equals(player.getUniqueId())) playerConfiguration = config;
        }

        if (playerConfiguration == null) {
            playerConfiguration = new PlayerConfiguration(this, player);
            this.playerConfigurations.add(playerConfiguration);
        }

        return playerConfiguration;
    }

    private int getClaimConfigurationInsertIndex(CuboidRegion region) {
        int length = this.claimConfigurations.size();
        int distance = region.distance();

        if (length == 0) {
            return 0;
        } else if (length == 1) {
            return distance > this.claimConfigurations.get(0).getRegion().distance() ? 1 : 0;
        }

        int lowBound = 0;
        int highBound = length - 1;

        int cursor = (int) Math.ceil(lowBound + highBound) / 2;

        while (cursor != length - 1 && (distance < this.claimConfigurations.get(cursor).getRegion().distance() || distance > this.claimConfigurations.get(cursor + 1).getRegion().distance())) {
            if (lowBound > highBound) {
                return highBound > 0 ? length : 0;
            }

            if (distance < this.claimConfigurations.get(cursor).getRegion().distance()) {
                highBound = cursor - 1;
            } else {
                lowBound = cursor + 1;
            }

            cursor = (int) Math.ceil(lowBound + highBound) / 2;
        }

        return cursor + 1;
    }

    private ClaimConfiguration createClaimConfiguration(Player player, String name, CuboidRegion region) {
        ClaimConfiguration claimConfiguration = new ClaimConfiguration(player, name, region);
        this.claimConfigurations.add(this.getClaimConfigurationInsertIndex(region), claimConfiguration);
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
    public ClaimConfiguration getClaimConfigurationByLocation(Location location) {
        int length = this.claimConfigurations.size();

        if (length == 0) return null;

        int distance = (int) Math.ceil(Math.sqrt(location.getBlockX() * location.getBlockX() + location.getBlockZ() * location.getBlockZ()));

        int lowBound = 0;
        int highBound = length - 1;
        int cursor = (int) Math.ceil(lowBound + highBound) / 2;
        ClaimConfiguration lastClaim = this.claimConfigurations.get(cursor);
        boolean doesLastClaimContainsLocation = lastClaim.getRegion().contains(location);

        while (lowBound <= highBound && !doesLastClaimContainsLocation) {
            if (distance > this.claimConfigurations.get(cursor).getRegion().distance()) {
                lowBound = cursor + 1;
            } else {
                highBound = cursor - 1;
            }

            cursor = (int) Math.ceil(lowBound + highBound) / 2;
            lastClaim = this.claimConfigurations.get(cursor);
            doesLastClaimContainsLocation = lastClaim.getRegion().contains(location);
        }

        return doesLastClaimContainsLocation ? lastClaim : null;
    }

    public boolean removeClaim(ClaimConfiguration claimConfiguration) {
        return this.claimConfigurations.remove(claimConfiguration);
    }

    public void save() {
        List<Map<String, Object>> serializedClaims = new ArrayList<>();

        for (ClaimConfiguration claim : this.claimConfigurations) {
            serializedClaims.add(claim.serialize());
        }

        List<Map<String, Object>> serializedPlayers = new ArrayList<>();

        for (PlayerConfiguration player : this.playerConfigurations) {
            serializedPlayers.add(player.serialize());
        }

        claimYamlConfig.get().set("claims", serializedClaims);
        playerYamlConfig.get().set("players", serializedPlayers);

        claimYamlConfig.save();
        playerYamlConfig.save();
    }
}
