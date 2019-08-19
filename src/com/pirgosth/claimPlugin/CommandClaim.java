package com.pirgosth.claimPlugin;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

public class CommandClaim implements CommandExecutor{
	public class CuboidRegionExtended{
		public CuboidRegion region;
		public BlockVector3 pos1;
		public BlockVector3 pos2;
		public CuboidRegionExtended(CuboidRegion region, BlockVector3 pos1, BlockVector3 pos2) {
			this.region = region;
			this.pos1 = pos1;
			this.pos2 = pos2;
		}
	}
	
	private CuboidRegionExtended getOnPlayerCenteredCuboid(Player player, int length, int yMax) {
		if(length <= 0) {
			return null;
		}
		BlockVector3 playerPos = BlockVector3.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
		BlockVector3 pos1 = BlockVector3.at(playerPos.getX()-length/2, 0, playerPos.getZ()-length/2);
		BlockVector3 pos2 = BlockVector3.at(playerPos.getX()+length/2, yMax, playerPos.getZ()+length/2);
		return new CuboidRegionExtended(new CuboidRegion(pos1, pos2), pos1, pos2);
	}
	
	private String claimYmlFormat(BlockVector3 pos1, BlockVector3 pos2) {
		return "regions.("+pos1.getBlockX()+"," + pos1.getBlockY() + "," + pos1.getBlockZ() + ")/("
				+ pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ() + ")";
	}
	
	public void dispClaimInfo(Player p) {
		ChatColor valueColor = ChatColor.GREEN;
		ChatColor textColor = ChatColor.GRAY;
		ClaimData cd = main.cds.get(p.getName());
		if(cd != null) {
			int radius = Math.abs(cd.getCoordinates().pos1().getBlockX()-cd.getCoordinates().pos2().getBlockX());
			p.sendMessage(textColor+"Claim Info:\nName: "+valueColor+cd.getName()+
					textColor+"\nBorders: "+valueColor+cd.getNode()+
					textColor+"\nRadius: "+valueColor+radius+
					textColor+"\nOwners: "+valueColor+cd.getOwners());
		}
		else {
			p.sendMessage(textColor+"You're not in a claim!");
		}
	}
	
	public boolean isClaimInClearArea(Player player, CuboidRegion region) {
		BlockVector3 min = region.getMinimumPoint();
		BlockVector3 max = region.getMaximumPoint();
		for(int i = Math.min(min.getBlockX(), max.getBlockX()); i<=Math.max(min.getBlockX(), max.getBlockX()); i++) {
			for(int j = Math.min(min.getBlockZ(), max.getBlockZ()); j<=Math.max(min.getBlockZ(), max.getBlockZ()); j++) {
				if(ClaimData.getInRegion(new Location(player.getWorld(), i, 0, j)) != null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Console can't execute this command !");
			return true;
		}
		Player p = (Player) sender;
		try {
			WorldEditPlugin we = WorldEditCatcher.getWorldEdit();
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("create")) {
					if(!p.hasPermission("claimPlugin.command.create")) {
						main.insufficientPerm(p, "claimPlugin.command.create");
						return true;
					}
					if(args.length > 2) {
						if(main.cds.get(p.getName()) == null) {
							if(args[2].equalsIgnoreCase("cuboid") && args.length == 4) {
								//Cuboid claim
								if(main.playersYml.get().getConfigurationSection(p.getName()) != null &&
										main.containsIgnoringCase(new ArrayList<String>(main.playersYml.get().getConfigurationSection(p.getName()).getKeys(false)), args[1])) {
									p.sendMessage(ChatColor.GRAY+"You have already a claim named "+ChatColor.GOLD+args[1]);
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
								CuboidRegionExtended select = getOnPlayerCenteredCuboid(p, r, 400);
								
								//If claim is to near from another
								if(!isClaimInClearArea(p, select.region)) {
									p.sendMessage(ChatColor.DARK_AQUA+"Your claim is to near from another one.");
									return true;
								}
								String claimArea = claimYmlFormat(select.pos1, select.pos2);
								main.claimsYml.get().set(claimArea+".name", args[1]);
								main.claimsYml.get().set(claimArea+".owners", Arrays.asList(sender.getName()));
								main.claimsYml.save();
								main.playersYml.get().set(p.getName()+"."+args[1]+".node", claimArea);
								main.playersYml.save();
								sender.sendMessage(ChatColor.BLUE + "Area claimed in a radius of " + args[3] + " blocks!");
								main.cds.put(sender.getName(), ClaimData.getInRegion(((Player) sender).getLocation()));
								return true;
							}
							else if(args[2].equalsIgnoreCase("selection")) {
								//Selection claim
								World w = we.getSession(p).getSelectionWorld();
								if(w == null) {
									sender.sendMessage(ChatColor.RED + "Make selection first !");
									return true;
								}
								Region r = we.getSession(p).getSelection(w);
								String claimArea = claimYmlFormat(BlockVector3.at(r.getMaximumPoint().getX(), 0, r.getMaximumPoint().getZ()), BlockVector3.at(r.getMinimumPoint().getX(), 400, r.getMinimumPoint().getBlockZ()));
								main.claimsYml.get().set(claimArea+".name", args[1]);
								main.claimsYml.get().set(claimArea+".owners", Arrays.asList(sender.getName()));
								main.claimsYml.save();
								sender.sendMessage(ChatColor.BLUE + "Selection claimed successfully!");
								return true;
							}
							else {
								sender.sendMessage(ChatColor.DARK_RED+"Invalid Argument.");
								return true;
							}
						}
						else {
							p.sendMessage(ChatColor.DARK_RED+"You can't claim a claim !");
							return true;
						}
					}
					else {
						sender.sendMessage(ChatColor.DARK_GREEN+"Missing claim type (<cuboid/selection>)");
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("remove")) {
					if(!p.hasPermission("claimPlugin.command.remove")) {
						main.insufficientPerm(p, "claimPlugin.command.remove");
						return true;
					}
					if(args.length > 1) {
						//remove named claim
						p.sendMessage(ChatColor.DARK_AQUA+"Work in progress ...");
					}
					else {
						//remove inClaim claim
						ClaimData cd = null;
						cd = ClaimData.getInRegion(p.getLocation());
						if(cd != null) {
							if(!main.containsIgnoringCase(cd.getOwners(), p.getName()) && !p.hasPermission("claimPlugin.others.remove")) {
								sender.sendMessage(ChatColor.DARK_RED+"You're not allowed to remove this claim !");
								main.insufficientPerm(p, "claimPlugin.others.remove");
								return true;
							}
							main.claimsYml.get().set("regions."+cd.getNode(), null);
							main.claimsYml.save();
							main.playersYml.get().set(p.getName()+"."+cd.getName(), null);
							main.playersYml.save();
							main.cds.put(p.getName(), null);
							sender.sendMessage(ChatColor.GRAY+"Claim: "+ChatColor.GREEN+cd.getNode()+ChatColor.GRAY+" has been removed !");
						}
						else {
							sender.sendMessage(ChatColor.DARK_RED+"No claim to remove here");
						}
						return true;
					}
				}
				else if(args[0].equalsIgnoreCase("info")) {
					if(!p.hasPermission("claimPlugin.command.info")) {
						main.insufficientPerm(p, "claimPlugin.command.info");
						return true;
					}
					dispClaimInfo(p);
				}
				else {
					sender.sendMessage(ChatColor.DARK_RED + "Invalid Argument");
					return true;
				}
			}
		} catch (MissingPluginException e) {
			e.printStackTrace();
		} catch (IncompleteRegionException e) {
//			e.printStackTrace();
			p.sendMessage(ChatColor.DARK_RED+"Incomplete selection");
		}
		return true;
	}

}
