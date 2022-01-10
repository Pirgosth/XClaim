package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import io.github.pirgosth.xclaim.XClaim;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class XClaimConfig implements ConfigurationSerializable {
    @Getter
    private final int claimCountPerPlayer;
    private final List<WorldSection> enabledWorlds;
    private static XClaimConfig instance = null;

    public static XClaimConfig getConfiguration() {
        if (instance == null) XClaimConfig.reloadConfiguration();
        return instance;
    }

    public static void reloadConfiguration() {
        instance = new XClaimConfig(XClaim.getInstance().getConfig().getValues(true));
    }

    private List<String> getEnabledWorldNames() {
        List<String> worldNames = new ArrayList<>();
        for(WorldSection section : enabledWorlds) {
            worldNames.add(section.getWorld().getName());
        }
        return worldNames;
    }

    public boolean isWorldEnabled(@NotNull World world) {
        return this.getEnabledWorldNames().contains(world.getName());
    }

    @Nullable
    public WorldSection getWorldSection(@NotNull World world) {
        for (WorldSection section : this.enabledWorlds) {
            if(section.getWorld().equals(world)) return section;
        }
        return null;
    }

    public void save() {
        for (Map.Entry<String, Object> entry : this.serialize().entrySet()) {
            XClaim.getInstance().getConfig().set(entry.getKey(), entry.getValue());
        }
        for (WorldSection section : this.enabledWorlds) {
            section.save();
        }
        XClaim.getInstance().saveConfig();
    }

    public XClaimConfig(Map<String, Object> map) {
        Object rawClaimCountPerPlayer = map.get("maximum-claims-per-player");
        //TODO: Add global defaults for config.
        this.claimCountPerPlayer = (rawClaimCountPerPlayer instanceof Integer) ? (Integer) rawClaimCountPerPlayer : 3;

        this.enabledWorlds = new ArrayList<>();
        Object rawWorlds = map.get("enabled-worlds");
        List<String> enabledWorldNames = (rawWorlds instanceof List) ? SerializationUtils.safeStringListCast((List<?>) rawWorlds) : new ArrayList<>();
        for (String worldName : enabledWorldNames) {
            this.enabledWorlds.add(new WorldSection(worldName));
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new LinkedHashMap<>();
        serialized.put("maximum-claims-per-player", this.claimCountPerPlayer);
        serialized.put("enabled-worlds", this.getEnabledWorldNames());
        return serialized;
    }
}
