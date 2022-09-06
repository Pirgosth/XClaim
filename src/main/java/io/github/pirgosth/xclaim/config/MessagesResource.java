package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.APluginResource;
import io.github.pirgosth.liberty.core.api.i18n.YamlField;
import io.github.pirgosth.xclaim.XClaim;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessagesResource extends APluginResource {

    @YamlField(key = "restrictions")
    public Map<String, String> restrictions = new LinkedHashMap<>();

    @YamlField(key = "information")
    public Map<String, String> information = new LinkedHashMap<>();

    public MessagesResource() {
        super(XClaim.getInstance(), "messages.yml");
    }

}
