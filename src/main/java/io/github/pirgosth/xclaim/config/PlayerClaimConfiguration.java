package io.github.pirgosth.xclaim.config;

import io.github.pirgosth.liberty.core.api.utils.SerializationUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClaimConfiguration implements ConfigurationSerializable {
    @NotNull
    private final WorldSection worldSection;
    @Getter @NotNull
    private final ClaimConfiguration claimConfiguration;
    @NotNull @Getter @Setter
    private Location home;

    public PlayerClaimConfiguration(@NotNull WorldSection worldSection, @NotNull ClaimConfiguration claimConfiguration, @NotNull Location home) {
        this.worldSection = worldSection;
        this.claimConfiguration = claimConfiguration;
        this.home = home;
    }

    public PlayerClaimConfiguration(@NotNull WorldSection worldSection, Map<String, Object> map) {
        this.worldSection = worldSection;
        Object rawClaimConfigId = map.get("id");
        Object rawHome = map.get("home");
        UUID claimId = (rawClaimConfigId instanceof String) ? UUID.fromString((String) rawClaimConfigId) : null;
        this.home = (rawHome instanceof Map) ? Location.deserialize(SerializationUtils.safeMapSerialize((Map<?, ?>) rawHome)) : Location.deserialize(new HashMap<>());
        this.claimConfiguration = claimId != null ? this.worldSection.getClaimConfigurationById(claimId) : null;
        //TODO: Improve error management.
        if(this.claimConfiguration == null) throw new IllegalArgumentException("Invalid claim configuration for claim id: " + claimId);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.claimConfiguration.getId().toString());
        map.put("home", this.home.serialize());
        return map;
    }
}
