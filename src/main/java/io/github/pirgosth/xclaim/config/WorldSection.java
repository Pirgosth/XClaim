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
    private Map<ChunkCoordinates, RegionChunk> regionChunkMap;
    private ArrayList<ClaimConfiguration> claimConfigurations;
    private ArrayList<PlayerConfiguration> playerConfigurations;

    @NotNull
    private RegionChunk getOrCreateChunk(ChunkCoordinates coordinates) {
        RegionChunk chunk = regionChunkMap.get(coordinates);
        if (chunk == null) {
            chunk = new RegionChunk();
            regionChunkMap.put(coordinates, chunk);
        }

        return chunk;
    }

    private void deserialize() {
        Map<String, Object> claimMap = claimYamlConfig.get().getValues(true);
        Object rawClaims = claimMap.get("claims");

        this.claimConfigurations = new ArrayList<>();
        this.regionChunkMap = new HashMap<>();

        ArrayList<ClaimConfiguration> claimConfigurations = (rawClaims instanceof List) ? new ArrayList<>(SerializationUtils.safeListCast(ClaimConfiguration.class, (List<?>) rawClaims)) : new ArrayList<>();
        for (ClaimConfiguration claimConfig : claimConfigurations) {
            this.claimConfigurations.add(claimConfig);
            Set<ChunkCoordinates> coordinatesSet = RegionChunk.GetContainingChunkCoordinates(claimConfig.getRegion());
            for (ChunkCoordinates coordinates : coordinatesSet) {
                RegionChunk chunk = this.getOrCreateChunk(coordinates);
                chunk.getClaimConfigurations().add(claimConfig);
            }
        }

        Map<String, Object> playerMap = playerYamlConfig.get().getValues(true);
        Object rawPlayers = playerMap.get("players");
        if (rawPlayers != null && !(rawPlayers instanceof List)) throw new IllegalArgumentException("PlayerConfiguration is not valid");

        ArrayList<PlayerConfiguration> deserializedPlayerConfigurations = new ArrayList<>();
        for (Object rawPlayerConfig : rawPlayers != null ? (List<?>) rawPlayers : new ArrayList<>()) {
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
        for (ChunkCoordinates coordinates : RegionChunk.GetContainingChunkCoordinates(region)) {
            RegionChunk chunk = this.regionChunkMap.get(coordinates);
            if (chunk == null) continue;
            for (ClaimConfiguration claimConfiguration : chunk.getClaimConfigurations()) {
                if (claimConfiguration.getRegion().overlap(region)) return false;
            }
        }

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

    private ClaimConfiguration createClaimConfiguration(Player player, String name, CuboidRegion region) {
        ClaimConfiguration claimConfiguration = new ClaimConfiguration(player, name, region);
        this.claimConfigurations.add(claimConfiguration);
        for (ChunkCoordinates coordinates : RegionChunk.GetContainingChunkCoordinates(claimConfiguration.getRegion())) {
            this.getOrCreateChunk(coordinates).getClaimConfigurations().add(claimConfiguration);
        }
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
        RegionChunk chunk = this.regionChunkMap.get(RegionChunk.GetChunkCoordinates(location));
        if (chunk == null) return null;
        for (ClaimConfiguration claimConfiguration : chunk.getClaimConfigurations()) {
            if (claimConfiguration.getRegion().contains(location)) return claimConfiguration;
        }
        return null;
    }

    public void removeClaim(ClaimConfiguration claimConfiguration) {
        for (ChunkCoordinates coordinates : RegionChunk.GetContainingChunkCoordinates(claimConfiguration.getRegion())) {
            RegionChunk chunk = this.regionChunkMap.get(coordinates);
            if (chunk == null) continue;
            chunk.getClaimConfigurations().remove(claimConfiguration);
        }
        this.claimConfigurations.remove(claimConfiguration);
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
