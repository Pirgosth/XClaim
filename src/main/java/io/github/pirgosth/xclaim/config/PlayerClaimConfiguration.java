package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.i18n.ResourceContext;
import io.github.pirgosth.liberty.core.api.i18n.YamlContextField;
import io.github.pirgosth.liberty.core.api.i18n.YamlSerializable;
import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerClaimConfiguration extends YamlSerializable {
    @NotNull
    @YamlContextField(key = "world-section")
    private WorldSection worldSection;
    @NotNull
    @Getter
    private ClaimConfiguration claimConfiguration;
    @NotNull
    @Getter
    @Setter
    private Location home;

    public PlayerClaimConfiguration(@NotNull WorldSection worldSection, @NotNull ClaimConfiguration claimConfiguration, @NotNull Location home) {
        this.worldSection = worldSection;
        this.claimConfiguration = claimConfiguration;
        this.home = home;
    }

    public PlayerClaimConfiguration() {
        super();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("id", this.claimConfiguration.getId().toString());
        map.put("home", this.home.serialize());
        return map;
    }

    @Override
    public void deserialize(Map<String, Object> values, ResourceContext context) {
        super.deserialize(values, context);
        Object rawClaimConfigId = values.get("id");
        Object rawHome = values.get("home");

        if (!(rawClaimConfigId instanceof String claimIdUUID))
            return;

        if (!(rawHome instanceof Map homeMap))
            return;

        this.home = Location.deserialize(SerializationUtils.safeMapSerialize(homeMap));
        this.claimConfiguration = this.worldSection.getClaimConfigurationById(UUID.fromString(claimIdUUID));
    }
}
