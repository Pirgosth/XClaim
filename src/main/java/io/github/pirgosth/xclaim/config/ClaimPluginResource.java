package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.APluginResource;
import io.github.pirgosth.liberty.core.api.i18n.YamlField;
import io.github.pirgosth.xclaim.XClaim;

import java.util.ArrayList;
import java.util.List;

public class ClaimPluginResource extends APluginResource {
    @YamlField(key = "claims")
    public List<ClaimConfiguration> claims = new ArrayList<>();

    public ClaimPluginResource(String path) {
        super(XClaim.getInstance(), path);
    }
}
