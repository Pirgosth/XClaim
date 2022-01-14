package io.github.pirgosth.xclaim.cache;

import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import org.jetbrains.annotations.Nullable;

public class PlayerClaimCache implements IPlayerClaimCache{
    private ClaimConfiguration standingClaim;

    @Override
    public boolean isInWild() {
        return this.standingClaim == null;
    }

    @Override
    public @Nullable ClaimConfiguration getClaim() {
        return this.standingClaim;
    }

    @Override
    public void setClaim(ClaimConfiguration claimConfiguration) {
        this.standingClaim = claimConfiguration;
    }
}
