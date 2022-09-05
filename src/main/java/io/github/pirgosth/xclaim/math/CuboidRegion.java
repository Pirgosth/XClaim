package io.github.pirgosth.xclaim.math;

import io.github.pirgosth.liberty.core.api.i18n.ResourceContext;
import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CuboidRegion extends Region {

    private World world;
    @NotNull
    @Getter
    private BlockVector lowCorner;
    @NotNull
    @Getter
    private BlockVector highCorner;

    public CuboidRegion(Location center, int radius) {
        world = Objects.requireNonNull(center.getWorld());
        lowCorner = new BlockVector(center.getX() - radius, world.getMinHeight(), center.getZ() - radius);
        highCorner = new BlockVector(center.getX() + radius, world.getMaxHeight(), center.getZ() + radius);
    }

    public CuboidRegion() {
        super();
    }

    public boolean contains(Location location) {
        return Objects.equals(location.getWorld(), world) &&
                lowCorner.getBlockX() <= location.getBlockX() && location.getBlockX() <= highCorner.getBlockX() &&
                lowCorner.getBlockZ() <= location.getBlockZ() && location.getBlockZ() <= highCorner.getBlockZ();
    }

    @Override
    public boolean overlap(Region region) {
        if (region instanceof CuboidRegion cuboidRegion) {
            return cuboidRegion.getLowCorner().getBlockX() < highCorner.getBlockX() &&
                    cuboidRegion.getHighCorner().getBlockX() > lowCorner.getBlockX() &&
                    cuboidRegion.getLowCorner().getBlockZ() < highCorner.getBlockZ() &&
                    cuboidRegion.getHighCorner().getBlockZ() > lowCorner.getBlockZ();
        }

        return true;
    }

    public int getRadius() {
        return (highCorner.getBlockX() - lowCorner.getBlockX()) / 2;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("world", this.world.getName());
        map.put("low-corner", this.lowCorner.serialize());
        map.put("high-corner", this.highCorner.serialize());
        return map;
    }

    @Override
    public void deserialize(Map<String, Object> values, @Nullable ResourceContext context) {
        if (!(values.get("world") instanceof String rawWorldName))
            throw new IllegalArgumentException("Invalid type for world!");
        if (!(values.get("low-corner") instanceof Map rawLowCorner))
            throw new IllegalArgumentException("Invalid type for low-corner!");
        if (!(values.get("high-corner") instanceof Map rawHighCorner))
            throw new IllegalArgumentException("Invalid type for high-corner!");

        this.world = Bukkit.getWorld(rawWorldName);
        this.lowCorner = BlockVector.deserialize(SerializationUtils.safeMapSerialize((Map<?, ?>) rawLowCorner));
        this.highCorner = BlockVector.deserialize(SerializationUtils.safeMapSerialize((Map<?, ?>) rawHighCorner));
    }

    @Override
    public String toString() {
        return String.format("[%s;%s]-[%s;%s]", lowCorner.getBlockX(), lowCorner.getBlockZ(), highCorner.getBlockX(), highCorner.getBlockZ());
    }
}
