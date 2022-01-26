package io.github.pirgosth.xclaim;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TntData {
    @Getter @NotNull
    private final Location location;
    @Getter
    private final Player player;

    public TntData(@NotNull Location location, Player player) {
        this.location = location;
        this.player = player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TntData tntData = (TntData) o;
        return Objects.equals(location.getBlockX(), tntData.location.getBlockX())
                && Objects.equals(location.getBlockY(), tntData.location.getBlockY())
                && Objects.equals(location.getBlockZ(), tntData.location.getBlockZ());
    }

    @Override
    public int hashCode() {
        return Objects.hash(new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}