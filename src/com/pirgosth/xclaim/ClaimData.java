package com.pirgosth.xclaim;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.pirgosth.xclaim.Coordinates.CoordinateFormatException;
import com.sk89q.worldedit.regions.CuboidRegion;

public class ClaimData {
	public static void updatePlayerRegion(Player player) {
		main.cds.put(player.getName(), ClaimData.getInRegion(player.getLocation(), player.getWorld().getName()));
	}
	
	public static ClaimData getPlayerRegion(Player player) {
		return main.cds.get(player.getName());
	}
	
	public static void setPlayerRegion(Player player, ClaimData cd) {
		main.cds.put(player.getName(), cd);
	}
	
	public static ClaimData getInRegion(Location location, String world) {
		if(!main.worlds.get(world)) {
			return null;
		}
		Set<String> claims = main.claimsYml.get(world).get().getConfigurationSection("").getKeys(false);
		if(claims.size() == 0) {
			return null;
		}
		for(String claim: claims) {
			Coordinates co = null;
			try {
				co = Coordinates.extractCoordinates(claim);
				CuboidRegion area = new CuboidRegion(co.pos1(), co.pos2());
				if(area.contains(Functions.Location2Vector(location))) { //&& main.claimsYml.get().getStringList(claim+".owners").contains(player.getName())
					return new ClaimData(co, claim, main.claimsYml.get(world).get().getString(claim+".name"), main.claimsYml.get(world).get().getStringList(claim+".owners"));
				}
			} catch (CoordinateFormatException e) {
				continue;
			}
		}
		return null;
	}
	
	public static boolean isMemberOf(String node, Player player) {
		return Functions.containsIgnoringCase(ClaimYml.getOwners(player.getWorld().getName(), node), player.getName());
	}
	
	private String node = "";
	private String name = "";
	private List<String> owners = null;
	private Coordinates co = null;
	public ClaimData(Coordinates co, String node, String name, List<String> owners) {
		this.node = node;
		this.name = name;
		this.owners = owners;
		this.co = co;
	}
	public String getNode() {
		return node;
	}
	public String getName() {
		return name;
	}
	public List<String> getOwners() {
		return owners;
	}
	public Coordinates getCoordinates() {
		return co;
	}
	public CuboidRegion getRegion() {
		return new CuboidRegion(co.pos1(), co.pos2());
	}
	public void addOwner(String owner) {
		owners.add(owner);
	}
	public void delOwner(String owner) {
		owners.remove(owner);
	}
}
