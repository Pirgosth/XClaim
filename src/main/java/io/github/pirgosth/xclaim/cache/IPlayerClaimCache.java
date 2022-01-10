package io.github.pirgosth.xclaim.cache;

import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import org.jetbrains.annotations.Nullable;

public interface IPlayerClaimCache {
    boolean isInWild();
    @Nullable ClaimConfiguration getClaim();

}
