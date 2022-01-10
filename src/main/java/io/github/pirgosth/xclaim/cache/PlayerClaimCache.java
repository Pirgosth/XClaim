package io.github.pirgosth.xclaim.cache;

import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import org.jetbrains.annotations.Nullable;

public class PlayerClaimCache implements IPlayerClaimCache{

    @Override
    public boolean isInWild() {
        return true;
    }

    @Override
    public @Nullable ClaimConfiguration getClaim() {
        return null;
    }

}
