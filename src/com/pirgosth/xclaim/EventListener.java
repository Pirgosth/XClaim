package com.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.regions.CuboidRegion;

public class EventListener implements Listener{
	//TODO ADD RECENT REGION IN CDS TO AVOID RECALCULATE REGION ON EVERY SINGLE EVENT
	//TODO CLARIFY CODE
	private JavaPlugin plugin = null;

	public EventListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	private boolean cancelInteract(Player player, Location location, String permission, String restriction) {
		ClaimData cd = ClaimData.getInRegion(location, player.getWorld().getName());
		if(cd != null && !player.hasPermission(permission) && !ClaimData.isMemberOf(cd.getNode(), player)) {
			Messages.sendRestriction(restriction, player);
//			main.insufficientPerm(player, perm);
			return true;
		}
		return false;
	}
	
	private boolean cancelInteract(Player player, Location location) {
		ClaimData cd = ClaimData.getInRegion(location, player.getWorld().getName());
		if(cd != null && !ClaimData.isMemberOf(cd.getNode(), player)) {
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		ClaimData.updatePlayerRegion(event.getPlayer());
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		//TODO FIX LEAVING/ENTERING MESSAGES
		ClaimData.updatePlayerRegion(event.getPlayer());
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		//TODO FIX LEAVING/ENTERING MESSAGES
		ClaimData.updatePlayerRegion(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || 
				event.getFrom().getZ() != event.getTo().getZ()) {
			Player player = event.getPlayer();
			ClaimData cd = ClaimData.getPlayerRegion(player);
			if(cd == null) {
				ClaimData.updatePlayerRegion(player);
				if(cd != ClaimData.getPlayerRegion(player)) {
					cd = ClaimData.getPlayerRegion(player);
					//On entering claim event
					event.getPlayer().sendMessage(ChatColor.GRAY+"Welcome to "+ChatColor.GREEN+cd.getOwners()+ChatColor.GRAY+" <"+ChatColor.GREEN+cd.getName()+ChatColor.GRAY+"> claim !");
				}
				else {
					return;
				}
			}
			if(cd != null) {
				CuboidRegion r = cd.getRegion();
				//On leaving claim event
				if(!r.contains(Functions.Location2Vector(player.getLocation()))) {
					player.sendMessage(ChatColor.GRAY+"You're leaving "+ChatColor.YELLOW+cd.getOwners()+ChatColor.GRAY+" <"+ChatColor.YELLOW+cd.getName()+ChatColor.GRAY+"> claim.");
					ClaimData.setPlayerRegion(player, null);
				}
			}
		}
	}
	
	private Set<TntData> Tnts = new HashSet<>();
	
	@EventHandler
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			event.setCancelled(cancelInteract(player, event.getItem().getLocation(), "xclaim.others.items.pickup", "on-others-items-pickup"));
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(cancelInteract(player, player.getLocation(), "xclaim.others.items.drop", "on-others-items-drop"));
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(cancelInteract(event.getPlayer(), event.getBlock().getLocation(), "xclaim.others.blocks.break", "on-others-blocks-break"));
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.TNT) {
			Tnts.add(new TntData(event.getBlock().getLocation(), event.getPlayer()));
		}
		event.setCancelled(cancelInteract(event.getPlayer(), event.getBlock().getLocation(), "xclaim.others.blocks.place", "on-others-blocks-place"));
	}
	
	@EventHandler
	public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
		event.setCancelled(cancelInteract(event.getPlayer(), event.getEntity().getLocation(), "xclaim.others.entity.leash", "on-others-entity-leash"));
		event.getPlayer().updateInventory();
	}
	
	@EventHandler
	public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();
		boolean isCancelled = cancelInteract(player, event.getEntity().getLocation(), "xclaim.others.entity.unleash", "on-others-entity-unleash");
		if(!isCancelled) {
			LivingEntity entity = (LivingEntity)event.getEntity();
			entity.setLeashHolder(null);
			player.getInventory().addItem(new ItemStack(Material.LEAD));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getHand() == EquipmentSlot.HAND) return;
		if(event.getAction() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			event.setCancelled(cancelInteract(event.getPlayer(), event.getClickedBlock().getLocation(), "xclaim.others.interact", "on-others-interact"));
//			event.getPlayer().sendMessage("PlayerInteract event");	
		}
	}
	
	@EventHandler
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
		//TODO ADD ARMOR STAND MANAGEMENT
		event.setCancelled(cancelInteract(event.getPlayer(), event.getRightClicked().getLocation(), "xclaim.others.interact.armorStand", "on-others-interact"));
//		event.getPlayer().sendMessage("PlayerArmorStandManipulate event");
	}
	
	@EventHandler
	public void onHangingPlace(HangingPlaceEvent event) {
		event.setCancelled(cancelInteract(event.getPlayer(), event.getEntity().getLocation(), "xclaim.others.interact.paintings.place", "on-others-interact"));
		event.getPlayer().updateInventory();
	}
	
	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		//TODO ADD HANGING BREAK MANAGEMENT
		if(event.getRemover() instanceof Player) {
			event.setCancelled(cancelInteract((Player)(event).getRemover(), event.getEntity().getLocation(), "xclaim.others.interact.paintings.break", "on-others-interact"));	
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.LEAD) {
			return;
		}
		Entity entity = event.getRightClicked();
		event.setCancelled(cancelInteract(event.getPlayer(), entity.getLocation(), "xclaim.others.interact", "on-others-interact"));
//		event.getPlayer().sendMessage("PlayerInteractEntity event");
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
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
	public void onBlockExplodedEvent(EntityExplodeEvent event) {
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
		if(event.getDamager() instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed)event.getDamager();
			ClaimData cd = ClaimData.getInRegion(event.getEntity().getLocation(), event.getEntity().getWorld().getName());
			if(cd != null) {
				if(tnt.hasMetadata("player")) {
					Player player = Bukkit.getPlayer(tnt.getMetadata("player").get(0).asString());
					if(player != null && player.hasPermission("xclaim.others.tnt.damage.entity")) {
						return;
					}
					else {
						event.setCancelled(cancelInteract(player, event.getEntity().getLocation()));
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
				event.setCancelled(cancelInteract(damager, entity.getLocation(), "xclaim.others.entity.hit", "on-others-entity-hit"));
			}
		}
	}
	
}
