package io.github.pirgosth.xclaim;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import io.github.pirgosth.xclaim.Coordinates.CoordinateFormatException;

public class ClaimData {
	
	public static void updatePlayerRegion(Player player, Location location) {
		XClaim.cds.put(player.getName(), ClaimData.getInRegion(location, player.getWorld().getName()));
	}
	
	public static void updatePlayerRegion(Player player) {
		updatePlayerRegion(player, player.getLocation());
	}
	
	public static ClaimData getPlayerRegion(Player player) {
		return XClaim.cds.get(player.getName());
	}
	
	public static void setPlayerRegion(Player player, ClaimData cd) {
		XClaim.cds.put(player.getName(), cd);
	}
	
	public static ClaimData getInRegion(Location location, String world) {
		if(!XClaim.worlds.get(world)) {
			return null;
		}
		Set<String> nodes = ClaimYml.getNodes(world);
		if(nodes.size() == 0) {
			return null;
		}
		for(String node: nodes) {
			Coordinates co = null;
			try {
				co = Coordinates.extractCoordinates(node);
				CuboidRegion area = new CuboidRegion(co.pos1(), co.pos2());
				if(area.contains(Functions.Location2Vector(location))) {
					return new ClaimData(co, node, ClaimYml.getName(world, node), ClaimYml.getOwners(world, node), ClaimYml.getMembers(world, node));
				}
			} catch (CoordinateFormatException e) {
				continue;
			}
		}
		return null;
	}

	public static ClaimData getInRegion(Entity entity) {
		return getInRegion(entity.getLocation(), entity.getWorld().getName());
	}
	
	public static ClaimData getInRegion(Block block) {
		return getInRegion(block.getLocation(), block.getWorld().getName());
	}
	
	public static void refreshOnlinePlayers() {
		for(Player player: Bukkit.getOnlinePlayers()) {
			ClaimData.setPlayerRegion(player, null);
			ClaimData.updatePlayerRegion(player);
		}
	}
	
	private String node = "";
	private String name = "";
	private List<String> members = null;
	private List<String> owners = null;
	private Coordinates co = null;
	public ClaimData(Coordinates co, String node, String name, List<String> owners, List<String> members) {
		this.node = node;
		this.name = name;
		this.members = members;
		this.owners = owners;
		this.co = co;
	}
	public String getNode() {
		return node;
	}
	public String getName() {
		return name;
	}
	public List<String> getMembers(){
		return members;
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
	public BlockVector3 getCenter() {
		return getRegion().getCenter().toBlockPoint();
	}
	public void addOwner(String owner) {
		owners.add(owner);
	}
	public void delOwner(String owner) {
		owners.remove(owner);
	}
	public boolean isOwnerOf(String playerName) {
		return Functions.containsIgnoringCase(owners, playerName);
	}
	public boolean isOwnerOf(Player player) {
		return isOwnerOf(player.getName());
	}
	public boolean isMemberOf(String playerName) {
		return Functions.containsIgnoringCase(members, playerName);
	}
	public boolean isMemberOf(Player player) {
		return isMemberOf(player.getName());
	}
}
