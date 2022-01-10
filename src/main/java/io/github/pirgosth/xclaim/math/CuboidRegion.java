package io.github.pirgosth.xclaim.math;

import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CuboidRegion extends Region implements ConfigurationSerializable {

    private final World world;
    @NotNull @Getter
    private final BlockVector lowCorner;
    @NotNull @Getter
    private final BlockVector highCorner;

    public CuboidRegion(Map<String, Object> map) {
        Object rawWorldId = map.get("world-id");
        Object rawLowCorner = map.get("low-corner");
        Object rawHighCorner = map.get("high-corner");

        this.world = (rawWorldId instanceof String) ? Bukkit.getWorld(UUID.fromString((String) rawWorldId)) : null;
        this.lowCorner = (rawLowCorner instanceof Map) ? BlockVector.deserialize(SerializationUtils.safeMapSerialize((Map<?, ?>) rawLowCorner)) : null;
        this.highCorner = (rawHighCorner instanceof Map) ? BlockVector.deserialize(SerializationUtils.safeMapSerialize((Map<?, ?>) rawHighCorner)) : null;
        if(this.lowCorner == null) throw new IllegalArgumentException("Low corner is null.");
        if(this.highCorner == null) throw new IllegalArgumentException("High corner is null.");
    }

    public CuboidRegion(Location center, int radius) {
        world = Objects.requireNonNull(center.getWorld());
        lowCorner = new BlockVector(center.getX() - radius, world.getMinHeight(), center.getZ() - radius);
        highCorner = new BlockVector(center.getX() + radius, world.getMaxHeight(), center.getZ() + radius);
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

        return false;
    }

    public int getRadius() {
        return (highCorner.getBlockX() - lowCorner.getBlockX())/2;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("world-id", this.world.getUID().toString());
        map.put("low-corner", this.lowCorner.serialize());
        map.put("high-corner", this.highCorner.serialize());

        return map;
    }
}
