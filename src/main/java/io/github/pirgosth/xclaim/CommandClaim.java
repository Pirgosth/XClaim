package io.github.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

public class CommandClaim implements CommandExecutor{
	
	private CuboidRegion getOnPlayerCenteredCuboid(Player player, int length, int yMax) {
		if(length <= 0) {
			return null;
		}
		BlockVector3 playerPos = BlockVector3.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
		BlockVector3 pos1 = BlockVector3.at(playerPos.getX()-length/2, 0, playerPos.getZ()-length/2);
		BlockVector3 pos2 = BlockVector3.at(playerPos.getX()+length/2, yMax, playerPos.getZ()+length/2);
		return new CuboidRegion(pos1, pos2);
	}
	
	private String claimYmlFormat(BlockVector3 pos1, BlockVector3 pos2) {
		return "("+pos1.getBlockX()+"," + pos1.getBlockY() + "," + pos1.getBlockZ() + ")/("
				+ pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ() + ")";
	}
	
	public void dispClaimInfo(Player player) {
		ChatColor valueColor = ChatColor.AQUA;
		ChatColor textColor = ChatColor.GRAY;
		ClaimData cd = ClaimData.getPlayerRegion(player);
		if(cd != null) {
			int radius = Math.abs(cd.getCoordinates().pos1().getBlockX()-cd.getCoordinates().pos2().getBlockX());
			player.sendMessage(textColor+"Claim Info:\nName: "+valueColor+cd.getName()+
					textColor+"\nBorders: "+valueColor+cd.getNode()+
					textColor+"\nRadius: "+valueColor+radius+
					textColor+"\nMembers: "+valueColor+cd.getMembers()+
					textColor+"\nOwners: "+valueColor+cd.getOwners());
		}
		else {
			Messages.sendInformation("on-not-in-claim", player);
		}
	}
	
	public void dispClaimList(Player player) {
		String world = player.getWorld().getName();
		if(XClaim.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims") == null || 
				XClaim.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims").getKeys(false).size() == 0) {
			Messages.sendInformation("on-no-claim", player);
			return;
		}
		Set<String> list = XClaim.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims").getKeys(false);
		Messages.sendInformation("on-claims-disp", player, list.toString());
	}
	
	public boolean isClaimInClearArea(Player player, CuboidRegion region) {
		String world = player.getWorld().getName();
		BlockVector3 min = region.getMinimumPoint();
		BlockVector3 max = region.getMaximumPoint();
		for(int i = Math.min(min.getBlockX(), max.getBlockX()); i<=Math.max(min.getBlockX(), max.getBlockX()); i++) {
			for(int j = Math.min(min.getBlockZ(), max.getBlockZ()); j<=Math.max(min.getBlockZ(), max.getBlockZ()); j++) {
				if(ClaimData.getInRegion(new Location(player.getWorld(), i, 0, j), world) != null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if(sender instanceof ConsoleCommandSender) {
				ConsoleCommandSender console = (ConsoleCommandSender)sender;
				if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
					JavaPlugin plugin = XClaim.getProvidingPlugin(getClass());
					Data.reload(plugin);
					for(Player p: Bukkit.getOnlinePlayers()) {
						ClaimData.updatePlayerRegion(p);
					}
					console.sendMessage(ChatColor.DARK_GREEN + "Plugin reloaded !");
					return true;
				}
				else {
					Messages.sendInformation("on-invalid-console", console);
					return true;
				}
			}
			if(sender instanceof Player) {
				Player player = (Player) sender;
				String world = player.getWorld().getName();
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("reload")) {
						if(!player.hasPermission("xclaim.command.reload")) {
							Messages.sendRestriction("on-command", player);
							return true;
						}
						JavaPlugin plugin = XClaim.getProvidingPlugin(getClass());
						Data.reload(plugin);
						for(Player p: Bukkit.getOnlinePlayers()) {
							ClaimData.updatePlayerRegion(p);
						}
						player.sendMessage(ChatColor.DARK_GREEN + "Plugin reloaded !");
						return true;
					}
					if(XClaim.worlds.get(world)) {
						if(args[0].equalsIgnoreCase("create")) {
							
							if(!player.hasPermission("xclaim.command.create")) {
								Messages.sendRestriction("on-command", player);
		//						main.insufficientPerm(player, "xclaim.command.create");
								return true;
							}
							if(args.length == 3) {
								if(XClaim.cds.get(player.getName()) == null) {
										//Cuboid claim
										if(XClaim.playersYml.get(world) == null) {
											Functions.log(ChatColor.DARK_RED + world);
										}
										if(XClaim.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims") != null &&
												Functions.containsIgnoringCase(new ArrayList<String>(XClaim.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims").getKeys(false)), args[1])) {
											Messages.sendInformation("on-multiple-name", player, args[1]);
											return true;
										}
										int r = -1;
										try {
											r = Integer.parseInt(args[2]);
										}
										catch(NumberFormatException e) {
											Messages.sendInformation("on-invalid-type-int", player);
											return true;
										}
										int rmax = XClaim.config.getInt("claims.range");
										if(r < 1 || r > rmax) {
											Messages.sendInformation("on-invalid-claim-size", player, Integer.toString(rmax));
											return true;
										}
										CuboidRegion select = getOnPlayerCenteredCuboid(player, r, 400);
										
										//If claim is to near from another
										if(!isClaimInClearArea(player, select)) {
											Messages.sendInformation("on-too-near", player);
											return true;
										}
										ConfigurationSection cfgs = XClaim.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims");
										if(!player.hasPermission("xclaim.claims.count.unlimited") && cfgs != null && cfgs.getKeys(false).size() >= XClaim.config.getInt("claims.count")) {
											Messages.sendInformation("on-too-many-claims", player);
											return true;
										}
										String claimArea = claimYmlFormat(select.getMinimumPoint(), select.getMaximumPoint());
										ClaimYml.addClaim(world, claimArea, args[1], player.getLocation(), Arrays.asList(player.getName()));
										PlayerYml.addClaim(world, claimArea, args[1], player);
										ClaimData.refreshOnlinePlayers();
										Messages.sendInformation("on-claim-created", player, Integer.toString(r));
										return true;
								}
								else {
									Messages.sendInformation("on-claim-overlap", player);
									return true;
								}
							}
							else if(args.length == 2) {
								Messages.sendInformation("on-missing-radius", player);
								return true;
							}
							else if(args.length == 1) {
								Messages.sendInformation("on-missing-name", player);
								return true;
							}
							else {
								Messages.sendInformation("on-too-many-arguments", player);
							}
						}
						else if(args[0].equalsIgnoreCase("remove")) {
							if(!player.hasPermission("xclaim.command.remove")) {
								Messages.sendRestriction("on-command", player);
		//						main.insufficientPerm(player, "xclaim.command.remove");
								return true;
							}
							if(args.length > 1) {
								//remove named claim
								if(PlayerYml.getClaimsByName(player).isEmpty()) {
									Messages.sendInformation("on-no-remove", player);
									return true;
								}
								
								String node = PlayerYml.getClaim(args[1], player);
								
								if(node == null) {
									Messages.sendInformation("on-incorrect-claim", player, args[1]);;
									return true;
								}
								
								ClaimData cd = ClaimYml.getClaim(world, node);
								
								if(!cd.isOwnerOf(player)) {
									Messages.sendInformation("on-not-owner-remove", player);
									return true;
								}
								
								List<String> members = ClaimYml.getMembers(world, cd.getNode());
								members.addAll(ClaimYml.getOwners(world, cd.getNode()));
								ClaimYml.delClaim(world, cd.getNode());
								for(String member: members) {
									PlayerYml.delClaim(world, cd.getName(), member);
									Player p = Bukkit.getPlayer(member);
									if(p != null) {
										Messages.sendInformation("on-remove-receive", p, cd.getName(), player.getName());
									}
								}
								ClaimData.refreshOnlinePlayers();
								Messages.sendInformation("on-remove", player, cd.getName());
								return true;
							}
							else {
								//remove inClaim claim
								ClaimData cd = ClaimData.getInRegion(player.getLocation(), world);
								if(cd == null) {
									Messages.sendInformation("on-not-in-claim", player);
									return true;
								}
								if(!cd.isOwnerOf(player) && !player.hasPermission("xclaim.others.remove")) {
									Messages.sendRestriction("on-others-remove", player);
									return true;
								}
								
								List<String> members = ClaimYml.getMembers(world, cd.getNode());
								members.addAll(ClaimYml.getOwners(world, cd.getNode()));
								ClaimYml.delClaim(world, cd.getNode());
								for(String member: members) {
									PlayerYml.delClaim(world, cd.getName(), member);
									Player p = Bukkit.getPlayer(member);
									if(!member.equalsIgnoreCase(player.getName()) && p != null) {
										Messages.sendInformation("on-remove-receive", p, cd.getName(), player.getName());
									}
								}
								ClaimData.refreshOnlinePlayers();
								Messages.sendInformation("on-remove-send", player, cd.getName());
								return true;
							}
						}
						else if(args[0].equalsIgnoreCase("info")) {
							if(!player.hasPermission("xclaim.command.info")) {
								Messages.sendRestriction("on-command", player);
		//						main.insufficientPerm(player, "xclaim.command.info");
								return true;
							}
							dispClaimInfo(player);
						}
						else if(args[0].equalsIgnoreCase("list")) {
							if(!player.hasPermission("xclaim.command.list")) {
								Messages.sendRestriction("on-command", player);
		//						main.insufficientPerm(player, "xclaim.command.list");
								return true;
							}
							dispClaimList(player);
						}
						else if(args[0].equalsIgnoreCase("home")) {
							if(!player.hasPermission("xclaim.command.home")) {
								Messages.sendRestriction("on-command", player);
								return true;
							}
							if(args.length < 2) {
								Messages.sendInformation("on-missing-claim", player);
								return true;
							}
							String claimName = args[1];
							String claimNode = PlayerYml.getClaim(claimName, player);
							if(claimNode == null) {
								Messages.sendInformation("on-incorrect-claim", player, claimName);
								return true;
							}
							
							BlockVector3 homeLocation = ClaimYml.getHome(world, PlayerYml.getClaim(claimName, player));
							
							if(homeLocation.getBlockY() == 0) {
								Messages.sendInformation("on-invalid-home", player);
								return true;
							}
							
							Messages.sendInformation("on-home-teleport", player, claimName);
							player.teleport(new Location(player.getWorld(), homeLocation.getBlockX(), homeLocation.getBlockY(), homeLocation.getBlockZ()));
							
							return true;
						}
						else if(args[0].equalsIgnoreCase("sethome")) {
							if(!player.hasPermission("xclaim.command.sethome")) {
								Messages.sendRestriction("on-command", player);
								return true;
							}
							
							ClaimData cd = ClaimData.getPlayerRegion(player);
							if(cd == null) {
								Messages.sendInformation("on-not-in-claim", player);
								return true;
							}
							if(!cd.isOwnerOf(player)) {
								Messages.sendInformation("on-not-in-own-claim", player);
								return true;
							}
							
							ClaimYml.setHome(world, cd.getNode(), player.getLocation());
							Messages.sendInformation("on-sethome", player, cd.getName());
							return true;
						}
						else if(args[0].equalsIgnoreCase("addOwner")) {
							if(!player.hasPermission("xclaim.command.addOwner")) {
								Messages.sendRestriction("on-command", player);
								return true;
							}
							if(args.length == 2) {
								ClaimData cd = XClaim.cds.get(player.getName());
								if(cd == null) {
									Messages.sendInformation("on-not-in-claim-add-owner", player);
									return true;
								}
								if(!cd.isOwnerOf(player)) {
									Messages.sendInformation("on-not-in-own-claim-add-owner", player);
									return true;
								}
								if(!Functions.containsIgnoringCase(Bukkit.getOnlinePlayers(), args[1])){
									Messages.sendInformation("on-player-not-found", player, args[1]);
									return true;
								}
								if(cd.isOwnerOf(args[1])) {
									Messages.sendInformation("on-already-owner", player, args[1]);
									return true;
								}
								if(cd.isMemberOf(args[1])) {
									Messages.sendInformation("on-already-member", player, args[1]);
									return true;
								}
								
								Player addedPlayer = Bukkit.getPlayer(args[1]);
								ClaimYml.addOwner(world, cd.getNode(), addedPlayer);
								PlayerYml.addClaim(world, cd.getNode(), cd.getName(), addedPlayer);
								List<String> owners = ClaimYml.getOwners(world, cd.getNode());
								owners.addAll(ClaimYml.getMembers(world, cd.getNode()));
								for(String owner: owners) {
									Player p = Bukkit.getPlayer(owner);
									if(p != null) {
										ClaimData.setPlayerRegion(p, null);
										ClaimData.updatePlayerRegion(p);
									}
								}
								ClaimData.setPlayerRegion(addedPlayer, null);
								ClaimData.updatePlayerRegion(addedPlayer);
								Messages.sendInformation("on-added-owner-send", player, args[1]);
								Messages.sendInformation("on-added-owner-received", addedPlayer, cd.getName(), player.getName());
								return true;
							}
							else if(args.length > 2){
								Messages.sendInformation("on-too-many-args", player);
								return true;
							}
							else {
								Messages.sendInformation("on-missing-playername", player);
								return true;
							}
						}
						else if(args[0].equalsIgnoreCase("delOwner")) {
							if(!player.hasPermission("xclaim.command.delOwner")) {
								Messages.sendRestriction("on-command", player);
		//						main.insufficientPerm(player, "xclaim.command.delOwner");
								return true;
							}
							if(args.length == 2) {
								ClaimData cd = XClaim.cds.get(player.getName());
								if(cd == null) {
									Messages.sendInformation("on-not-in-claim-del-owner", player);
									return true;
								}
								if(!cd.isOwnerOf(player)) {
									Messages.sendInformation("on-not-in-own-claim-del-owner", player);
									return true;
								}
								if(cd.getOwners().size() == 1) {
									Messages.sendInformation("on-too-few-owners", player);
									return true;
								}
								if(!cd.isOwnerOf(args[1])){
									Messages.sendInformation("on-not-owner", player, args[1]);
									return true;
								}
								ClaimYml.delOwner(player.getWorld().getName(), cd.getNode(), args[1]);
								PlayerYml.delClaim(player.getWorld().getName(), cd.getName(), args[1]);
								List<String> owners = ClaimYml.getOwners(world, cd.getNode());
								owners.addAll(ClaimYml.getMembers(world, cd.getNode()));
								for(String owner: owners) {
									Player p = Bukkit.getPlayer(owner);
									if(p != null) {
										ClaimData.setPlayerRegion(p, null);
										ClaimData.updatePlayerRegion(p);
									}
								}
								Player removedPlayer = Bukkit.getPlayer(args[1]);
								if(removedPlayer != null) {
									ClaimData.setPlayerRegion(removedPlayer, null);
									ClaimData.updatePlayerRegion(removedPlayer);
									Messages.sendInformation("on-del-owner-received", removedPlayer, cd.getName(), player.getName());
								}
								Messages.sendInformation("on-del-owner-send", player, args[1]);
								return true;
							}
							else if(args.length > 2){
								Messages.sendInformation("on-too-many-args", player);
								return true;
							}
							else {
								Messages.sendInformation("on-missing-playername", player);
								return true;
							}
						}
						else if(args[0].equalsIgnoreCase("addMember")) {
							if(!player.hasPermission("xclaim.command.addMember")) {
								Messages.sendRestriction("on-command", player);
								return true;
							}
							if(args.length == 2) {
								ClaimData cd = XClaim.cds.get(player.getName());
								if(cd == null) {
									Messages.sendInformation("on-not-in-claim-add-member", player);
									return true;
								}
								if(!cd.isOwnerOf(player)) {
									Messages.sendInformation("on-not-in-own-claim-add-member", player);
									return true;
								}
								if(!Functions.containsIgnoringCase(Bukkit.getOnlinePlayers(), args[1])){
									Messages.sendInformation("on-player-not-found", player, args[1]);
									return true;
								}
								if(cd.isMemberOf(args[1])) {
									Messages.sendInformation("on-already-member", player, args[1]);
									return true;
								}
								if(cd.isOwnerOf(args[1])) {
									Messages.sendInformation("on-already-owner", player, args[1]);
									return true;
								}
								
								Player addedPlayer = Bukkit.getPlayer(args[1]);
								ClaimYml.addMember(world, cd.getNode(), addedPlayer);
								PlayerYml.addClaim(world, cd.getNode(), cd.getName(), addedPlayer);
								List<String> members = ClaimYml.getMembers(world, cd.getNode());
								members.addAll(ClaimYml.getOwners(world, cd.getNode()));
								for(String member: members) {
									Player p = Bukkit.getPlayer(member);
									if(p != null) {
										ClaimData.setPlayerRegion(p, null);
										ClaimData.updatePlayerRegion(p);
									}
								}
								ClaimData.setPlayerRegion(addedPlayer, null);
								ClaimData.updatePlayerRegion(addedPlayer);
								Messages.sendInformation("on-added-member-send", player, args[1]);
								Messages.sendInformation("on-added-member-received", addedPlayer, cd.getName(), player.getName());
								return true;
							}
							else if(args.length > 2){
								Messages.sendInformation("on-too-many-args", player);
								return true;
							}
							else {
								Messages.sendInformation("on-missing-playername", player);
								return true;
							}
						}
						else if(args[0].equalsIgnoreCase("delMember")) {
							if(!player.hasPermission("xclaim.command.delMember")) {
								Messages.sendRestriction("on-command", player);
								return true;
							}
							if(args.length == 2) {
								ClaimData cd = XClaim.cds.get(player.getName());
								if(cd == null) {
									Messages.sendInformation("on-not-in-claim-del-member", player);
									return true;
								}
								if(!cd.isOwnerOf(player)) {
									Messages.sendInformation("on-not-in-own-claim-del-member", player);
									return true;
								}
								if(!cd.isMemberOf(args[1])){
									Messages.sendInformation("on-not-member", player, args[1]);
									return true;
								}
								ClaimYml.delMember(player.getWorld().getName(), cd.getNode(), args[1]);
								PlayerYml.delClaim(player.getWorld().getName(), cd.getName(), args[1]);
								List<String> members = ClaimYml.getMembers(world, cd.getNode());
								members.addAll(ClaimYml.getOwners(world, cd.getNode()));
								for(String member: members) {
									Player p = Bukkit.getPlayer(member);
									if(p != null) {
										ClaimData.setPlayerRegion(p, null);
										ClaimData.updatePlayerRegion(p);
									}
								}
								Player removedPlayer = Bukkit.getPlayer(args[1]);
								if(removedPlayer != null) {
									ClaimData.setPlayerRegion(removedPlayer, null);
									ClaimData.updatePlayerRegion(removedPlayer);
									Messages.sendInformation("on-del-member-received", removedPlayer, cd.getName(), player.getName());
								}
								Messages.sendInformation("on-del-member-send", player, args[1]);
								return true;
							}
							else if(args.length > 2){
								Messages.sendInformation("on-too-many-args", player);
								return true;
							}
							else {
								Messages.sendInformation("on-missing-playername", player);
								return true;
							}
						}
						else if(args[0].equalsIgnoreCase("leave")) {
							if(!player.hasPermission("xclaim.command.leave")) {
								Messages.sendRestriction("on-command", player);
								return true;
							}
							ClaimData cd = ClaimData.getInRegion(player);
							if(cd == null) {
								Messages.sendInformation("on-not-in-claim", player);
								return true;
							}
							if(!cd.isMemberOf(player)) {
								Messages.sendInformation("on-not-in-claim-leave", player);
								return true;
							}
							ClaimYml.delMember(player.getWorld().getName(), cd.getNode(), player.getName());
							PlayerYml.delClaim(player.getWorld().getName(), cd.getName(), player.getName());
							
							List<String> members = ClaimYml.getMembers(world, cd.getNode());
							members.addAll(ClaimYml.getOwners(world, cd.getNode()));
							for(String member: members) {
								Player p = Bukkit.getPlayer(member);
								if(p != null) {
									ClaimData.setPlayerRegion(p, null);
									ClaimData.updatePlayerRegion(p);
									Messages.sendInformation("on-claim-left-send", p, cd.getName(), player.getName());
								}
							}
							
							ClaimData.updatePlayerRegion(player);
							
							Messages.sendInformation("on-claim-left-receive", player, cd.getName());
							return true;
						}
						else {
							Messages.sendInformation("on-invalid-argument", player);
							return true;
						}
					}
				}
			}
		return true;
	}

}
