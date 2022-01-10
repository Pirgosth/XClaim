package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import io.github.pirgosth.xclaim.math.CuboidRegion;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimConfiguration implements ConfigurationSerializable {
    @Getter
    private final UUID id;
    @Getter
    private final String name;
    @Getter
    private final List<ClaimMember>  members;
    @Getter @NotNull
    private final CuboidRegion region;

    public ClaimConfiguration(Player player, String name, @NotNull CuboidRegion region) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.region = region;
        this.members = new ArrayList<>(List.of(new ClaimMember(player, ClaimMember.Role.Owner)));
    }

    public ClaimConfiguration(Map<String, Object> map) {
        Object rawId = map.get("id");
        Object rawName = map.get("name");
        Object rawMembers = map.get("members");
        Object rawRegion = map.get("region");

        this.id = (rawId instanceof String) ? UUID.fromString((String) rawId) : null;
        this.name = (rawName instanceof String) ? (String) rawName : "Invalid Name";
        this.members = (rawMembers instanceof List) ? SerializationUtils.safeListCast(ClaimMember.class, (List<?>) rawMembers) : new ArrayList<>();
        this.region = (rawRegion instanceof Map) ? new CuboidRegion(SerializationUtils.safeMapSerialize((Map<?, ?>) rawRegion)) : null;
        if(this.region == null) throw new IllegalArgumentException("Region is null in deserialization.");
    }

    @Nullable
    public ClaimMember getMember(OfflinePlayer player) {
        for (ClaimMember member : this.members) {
            if (member.getSpigotPlayer().equals(player)) return member;
        }
        return null;
    }

    @Nullable
    public ClaimMember getMember(String name) {
        for (ClaimMember member : this.members) {
            if (Objects.equals(member.getSpigotPlayer().getName(), name)) return member;
        }
        return null;
    }

    public boolean isMember(OfflinePlayer player) {
        return this.getMember(player) != null;
    }

    public List<ClaimMember> getOwners() {
        List<ClaimMember> owners = new ArrayList<>();
        for (ClaimMember member : this.members) {
            if (member.getRole().equals(ClaimMember.Role.Owner)) owners.add(member);
        }
        return owners;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        List<Map<String, Object>> serializedMembers = new ArrayList<>();

        for(ClaimMember member : this.members) {
            serializedMembers.add(member.serialize());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id.toString());
        map.put("name", this.name);
        map.put("members", serializedMembers);
        map.put("region", this.region.serialize());

        return map;
    }
}
