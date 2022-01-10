package io.github.pirgosth.xclaim.cache;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClaimCacheManager implements IPlayerClaimCacheManager {
    private static IPlayerClaimCacheManager instance;

    public static IPlayerClaimCacheManager getInstance() {
        if(instance == null) {
            instance = new PlayerClaimCacheManager();
        }
        return instance;
    }

    private final Map<UUID, IPlayerClaimCache> playerClaimCacheMap;

    public PlayerClaimCacheManager() {
        this.playerClaimCacheMap = new HashMap<>();
    }

    @Override
    @NotNull
    public IPlayerClaimCache getPlayerClaimCache(@NotNull OfflinePlayer player) {
        IPlayerClaimCache playerClaimCache = this.playerClaimCacheMap.get(player.getUniqueId());
        if(playerClaimCache == null) {
            playerClaimCache = new PlayerClaimCache();
            playerClaimCacheMap.put(player.getUniqueId(), playerClaimCache);
        }
        return playerClaimCache;
    }

    @Override
    public boolean updatePlayerClaimCache(@NotNull OfflinePlayer player) {
        return false;
    }


}
