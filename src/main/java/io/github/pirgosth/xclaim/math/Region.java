package io.github.pirgosth.xclaim.math;

import io.github.pirgosth.liberty.core.api.i18n.YamlSerializable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public abstract class Region extends YamlSerializable {
    abstract boolean contains(Location location);

    public boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }

    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    abstract boolean overlap(Region region);
}
