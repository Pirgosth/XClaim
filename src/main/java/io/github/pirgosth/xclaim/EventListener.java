package io.github.pirgosth.xclaim;

import io.github.pirgosth.liberty.core.api.utils.ChatUtils;
import io.github.pirgosth.xclaim.cache.IPlayerClaimCache;
import io.github.pirgosth.xclaim.cache.PlayerClaimCacheManager;
import io.github.pirgosth.xclaim.config.ClaimConfiguration;
import io.github.pirgosth.xclaim.config.WorldSection;
import io.github.pirgosth.xclaim.config.XClaimConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockVector;

import java.util.*;

public class EventListener implements Listener {
    //TODO ADD RECENT REGION IN CDS TO AVOID RECALCULATE REGION ON EVERY SINGLE EVENT
    //TODO CLARIFY CODE

    private final Map<UUID, Long> messageTimeouts = new HashMap<>();

    private boolean canSendMessage(Player player) {
        if (!this.messageTimeouts.containsKey(player.getUniqueId())) {
            this.messageTimeouts.put(player.getUniqueId(), System.currentTimeMillis());
            return true;
        }
        long lastTime = this.messageTimeouts.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 2000L) {
            this.messageTimeouts.put(player.getUniqueId(), currentTime);
            return true;
        }
        return false;
    }

    private boolean cancelEvent(Player player, Location location) {
        //TODO: Store interactions with claims in playerCache (List of recent claims) and search for them before searching in all claims.
        if (location.getWorld() == null) return false;
        WorldSection worldSection = XClaimConfig.getConfiguration().getWorldSection(location.getWorld());
        if (worldSection == null) return false;

        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(location);
        return claimConfiguration != null && !claimConfiguration.isMember(player);
    }

    private boolean cancelEvent(Player player, Location location, String permission, String restriction) {
        boolean cancel = this.cancelEvent(player, location);
        if (cancel && !player.hasPermission(permission)) {
            if (this.canSendMessage(player)) {
                ChatUtils.sendColorMessage(player, String.format("You can't do that here: %s", restriction));
            }
            return true;
        }
        return false;
    }

    private boolean isWorldDisabled(World world) {
        if (world == null) return true;
        return XClaimConfig.getConfiguration().getWorldSection(world) == null;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.isWorldDisabled(event.getPlayer().getWorld())) return;
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            Player player = event.getPlayer();
            IPlayerClaimCache playerClaimCache = PlayerClaimCacheManager.getInstance().getPlayerClaimCache(player);
            ClaimConfiguration oldClaimConfiguration = playerClaimCache.getClaim();

            if (oldClaimConfiguration == null || !oldClaimConfiguration.getRegion().contains(player)) {
                if (oldClaimConfiguration != null) {
                    ChatUtils.sendColorMessage(player, String.format("&7Leaving &a%s &7claim.", oldClaimConfiguration.name));
                }

                PlayerClaimCacheManager.getInstance().updatePlayerClaimCache(player);
                ClaimConfiguration freshClaimConfiguration = playerClaimCache.getClaim();
                if (freshClaimConfiguration != null) {
                    ChatUtils.sendColorMessage(player, String.format("&7Entering &a%s &7claim.", freshClaimConfiguration.name));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.isWorldDisabled(event.getPlayer().getWorld())) return;
        PlayerClaimCacheManager.getInstance().updatePlayerClaimCache(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        //TODO FIX LEAVING/ENTERING MESSAGES
        if (isWorldDisabled(event.getPlayer().getWorld())) return;
        PlayerClaimCacheManager.getInstance().updatePlayerClaimCache(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        //TODO FIX LEAVING/ENTERING MESSAGES
        if (isWorldDisabled(event.getPlayer().getWorld())) return;
        PlayerClaimCacheManager.getInstance().updatePlayerClaimCache(event.getPlayer());
    }

    private final HashMap<BlockVector, Player> Tnts = new HashMap<>();

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) return;
        if (event.getEntity() instanceof Player player && cancelEvent(player, event.getItem().getLocation(), "xclaim.others.items.pickup", "on-others-items-pickup")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isWorldDisabled(event.getPlayer().getWorld())) return;
        Player player = event.getPlayer();
        if (cancelEvent(player, player.getLocation(), "xclaim.others.items.drop", "on-others-items-drop")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isWorldDisabled(event.getBlock().getWorld())) return;
        if (cancelEvent(event.getPlayer(), event.getBlock().getLocation(), "xclaim.others.blocks.break", "on-others-blocks-break")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isWorldDisabled(event.getBlock().getWorld())) return;
        if (event.getBlock().getType() == Material.TNT) {
            Location location = event.getBlock().getLocation();
            Tnts.put(new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), event.getPlayer());
        }
        if (cancelEvent(event.getPlayer(), event.getBlock().getLocation(), "xclaim.others.blocks.place", "on-others-blocks-place")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) return;
        if (cancelEvent(event.getPlayer(), event.getEntity().getLocation(), "xclaim.others.entity.leash", "on-others-entity-leash")) {
            event.setCancelled(true);
        }
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) return;
        Player player = event.getPlayer();
        boolean isCancelled = cancelEvent(player, event.getEntity().getLocation(), "xclaim.others.entity.unleash", "on-others-entity-unleash");
        if (!isCancelled) {
            event.setCancelled(true);
            LivingEntity entity = (LivingEntity) event.getEntity();
            entity.setLeashHolder(null);
            player.getInventory().addItem(new ItemStack(Material.LEAD));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isWorldDisabled(event.getPlayer().getWorld())) return;
        //if(event.getHand() == EquipmentSlot.HAND) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (cancelEvent(event.getPlayer(), event.getClickedBlock().getLocation(), "xclaim.others.interact", "on-others-interact")) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.FARMLAND) {
            if (cancelEvent(event.getPlayer(), event.getClickedBlock().getLocation(), "xclaim.others.farmland.break", "on-other-farmland-break")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (isWorldDisabled(event.getPlayer().getWorld())) return;
        //TODO ADD ARMOR STAND MANAGEMENT
        if (cancelEvent(event.getPlayer(), event.getRightClicked().getLocation(), "xclaim.others.interact.armorStand", "on-others-interact")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.getPlayer() != null && isWorldDisabled(event.getPlayer().getWorld())) return;
        if (cancelEvent(event.getPlayer(), event.getEntity().getLocation(), "xclaim.others.interact.paintings.place", "on-others-interact")) {
            event.setCancelled(true);
        }
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) {
            return;
        }
        //TODO ADD HANGING BREAK MANAGEMENT
        if (event.getRemover() instanceof Player) {
            if (cancelEvent((Player) (event).getRemover(), event.getEntity().getLocation(), "xclaim.others.interact.paintings.break", "on-others-interact")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isWorldDisabled(event.getPlayer().getWorld())) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.LEAD) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (cancelEvent(event.getPlayer(), entity.getLocation(), "xclaim.others.interact", "on-others-interact")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) return;
        if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
            Location location = event.getLocation();
            BlockVector vector = new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Player tntPrimer = Tnts.get(vector);
            if (tntPrimer != null) {
//				tnt.getPlayer().sendMessage(ChatColor.DARK_AQUA+"Your tnt had been ignited !");
                event.getEntity().setMetadata("player", new FixedMetadataValue(XClaim.getInstance(), tntPrimer.getName()));
                Tnts.remove(vector);
            }
        }
    }

    @EventHandler
    public void onPlayerShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player) || isWorldDisabled(player.getWorld())) return;
        if (cancelEvent(player, player.getLocation(), "xclaim.others.bow", "on-other-bow")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreeperExplode(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper) || isWorldDisabled(creeper.getWorld())) return;
        if (creeper.getTarget() instanceof Player player && cancelEvent(player, creeper.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplodedEvent(EntityExplodeEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) return;
        if (event.getEntity() instanceof TNTPrimed tnt) {
            Player tntDamager = null;

            if (tnt.hasMetadata("player")) {
                String playerName = tnt.getMetadata("player").get(0).asString();
                tntDamager = Bukkit.getPlayer(playerName);
            }

            if (tntDamager != null && tntDamager.hasPermission("xclaim.others.tnt.damage.blocks")) {
                return;
            }

            WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(tnt.getWorld()));
            ClaimConfiguration claimConfiguration = null;
            for (Block block : new ArrayList<>(event.blockList())) {
                if (block.getType() == Material.TNT) continue;
                if (claimConfiguration == null || !claimConfiguration.getRegion().contains(block.getLocation())) {
                    claimConfiguration = worldSection.getClaimConfigurationByLocation(block.getLocation());
                }
                if (claimConfiguration != null) {
                    if (tntDamager == null || !claimConfiguration.isMember(tntDamager)) {
                        event.blockList().remove(block);
                    }
                }
            }
        } else if (event.getEntity() instanceof ExplosiveMinecart minecart) {
            WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(minecart.getWorld()));
            ClaimConfiguration claimConfiguration = null;
            for (Block block : new ArrayList<>(event.blockList())) {
                if (block.getType() == Material.TNT) continue;
                if (claimConfiguration == null || !claimConfiguration.getRegion().contains(block.getLocation())) {
                    claimConfiguration = worldSection.getClaimConfigurationByLocation(block.getLocation());
                }
                if (claimConfiguration != null) {
                    event.blockList().remove(block);
                }
            }
        } else if (event.getEntity() instanceof Creeper creeper && creeper.getTarget() instanceof Player target) {
            WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(creeper.getWorld()));
            ClaimConfiguration claimConfiguration = null;
            for (Block block : new ArrayList<>(event.blockList())) {
                if (block.getType() == Material.TNT) continue;
                if (claimConfiguration == null || !claimConfiguration.getRegion().contains(block.getLocation())) {
                    claimConfiguration = worldSection.getClaimConfigurationByLocation(block.getLocation());
                }
                if (claimConfiguration != null) {
                    if (!claimConfiguration.isMember(target)) {
                        event.blockList().remove(block);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamagedEntity(EntityDamageByEntityEvent event) {
        if (isWorldDisabled(event.getEntity().getWorld())) {
            return;
        }
        if (event.getDamager() instanceof TNTPrimed tnt) {
            WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(tnt.getWorld()));
            ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(event.getEntity().getLocation());

            //If the target is in wild, we have nothing to hande
            if (claimConfiguration == null) return;
            //If the target is in a claim and the tnt cause is unknown, we must cancel this event.
            if (!tnt.hasMetadata("player")) {
                event.setCancelled(true);
                return;
            }

            Player tntDamager = Bukkit.getPlayer(tnt.getMetadata("player").get(0).asString());

            if (tntDamager == null || !tntDamager.hasPermission("xclaim.others.tnt.damage.entity") || cancelEvent(tntDamager, event.getEntity().getLocation()))
                event.setCancelled(true);

        } else if (event.getDamager() instanceof Player damager) {
            Entity entity = event.getEntity();
            if (!damager.hasPermission("xclaim.others.entity.hit")) {
                if (cancelEvent(damager, entity.getLocation(), "xclaim.others.entity.hit", "on-others-entity-hit")) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            if (cancelEvent(shooter, event.getEntity().getLocation(), "xclaim.others.entity.arrow", "on-others-entity-arrow")) {
                event.setCancelled(true);
            }
        } else if (event.getDamager() instanceof Creeper creeper && creeper.getTarget() instanceof Player player) {
            WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(creeper.getWorld()));
            ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(event.getEntity().getLocation());
            if (claimConfiguration != null && !claimConfiguration.isMember(player)) {
                event.setCancelled(true);
            }
        } else if (event.getDamager() instanceof ExplosiveMinecart minecart) {
            WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(minecart.getWorld()));
            ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(event.getEntity().getLocation());
            if (claimConfiguration != null) {
                event.setCancelled(true);
            }
        }
    }

}
