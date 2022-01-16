package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.xclaim.math.CuboidRegion;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class RegionChunk {
    public final static int CHUNK_SIZE = 10000;

    @Getter
    @NotNull
    private final Set<ClaimConfiguration> claimConfigurations;

    public static ChunkCoordinates GetChunkCoordinates(BlockVector position) {
        return new ChunkCoordinates(position.getBlockX() / CHUNK_SIZE, position.getBlockZ() / CHUNK_SIZE);
    }

    public static ChunkCoordinates GetChunkCoordinates(Location location) {
        return GetChunkCoordinates(new BlockVector(location.getBlockX(), 0, location.getBlockZ()));
    }

    public static Set<ChunkCoordinates> GetContainingChunkCoordinates(CuboidRegion region) {
        Set<ChunkCoordinates> coordinatesSet = new HashSet<>();
        for (int i = 0; i <= (region.getRadius() * 2 + 1) / CHUNK_SIZE; i++) {
            for (int j = 0; j <= (region.getRadius() * 2 + 1) / CHUNK_SIZE; j++) {
                BlockVector vector = region.getLowCorner().clone();
                vector.setX(vector.getBlockX() + i * CHUNK_SIZE);
                vector.setZ(vector.getBlockZ() + j * CHUNK_SIZE);
                coordinatesSet.add(GetChunkCoordinates(vector));
            }
        }

        return coordinatesSet;
    }

    public RegionChunk() {
        this.claimConfigurations = new HashSet<>();
    }

}
