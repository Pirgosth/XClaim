package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.ResourceContext;
import io.github.pirgosth.liberty.core.api.i18n.YamlField;
import io.github.pirgosth.liberty.core.api.i18n.YamlSerializable;
import io.github.pirgosth.xclaim.math.CuboidRegion;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimConfiguration extends YamlSerializable {
    @Getter
    private UUID id;
    @YamlField(key = "name")
    public String name;
    @Getter
    @YamlField(key = "members")
    private List<ClaimMember> members;
    @Getter
    @YamlField(key = "region")
    private CuboidRegion region;

    public ClaimConfiguration(Player player, String name, @NotNull CuboidRegion region) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.region = region;
        this.members = new ArrayList<>(List.of(new ClaimMember(player, ClaimMember.Role.Owner)));
    }

    // Empty constructor used for deserialization.
    public ClaimConfiguration() {
        super();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("id", this.id.toString());
        return map;
    }

    @Override
    public void deserialize(Map<String, Object> values, ResourceContext context) {
        super.deserialize(values, context);
        Object rawClaimConfigId = values.get("id");

        if (!(rawClaimConfigId instanceof String claimIdUUID))
            return;

        this.id = UUID.fromString(claimIdUUID);
    }

    @Nullable
    public ClaimMember getMember(OfflinePlayer player) {
        for (ClaimMember member : this.members) {
            if (member.getSpigotPlayer().getUniqueId().equals(player.getUniqueId())) return member;
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
}
