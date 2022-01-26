package io.github.pirgosth.xclaim.cache;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IPlayerClaimCacheManager {

    @NotNull
    IPlayerClaimCache getPlayerClaimCache(@NotNull Player player);
    boolean updatePlayerClaimCache(@NotNull Player player, boolean force);
    boolean updatePlayerClaimCache(@NotNull Player player);
    void updateOnlinePlayersClaimCache(boolean force);
}
