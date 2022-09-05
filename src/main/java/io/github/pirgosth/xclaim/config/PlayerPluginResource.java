package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.APluginResource;
import io.github.pirgosth.liberty.core.api.i18n.ResourceContext;
import io.github.pirgosth.liberty.core.api.i18n.YamlField;
import io.github.pirgosth.xclaim.XClaim;

import java.util.ArrayList;
import java.util.List;

public class PlayerPluginResource extends APluginResource {
    @YamlField(key = "players")
    public List<PlayerConfiguration> players = new ArrayList<>();

    public PlayerPluginResource(WorldSection worldSection, String path) {
        super(XClaim.getInstance(), path, ResourceContext.builder().set("world-section", worldSection).build());
    }
}
