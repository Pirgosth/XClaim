package com.pirgosth.claimPlugin;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;

import com.pirgosth.claimPlugin.Coordinates.CoordinateFormatException;
import com.sk89q.worldedit.regions.CuboidRegion;

public class ClaimData {
	
	public static ClaimData getInRegion(Location location) {
		Set<String> claims = main.claimsYml.get().getConfigurationSection("regions").getKeys(false);
		if(claims.size() == 0) {
			return null;
		}
		for(String claim: claims) {
			Coordinates co = null;
			try {
				co = Coordinates.extractCoordinates(claim);
				CuboidRegion area = new CuboidRegion(co.pos1(), co.pos2());
				if(area.contains(main.Location2Vector(location))) { //&& main.claimsYml.get().getStringList(claim+".owners").contains(player.getName())
					return new ClaimData(co, claim, main.claimsYml.get().getString("regions."+claim+".name"), main.claimsYml.get().getStringList("regions."+claim+".owners"));
				}
			} catch (CoordinateFormatException e) {
				continue;
			}
		}
		return null;
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
}
