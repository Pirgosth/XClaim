package io.github.pirgosth.xclaim.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimMember implements ConfigurationSerializable {

    public enum Role {
        Member,
        Owner
    }

    @Getter
    private final OfflinePlayer spigotPlayer;
    @Getter @Setter
    private Role role;

    public ClaimMember(OfflinePlayer player, Role role) {
        this.spigotPlayer = player;
        this.role = role;
    }

    public ClaimMember(Map<String, Object> map) {
        Object rawSpigotPlayer = map.get("player");
        Object rawRole = map.get("role");

        this.spigotPlayer = (rawSpigotPlayer instanceof String) ? Bukkit.getOfflinePlayer(UUID.fromString((String) rawSpigotPlayer)) : null;
        this.role = (rawRole instanceof Integer) ? Role.values()[(Integer) rawRole] : Role.Member;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialize = new HashMap<>();
        serialize.put("player", this.spigotPlayer.getUniqueId().toString());
        serialize.put("role", this.role.ordinal());
        return serialize;
    }
}
