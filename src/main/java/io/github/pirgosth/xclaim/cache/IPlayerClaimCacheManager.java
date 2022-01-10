package io.github.pirgosth.xclaim.cache;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public interface IPlayerClaimCacheManager {

    @NotNull
    IPlayerClaimCache getPlayerClaimCache(@NotNull OfflinePlayer player);

    boolean updatePlayerClaimCache(@NotNull OfflinePlayer player);
}
