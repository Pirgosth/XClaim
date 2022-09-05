package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.APluginResource;
import io.github.pirgosth.liberty.core.api.i18n.YamlField;
import io.github.pirgosth.xclaim.XClaim;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XClaimConfig extends APluginResource {
    @YamlField(key = "maximum-claims-per-player")
    public int claimCountPerPlayer;
    @YamlField(key = "enabled-worlds")
    private List<String> enabledWorlds = new ArrayList<>();
    private final Map<String, WorldSection> worldSections = new HashMap<>();
    private static XClaimConfig instance = null;

    public static XClaimConfig getConfiguration() {
        if (instance == null) XClaimConfig.instance = new XClaimConfig();
        return instance;
    }

    public XClaimConfig() {
        super(XClaim.getInstance(), "config.yml");
    }

    @Override
    public void reload() {
        super.reload();
        worldSections.clear();
        for (String worldName : this.enabledWorlds) {
            this.worldSections.put(worldName, new WorldSection(worldName));
        }
    }

    @Override
    public void save() {
        super.save();
        for (WorldSection section : this.worldSections.values())
            section.save();
    }

    public boolean isWorldEnabled(@NotNull World world) {
        return this.enabledWorlds.contains(world.getName());
    }

    @Nullable
    public WorldSection getWorldSection(@NotNull World world) {
        return this.worldSections.get(world.getName());
    }
}
