package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.ResourceContext;
import io.github.pirgosth.liberty.core.api.i18n.YamlSerializable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimMember extends YamlSerializable {

    public enum Role {
        Member,
        Owner
    }

    @Getter
    private OfflinePlayer spigotPlayer;
    @Getter
    @Setter
    private Role role;

    public ClaimMember(OfflinePlayer player, Role role) {
        this.spigotPlayer = player;
        this.role = role;
    }

    public ClaimMember() {
        super();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialize = new HashMap<>();
        serialize.put("player", this.spigotPlayer.getUniqueId().toString());
        serialize.put("role", this.role.ordinal());
        return serialize;
    }

    @Override
    public void deserialize(Map<String, Object> values, ResourceContext context) {
        if (!(values.get("player") instanceof String rawSpigotPlayer))
            throw new IllegalArgumentException("Invalid type for player!");
        if (!(values.get("role") instanceof Integer rawRole))
            throw new IllegalArgumentException("Invalid type for player!");

        this.spigotPlayer = Bukkit.getOfflinePlayer(UUID.fromString(rawSpigotPlayer));
        this.role = Role.values()[rawRole];
    }
}
