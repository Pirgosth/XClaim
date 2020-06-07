package io.github.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.regions.CuboidRegion;

public class EventListener implements Listener{
	//TODO ADD RECENT REGION IN CDS TO AVOID RECALCULATE REGION ON EVERY SINGLE EVENT
	//TODO CLARIFY CODE
	private JavaPlugin plugin = null;
	
	private Map<UUID, Long> messageTimeouts = new HashMap<>();

	public EventListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	private boolean canSendMessage(Player player, long timeout) {
		if(!this.messageTimeouts.containsKey(player.getUniqueId())) {
			this.messageTimeouts.put(player.getUniqueId(), System.currentTimeMillis());
			return true;
		}
		long lastTime = this.messageTimeouts.get(player.getUniqueId());
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastTime > timeout) {
			this.messageTimeouts.put(player.getUniqueId(), currentTime);
			return true;
		}
		return false;
	}
	
	private boolean cancelEvent(Player player, Location location) {
		ClaimData cd = ClaimData.getInRegion(location, player.getWorld().getName());
		if(cd != null && !cd.isOwnerOf(player) && !cd.isMemberOf(player)) {
			return true;
		}
		return false;
	}
	
	private boolean cancelEvent(Player player, Location location, String permission, String restriction) {
		boolean cancel = cancelEvent(player, location);
		if(cancel && !player.hasPermission(permission)) {
			if(this.canSendMessage(player, 2000)) {
				Messages.sendRestriction(restriction, player);
			}
			return true;
		}
		return false;
	}
	
	private boolean isWorldEnabled(Entity entity) {
		return XClaim.worlds.get(entity.getWorld().getName());
	}
	
	private boolean isWorldEnabled(Block block) {
		return XClaim.worlds.get(block.getWorld().getName());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
			Player player = event.getPlayer();
			ClaimData cd = ClaimData.getPlayerRegion(player);
			if(cd == null) {
				ClaimData.updatePlayerRegion(player);
				cd = ClaimData.getPlayerRegion(player);
				if(cd != null) {
					//On entering claim event
					Messages.sendInformation("on-claim-entry", player, cd.getName(), cd.getOwners().toString());
					return;
				}
			}
			else{
				CuboidRegion r = cd.getRegion();
				//On leaving claim event
				if(!r.contains(Functions.Location2Vector(player.getLocation()))) {//If no more in old claim
					Messages.sendInformation("on-claim-exit", player, cd.getName(), cd.getOwners().toString());
					ClaimData.setPlayerRegion(player, null);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		ClaimData.updatePlayerRegion(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		//TODO FIX LEAVING/ENTERING MESSAGES
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		ClaimData.updatePlayerRegion(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		//TODO FIX LEAVING/ENTERING MESSAGES
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		ClaimData.updatePlayerRegion(event.getPlayer(), event.getTo());
	}
	
	private Set<TntData> Tnts = new HashSet<>();
	
	@EventHandler
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			if(cancelEvent(player, event.getItem().getLocation(), "xclaim.others.items.pickup", "on-others-items-pickup")) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		Player player = event.getPlayer();
		if(cancelEvent(player, player.getLocation(), "xclaim.others.items.drop", "on-others-items-drop")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(!isWorldEnabled(event.getBlock())) {
			return;
		}
		if(cancelEvent(event.getPlayer(), event.getBlock().getLocation(), "xclaim.others.blocks.break", "on-others-blocks-break")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!isWorldEnabled(event.getBlock())) {
			return;
		}
		if(event.getBlock().getType() == Material.TNT) {
			Tnts.add(new TntData(event.getBlock().getLocation(), event.getPlayer()));
		}
		if(cancelEvent(event.getPlayer(), event.getBlock().getLocation(), "xclaim.others.blocks.place", "on-others-blocks-place")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		if(cancelEvent(event.getPlayer(), event.getEntity().getLocation(), "xclaim.others.entity.leash", "on-others-entity-leash")) {
			event.setCancelled(true);
		}
		event.getPlayer().updateInventory();
	}
	
	@EventHandler
	public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		event.setCancelled(true);
		Player player = event.getPlayer();
		boolean isCancelled = cancelEvent(player, event.getEntity().getLocation(), "xclaim.others.entity.unleash", "on-others-entity-unleash");
		if(!isCancelled) {
			LivingEntity entity = (LivingEntity)event.getEntity();
			entity.setLeashHolder(null);
			player.getInventory().addItem(new ItemStack(Material.LEAD));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		//if(event.getHand() == EquipmentSlot.HAND) return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			if(cancelEvent(event.getPlayer(), event.getClickedBlock().getLocation(), "xclaim.others.interact", "on-others-interact")) {
				event.setCancelled(true);
			}
//			event.getPlayer().sendMessage("PlayerInteract event");	
		}
		else if(event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.FARMLAND) {
			if(cancelEvent(event.getPlayer(), event.getClickedBlock().getLocation(), "xclaim.others.farmland.break", "on-other-farmland-break")) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		//TODO ADD ARMOR STAND MANAGEMENT
		if(cancelEvent(event.getPlayer(), event.getRightClicked().getLocation(), "xclaim.others.interact.armorStand", "on-others-interact")) {
			event.setCancelled(true);
		}
//		event.getPlayer().sendMessage("PlayerArmorStandManipulate event");
	}
	
	@EventHandler
	public void onHangingPlace(HangingPlaceEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		if(cancelEvent(event.getPlayer(), event.getEntity().getLocation(), "²", "on-others-interact")) {
			event.setCancelled(true);
		}
		event.getPlayer().updateInventory();
	}
	
	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		//TODO ADD HANGING BREAK MANAGEMENT
		if(event.getRemover() instanceof Player) {
			if(cancelEvent((Player)(event).getRemover(), event.getEntity().getLocation(), "xclaim.others.interact.paintings.break", "on-others-interact")) {
				event.setCancelled(true);	
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(!isWorldEnabled(event.getPlayer())) {
			return;
		}
		if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.LEAD) {
			return;
		}
		Entity entity = event.getRightClicked();
		if(cancelEvent(event.getPlayer(), entity.getLocation(), "xclaim.others.interact", "on-others-interact")) {
			event.setCancelled(true);
		}
//		event.getPlayer().sendMessage("PlayerInteractEntity event");
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		if(event.getEntity().getType() == EntityType.PRIMED_TNT) {
			TntData tnt = TntData.getTntData(Tnts, event.getLocation());
			if(tnt != null) {
//				tnt.getPlayer().sendMessage(ChatColor.DARK_AQUA+"Your tnt had been ignited !");
				event.getEntity().setMetadata("player", new FixedMetadataValue(plugin, tnt.getPlayer().getName()));
				Tnts.remove(tnt);
			}
		}
	}
	
	@EventHandler
	public void onPlayerShootBow(EntityShootBowEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		if(cancelEvent(player, player.getLocation(), "xclaim.others.bow", "on-other-bow")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreeperExplode(ExplosionPrimeEvent event) {
		if(!(event.getEntity() instanceof Creeper)) {
			return;
		}
		Creeper creeper = (Creeper) event.getEntity();
		if(!(creeper.getTarget() instanceof Player)) {
			return;
		}
		Player player = (Player) creeper.getTarget();
		if(cancelEvent(player, creeper.getLocation())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockExplodedEvent(EntityExplodeEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		if(event.getEntity() instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed) event.getEntity();
			String playerName = null;
			if(tnt.hasMetadata("player")) {
				playerName = new String(tnt.getMetadata("player").get(0).asString());
				Player player = Bukkit.getPlayer(playerName);
				if(player != null && player.hasPermission("xclaim.others.tnt.damage.blocks")) {
					return;
				}
			}
			ClaimData cd = null;
			for(Block block: new ArrayList<Block>(event.blockList())) {
				if(block.getType() == Material.TNT) {
					continue;
				}
				if(cd == null || !cd.getRegion().contains(Functions.Location2Vector(block.getLocation()))) {
					cd = ClaimData.getInRegion(block.getLocation(), block.getWorld().getName());
				}
				if(cd != null) {
					if(playerName != null && Functions.containsIgnoringCase(cd.getOwners(), playerName)) {
						continue;
					}
					event.blockList().remove(block);
				}
			}
		}
	}
	
	@EventHandler
	public void onDamagedEntity(EntityDamageByEntityEvent event) {
		if(!isWorldEnabled(event.getEntity())) {
			return;
		}
		if(event.getDamager() instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed)event.getDamager();
			ClaimData cd = ClaimData.getInRegion(event.getEntity().getLocation(), event.getEntity().getWorld().getName());
			if(cd != null) {
				if(tnt.hasMetadata("player")) {
					Player player = Bukkit.getPlayer(tnt.getMetadata("player").get(0).asString());
					if(player != null && player.hasPermission("xclaim.others.tnt.damage.entity")) {
						return;
					}
					else if(cancelEvent(player, event.getEntity().getLocation())){
						event.setCancelled(true);
					}
				}
				event.setCancelled(true);
			}
			else {
				return;
			}
		}
		else if(event.getDamager() instanceof Player) {
			Entity entity = event.getEntity();
			Player damager = (Player)event.getDamager();
			if(!damager.hasPermission("xclaim.others.entity.hit")) {
				if(cancelEvent(damager, entity.getLocation(), "xclaim.others.entity.hit", "on-others-entity-hit")) {
					event.setCancelled(true);
				}
			}
		}
		
		else if(event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile)event.getDamager();
			if(projectile.getShooter() == null || !(projectile.getShooter() instanceof Player)) {
				return;
			}
			Player shooter = (Player) projectile.getShooter();
			if(cancelEvent(shooter, event.getEntity().getLocation(), "xclaim.others.entity.arrow", "on-others-entity-arrow")) {
				event.setCancelled(true);
			}
		}
	}
	
}
