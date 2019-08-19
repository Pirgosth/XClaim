package com.pirgosth.claimPlugin;

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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.regions.CuboidRegion;

public class EventListener implements Listener{
	//TODO ADD RECENT REGION IN CDS TO AVOID RECALCULATE REGION ON EVERY SINGLE EVENT
	
	private JavaPlugin plugin = null;

	public EventListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	private boolean cancelInteract(Player player, Location location, String perm) {
		ClaimData cdB = ClaimData.getInRegion(location);
		if(cdB != null && !player.hasPermission(perm) && !main.containsIgnoringCase(main.claimsYml.get().getStringList("regions."+cdB.getNode()+".owners"), player.getName())) {
			main.insufficientPerm(player, perm);
			return true;
		}
		return false;
	}
	
	private boolean cancelInteract(Player player, Location location) {
		ClaimData cdB = ClaimData.getInRegion(location);
		if(cdB != null && !main.containsIgnoringCase(main.claimsYml.get().getStringList("regions."+cdB.getNode()+".owners"), player.getName())) {
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(main.cds.get(event.getPlayer().getName()) == null) {
			main.cds.put(event.getPlayer().getName(), ClaimData.getInRegion(event.getPlayer().getLocation()));
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		ClaimData cd = main.cds.get(event.getPlayer().getName());
		if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || 
				event.getFrom().getZ() != event.getTo().getZ()) {
			if(cd == null) {
				cd = ClaimData.getInRegion(event.getPlayer().getLocation());
				if(cd != null) {
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
				if(!r.contains(main.Location2Vector(event.getTo())) && r.contains(main.Location2Vector(event.getFrom()))) {
					event.getPlayer().sendMessage(ChatColor.GRAY+"You're leaving "+ChatColor.YELLOW+cd.getOwners()+ChatColor.GRAY+" <"+ChatColor.YELLOW+cd.getName()+ChatColor.GRAY+"> claim.");
					cd = null;
				}
				main.cds.put(event.getPlayer().getName(), cd);
			}
		}
	}
	
	private Set<TntData> Tnts = new HashSet<>();
	
	@EventHandler
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			event.setCancelled(cancelInteract(player, event.getItem().getLocation(), "claimPlugin.others.items.pickup"));
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(cancelInteract(player, player.getLocation(), "claimPlugin.others.items.drop"));
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(cancelInteract(event.getPlayer(), event.getBlock().getLocation(), "claimPlugin.others.blocks.break"));
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.TNT) {
			Tnts.add(new TntData(event.getBlock().getLocation(), event.getPlayer()));
		}
		event.setCancelled(cancelInteract(event.getPlayer(), event.getBlock().getLocation(), "claimPlugin.others.blocks.place"));
	}
	
	@EventHandler
	public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
		event.getPlayer().sendMessage("PlayerLeashEntityEvent called!");
		event.setCancelled(cancelInteract(event.getPlayer(), event.getEntity().getLocation(), "claimPlugin.others.entities.leash"));
		event.getPlayer().updateInventory();
	}
	
	@EventHandler
	public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();
		player.sendMessage("PlayerUnleashEntityEvent called!");
		boolean isCancelled = cancelInteract(player, event.getEntity().getLocation(), "claimPlugin.others.entities.unleash");
		if(!isCancelled) {
			LivingEntity entity = (LivingEntity)event.getEntity();
			entity.setLeashHolder(null);
			player.getInventory().addItem(new ItemStack(Material.LEAD));
		}
	}
	
//	@EventHandler
//	public void onEntityDeath(EntityDeathEvent event) {
//		if(event.getEntity().isLeashed() && event.getEntity().getLeashHolder() instanceof Player) {
//			Player player = (Player)event.getEntity().getLeashHolder();
//			LivingEntity entity = (LivingEntity)event.getEntity();
//			player.sendMessage(ChatColor.GOLD+"Player leashed creature died!");
//		}
//	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() != null) {
			event.getPlayer().sendMessage("Action not null: "+event.getAction().toString());
		}
		if(event.getAction() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			event.setCancelled(cancelInteract(event.getPlayer(), event.getClickedBlock().getLocation(), "claimPlugin.others.interact"));
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.LEAD) {
			event.getPlayer().sendMessage("Lead detected !");
			return;
		}
		Entity entity = event.getRightClicked();
		event.setCancelled(cancelInteract(event.getPlayer(), entity.getLocation(), "claimPlugin.others.interact"));
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if(event.getEntity().getType() == EntityType.PRIMED_TNT) {
//			main.log("TNT Spawned !");
			TntData tnt = TntData.getTntData(Tnts, event.getLocation());
			if(tnt != null) {
				tnt.getPlayer().sendMessage(ChatColor.DARK_AQUA+"Your tnt had been ignited !");
				event.getEntity().setMetadata("player", new FixedMetadataValue(plugin, tnt.getPlayer().getName()));
				Tnts.remove(tnt);
			}
		}
	}
	
	@EventHandler
	public void onBlockExplodedEvent(EntityExplodeEvent event) {
		if(event.getEntity() instanceof TNTPrimed) {
//			main.log("TNTPrimed !!");
			TNTPrimed tnt = (TNTPrimed) event.getEntity();
			String playerName = null;
			if(tnt.hasMetadata("player")) {
//				main.log("MetaData found !!!");
				playerName = new String(tnt.getMetadata("player").get(0).asString());
				Player player = Bukkit.getPlayer(playerName);
				if(player != null && player.hasPermission("claimPlugin.others.tnt.damage.blocks")) {
					return;
				}
			}
//			main.log("TNT EXPLODED !!!\n");
			ClaimData cd = null;
			for(Block block: new ArrayList<Block>(event.blockList())) {
				if(block.getType() == Material.TNT) {
					continue;
				}
				if(cd == null || !cd.getRegion().contains(main.Location2Vector(block.getLocation()))) {
					cd = ClaimData.getInRegion(block.getLocation());
				}
				if(cd != null) {
					if(playerName != null && main.containsIgnoringCase(cd.getOwners(), playerName)) {
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
			if(tnt.hasMetadata("player")) {
				Player player = Bukkit.getPlayer(tnt.getMetadata("player").get(0).asString());
				if(player != null && player.hasPermission("claimPlugin.others.tnt.damage.entities")) {
					return;
				}
				else {
					event.setCancelled(cancelInteract(player, event.getEntity().getLocation()));
				}
			}
			else {
				event.getEntity().sendMessage(ChatColor.DARK_AQUA+"Wild tnt!");
				event.setCancelled(true);
			}
		}
		else if(event.getDamager() instanceof Player) {
			Entity entity = event.getEntity();
			Player damager = (Player)event.getDamager();
			if(!damager.hasPermission("claimPlugin.others.entity.hit")) {
				event.setCancelled(cancelInteract(damager, entity.getLocation()));
				if(event.isCancelled()) {
					damager.sendMessage(ChatColor.DARK_RED+"You can't hit others entities!");
				}
			}
		}
	}
	
}
