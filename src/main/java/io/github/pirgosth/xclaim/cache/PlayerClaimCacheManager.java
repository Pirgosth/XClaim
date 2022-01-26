package io.github.pirgosth.xclaim.cache;

import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import io.github.pirgosth.xclaim.config.WorldSection;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClaimCacheManager implements IPlayerClaimCacheManager {
    private static IPlayerClaimCacheManager instance;

    public static IPlayerClaimCacheManager getInstance() {
        if (instance == null) {
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
    public IPlayerClaimCache getPlayerClaimCache(@NotNull Player player) {
        IPlayerClaimCache playerClaimCache = this.playerClaimCacheMap.get(player.getUniqueId());
        if (playerClaimCache == null) {
            playerClaimCache = new PlayerClaimCache();
            playerClaimCacheMap.put(player.getUniqueId(), playerClaimCache);
        }
        return playerClaimCache;
    }

    @Override
    public boolean updatePlayerClaimCache(@NotNull Player player, boolean force) {
        IPlayerClaimCache playerClaimCache = this.getPlayerClaimCache(player);

        ClaimConfiguration oldClaim = playerClaimCache.getClaim();

        WorldSection worldSection = XClaimConfig.getConfiguration().getWorldSection(player.getWorld());
        if (worldSection == null) {
            playerClaimCache.setClaim(null);
            return oldClaim == null;
        }

        if(!force && oldClaim != null && oldClaim.getRegion().contains(player)) {
            return false;
        }

        playerClaimCache.setClaim(worldSection.getClaimConfigurationByLocation(player.getLocation()));
        return true;
    }

    @Override
    public boolean updatePlayerClaimCache(@NotNull Player player) {
        return this.updatePlayerClaimCache(player, false);
    }

    @Override
    public void updateOnlinePlayersClaimCache(boolean force) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.updatePlayerClaimCache(onlinePlayer, force);
        }
    }
}
