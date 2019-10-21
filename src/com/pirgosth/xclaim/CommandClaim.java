package com.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

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
					textColor+"\nOwners: "+valueColor+cd.getOwners());
		}
		else {
			player.sendMessage(textColor+"You're not in a claim!");
		}
	}
	
	public void dispClaimList(Player player) {
		String world = player.getWorld().getName();
		ChatColor valueColor = ChatColor.GREEN;
		ChatColor textColor = ChatColor.GRAY;
		if(main.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims") == null || 
				main.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims").getKeys(false).size() == 0) {
			player.sendMessage(textColor+"You have no claims");
			return;
		}
		Set<String> list = main.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims").getKeys(false);
		player.sendMessage(textColor+"Your claims: "+valueColor+list);
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
		try {
			WorldEditPlugin we = WorldEditCatcher.getWorldEdit();
			if(sender instanceof ConsoleCommandSender) {
				if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
					Data.load(main.getProvidingPlugin(getClass()));
					for(Player p: Bukkit.getOnlinePlayers()) {
						ClaimData.updatePlayerRegion(p);
					}
					sender.sendMessage(ChatColor.DARK_GREEN + "Plugin reloaded !");
					return true;
				}
				else {
					sender.sendMessage(ChatColor.DARK_RED + "Console can't exexcute this command or command does not exist");
					return true;
				}
			}
			if(sender instanceof Player) {
				Player player = (Player) sender;
				String world = player.getWorld().getName();
				if(!main.worlds.get(world)) {
					return true;
				}
				if(args.length > 0) {
					if(args[0].equalsIgnoreCase("create")) {
						
						if(!player.hasPermission("xclaim.command.create")) {
							Messages.sendRestriction("on-command", player);
	//						main.insufficientPerm(player, "xclaim.command.create");
							return true;
						}
						if(args.length > 2) {
							if(main.cds.get(player.getName()) == null) {
								if(args[2].equalsIgnoreCase("cuboid") && args.length == 4) {
									//Cuboid claim
									if(main.playersYml.get(world) == null) {
										Functions.log(ChatColor.DARK_RED + world);
									}
									if(main.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims") != null &&
											Functions.containsIgnoringCase(new ArrayList<String>(main.playersYml.get(world).get().getConfigurationSection(player.getName()+".claims").getKeys(false)), args[1])) {
										player.sendMessage(ChatColor.GRAY+"You have already a claim named "+ChatColor.GOLD+args[1]);
										return true;
									}
									int r = -1;
									try {
										r = Integer.parseInt(args[3]);
									}
									catch(NumberFormatException e) {
										sender.sendMessage(ChatColor.DARK_RED+"Invalid argument! Must be an integer!");
										return true;
									}
									if(r < 1 || r > 200) {
										sender.sendMessage(ChatColor.DARK_RED+"Invalid argument! Size must be between 1 and 200 !");
										return true;
									}
									CuboidRegion select = getOnPlayerCenteredCuboid(player, r, 400);
									
									//If claim is to near from another
									if(!isClaimInClearArea(player, select)) {
										player.sendMessage(ChatColor.DARK_AQUA+"Your claim is to near from another one.");
										return true;
									}
									String claimArea = claimYmlFormat(select.getMinimumPoint(), select.getMaximumPoint());
									ClaimYml.addClaim(world, claimArea, args[1], Arrays.asList(player.getName()));
									PlayerYml.addClaim(claimArea, args[1], player);
									player.sendMessage(ChatColor.BLUE + "Area claimed in a radius of " + args[3] + " blocks!");
									ClaimData.updatePlayerRegion(player);
									return true;
								}
								else if(args[2].equalsIgnoreCase("selection")) {
									//Selection claim
									World w = we.getSession(player).getSelectionWorld();
									if(w == null) {
										sender.sendMessage(ChatColor.RED + "Make selection first !");
										return true;
									}
									try {
									Region r = we.getSession(player).getSelection(w);
									String claimArea = claimYmlFormat(BlockVector3.at(r.getMaximumPoint().getX(), 0, r.getMaximumPoint().getZ()), BlockVector3.at(r.getMinimumPoint().getX(), 400, r.getMinimumPoint().getBlockZ()));
									ClaimYml.addClaim(world, claimArea, args[1], Arrays.asList(player.getName()));
									}
									catch (IncompleteRegionException e) {
//										e.printStackTrace();
										player.sendMessage(ChatColor.DARK_RED+"Incomplete selection");
										return true;
									}
									player.sendMessage(ChatColor.BLUE + "Selection claimed successfully!");
									return true;
								}
								else {
									sender.sendMessage(ChatColor.DARK_RED+"Invalid Argument.");
									return true;
								}
							}
							else {
								player.sendMessage(ChatColor.DARK_RED+"You can't claim a claim !");
								return true;
							}
						}
						else {
							sender.sendMessage(ChatColor.DARK_GREEN+"Missing claim type (<cuboid/selection>)");
							return true;
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
								player.sendMessage(ChatColor.GRAY+"You have no claims to remove.");
								return true;
							}
							else if(PlayerYml.getClaim(args[1], player) == null) {
								player.sendMessage(ChatColor.GRAY+"You have no claims named "+ChatColor.DARK_AQUA+args[1]);
								return true;
							}
							String node = PlayerYml.getClaim(args[1], player);
							ClaimYml.delClaim(world, node);
							PlayerYml.delClaim(args[1], player);
							player.sendMessage(ChatColor.GRAY+"Claim: "+ChatColor.GREEN+node+ChatColor.GRAY+" has been removed !");
							ClaimData.updatePlayerRegion(player);
						}
						else {
							//remove inClaim claim
							ClaimData cd = ClaimData.getInRegion(player.getLocation(), world);
							if(cd == null) {
								player.sendMessage(ChatColor.DARK_RED+"No claim to remove here");
								return true;
							}
							if(!Functions.containsIgnoringCase(cd.getOwners(), player.getName()) && 
									!player.hasPermission("xclaim.others.remove")) {
								player.sendMessage(ChatColor.DARK_RED+"You're not allowed to remove this claim !");
	//							main.insufficientPerm(player, "xclaim.others.remove");
								return true;
							}
							ClaimYml.delClaim(world, cd.getNode());
							PlayerYml.delClaim(cd.getName(), player);
							ClaimData.updatePlayerRegion(player);
							player.sendMessage(ChatColor.GRAY+"Claim: "+ChatColor.GREEN+cd.getNode()+ChatColor.GRAY+" has been removed !");
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
					else if(args[0].equalsIgnoreCase("addOwner")) {
						ChatColor textColor = ChatColor.GRAY;
						ChatColor valueColor = ChatColor.GREEN;
						if(!player.hasPermission("xclaim.command.addOwner")) {
							Messages.sendRestriction("on-command", player);
	//						main.insufficientPerm(player, "xclaim.command.addOwner");
							return true;
						}
						if(args.length == 2) {
							ClaimData cd = main.cds.get(player.getName());
							if(cd == null) {
								player.sendMessage(textColor+"You must be in a claim to add a player!");
								return true;
							}
							if(!Functions.containsIgnoringCase(cd.getOwners(), player.getName())) {
								player.sendMessage(textColor+"You must be in your claims to add a player!");
								return true;
							}
							if(!Functions.containsIgnoringCase(Bukkit.getOnlinePlayers(), args[1])){
								player.sendMessage(textColor+"There is no player "+valueColor+args[1]+textColor+" online.");
								return true;
							}
							if(Functions.containsIgnoringCase(cd.getOwners(), args[1])) {
								player.sendMessage(textColor+"Player "+valueColor+args[1]+textColor+" is already an owner of this claim!");
								return true;
							}
							
							Player addedPlayer = Bukkit.getPlayer(args[1]);
							ClaimYml.addOwner(cd.getNode(), addedPlayer);
							PlayerYml.addClaim(cd.getNode(), cd.getName(), addedPlayer);
							ClaimData.updatePlayerRegion(player);
							ClaimData.updatePlayerRegion(addedPlayer);
							player.sendMessage(textColor+"Player "+valueColor+args[1]+textColor+" added to this claim!");
							addedPlayer.sendMessage(valueColor+player.getName()+textColor+" add you as owner of "+valueColor+cd.getName()+textColor+" claim.");
							return true;
						}
						else if(args.length > 2){
							sender.sendMessage(ChatColor.DARK_RED + "Too many arguments");
							return true;
						}
						else {
							sender.sendMessage(ChatColor.DARK_RED + "Missing player name");
							return true;
						}
					}
					else if(args[0].equalsIgnoreCase("delOwner")) {
						ChatColor textColor = ChatColor.GRAY;
						ChatColor valueColor = ChatColor.GREEN;
						if(!player.hasPermission("xclaim.command.delOwner")) {
							Messages.sendRestriction("on-command", player);
	//						main.insufficientPerm(player, "xclaim.command.delOwner");
							return true;
						}
						if(args.length == 2) {
							ClaimData cd = main.cds.get(player.getName());
							if(cd == null) {
								player.sendMessage(textColor+"You must be in a claim to remove a player!");
								return true;
							}
							if(!Functions.containsIgnoringCase(cd.getOwners(), player.getName())) {
								player.sendMessage(textColor+"You must be in your claims to remove a player!");
								return true;
							}
							if(cd.getOwners().size() == 1) {
								player.sendMessage(textColor+"There must have at least one owner in this claim!");
								return true;
							}
							if(!Functions.containsIgnoringCase(cd.getOwners(), args[1])){
								player.sendMessage(valueColor+args[1]+textColor+"is not a owner of this claim.");
								return true;
							}
							Player removedPlayer = Bukkit.getPlayer(args[1]);
							ClaimYml.delOwner(cd.getNode(), removedPlayer);
							PlayerYml.delClaim(cd.getName(), removedPlayer);
							ClaimData.updatePlayerRegion(player);
							ClaimData.updatePlayerRegion(removedPlayer);
							player.sendMessage(textColor+"Player "+valueColor+args[1]+" removed from this claim");
							removedPlayer.sendMessage(valueColor+player.getName()+textColor+" remove you from "+valueColor+cd.getName()+textColor+" claim.");
							return true;
						}
						else if(args.length > 2){
							sender.sendMessage(ChatColor.DARK_RED + "Too many arguments");
							return true;
						}
						else {
							sender.sendMessage(ChatColor.DARK_RED + "Missing player name");
							return true;
						}
					}
					else if(args[0].equalsIgnoreCase("reload")) {
						if(!player.hasPermission("xclaim.command.reload")) {
							Messages.sendRestriction("on-command", player);
							return true;
						}
						Data.load(main.getProvidingPlugin(getClass()));
						for(Player p: Bukkit.getOnlinePlayers()) {
							ClaimData.updatePlayerRegion(p);
						}
						player.sendMessage(ChatColor.DARK_GREEN + "Plugin reloaded !");
						return true;
					}
					else {
						sender.sendMessage(ChatColor.DARK_RED + "Invalid Argument");
						return true;
					}
				}
			}
		} catch (MissingPluginException e) {
			e.printStackTrace();
		}
		return true;
	}

}