package io.github.pirgosth.xclaim.config;

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
    private final ClaimPluginResource claimPluginResource;
    private final PlayerPluginResource playerPluginResource;
    private final Map<ChunkCoordinates, RegionChunk> regionChunkMap = new HashMap<>();

    @NotNull
    private RegionChunk getOrCreateChunk(ChunkCoordinates coordinates) {
        RegionChunk chunk = this.regionChunkMap.get(coordinates);
        if (chunk == null) {
            chunk = new RegionChunk();
            this.regionChunkMap.put(coordinates, chunk);
        }

        return chunk;
    }

    private void reloadChunks() {
        this.regionChunkMap.clear();
        for (ClaimConfiguration claimConfig : this.claimPluginResource.claims) {
            Set<ChunkCoordinates> coordinatesSet = RegionChunk.GetContainingChunkCoordinates(claimConfig.getRegion());
            for (ChunkCoordinates coordinates : coordinatesSet) {
                RegionChunk chunk = this.getOrCreateChunk(coordinates);
                chunk.getClaimConfigurations().add(claimConfig);
            }
        }
    }

    public WorldSection(String worldName) {
        this.world = Bukkit.getWorld(worldName);
        this.claimPluginResource = new ClaimPluginResource(String.format("%s/%s/claims.yml", baseYamlConfigPath, worldName));
        this.claimPluginResource.reload();
        this.reloadChunks();
        this.playerPluginResource = new PlayerPluginResource(this, String.format("%s/%s/players.yml", baseYamlConfigPath, worldName));
        this.playerPluginResource.reload();
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
        for (PlayerConfiguration config : this.playerPluginResource.players) {
            if (config.getPlayer().getUniqueId().equals(player.getUniqueId())) playerConfiguration = config;
        }

        if (playerConfiguration == null) {
            playerConfiguration = new PlayerConfiguration(player);
            this.playerPluginResource.players.add(playerConfiguration);
        }

        return playerConfiguration;
    }

    private ClaimConfiguration createClaimConfiguration(Player player, String name, CuboidRegion region) {
        ClaimConfiguration claimConfiguration = new ClaimConfiguration(player, name, region);
        this.claimPluginResource.claims.add(claimConfiguration);
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
        for (ClaimConfiguration config : this.claimPluginResource.claims) {
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
        this.claimPluginResource.claims.remove(claimConfiguration);
    }

    public void save() {
        this.claimPluginResource.save();
        this.playerPluginResource.save();
    }
}
