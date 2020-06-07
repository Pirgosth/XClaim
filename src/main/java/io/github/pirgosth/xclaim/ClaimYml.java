package io.github.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector3;

import io.github.pirgosth.xclaim.Coordinates.CoordinateFormatException;

public class ClaimYml {
	public static Set<String> getNodes(String world){
		return XClaim.claimsYml.get(world).get().getConfigurationSection("").getKeys(false);
	}
	public static void addClaim(String world, String node, String name, Location loc, List<String> owners) {
		XClaim.claimsYml.get(world).get().set(node+".name", name);
		XClaim.claimsYml.get(world).get().set(node+".home.x", loc.getBlockX());
		XClaim.claimsYml.get(world).get().set(node+".home.y", loc.getBlockY());
		XClaim.claimsYml.get(world).get().set(node+".home.z", loc.getBlockZ());
		XClaim.claimsYml.get(world).get().set(node+".owners", owners);
	}
	public static void delClaim(String world, String node) {
		XClaim.claimsYml.get(world).get().set(node, null);
	}
	
	public static ClaimData getClaim(String world, String node) {
		try {
			Coordinates co = Coordinates.extractCoordinates(node);
			return new ClaimData(co, node, XClaim.claimsYml.get(world).get().getString(node + ".name"), ClaimYml.getOwners(world, node), ClaimYml.getMembers(world, node));
		}
		catch (CoordinateFormatException e) {
			return null;
		}
	}
	
	public static String getName(String world, String node) {
		if(XClaim.claimsYml.get(world).get().getString(node + ".name") != null) {
			return XClaim.claimsYml.get(world).get().getString(node + ".name");
		}
		else {
			return new String();
		}
	}
	
	public static BlockVector3 getHome(String world, String node) {
		return BlockVector3.at(XClaim.claimsYml.get(world).get().getInt(node+".home.x"),
				XClaim.claimsYml.get(world).get().getInt(node+".home.y"), XClaim.claimsYml.get(world).get().getInt(node+".home.z"));
	}
	
	public static void setHome(String world, String node, Location location) {
		XClaim.claimsYml.get(world).get().set(node + ".home.x", location.getBlockX());
		XClaim.claimsYml.get(world).get().set(node + ".home.y", location.getBlockY());
		XClaim.claimsYml.get(world).get().set(node + ".home.z", location.getBlockZ());
	}
	
	public static List<String> getOwners(String world, String node){
		if(XClaim.claimsYml.get(world).get().getStringList(node+".owners") != null) {
			return XClaim.claimsYml.get(world).get().getStringList(node+".owners");
		}
		else {
			return new ArrayList<String>();
		}
	}
	
	public static void addOwner(String world, String node, Player owner) {
		List<String> newOwners = getOwners(world, node);
		newOwners.add(owner.getName());
		XClaim.claimsYml.get(world).get().set(node+".owners", newOwners);
	}
	
	public static void addOwner(String node, Player owner) {
		addOwner(owner.getWorld().getName(), node, owner);
	}
	
	public static void delOwner(String world, String node, String owner) {
		List<String> newOwners = getOwners(world, node);
		newOwners.remove(owner);
		XClaim.claimsYml.get(world).get().set(node+".owners", newOwners);
	}
	
	public static List<String> getMembers(String world, String node){
		if(XClaim.claimsYml.get(world).get().getStringList(node+".members") != null) {
			return XClaim.claimsYml.get(world).get().getStringList(node+".members");
		}
		else {
			return new ArrayList<String>();
		}
	}
	
	public static void addMember(String world, String node, Player member) {
		List<String> newMembers = getMembers(world, node);
		newMembers.add(member.getName());
		XClaim.claimsYml.get(world).get().set(node+".members", newMembers);
	}
	
	public static void addMember(String node, Player member) {
		addMember(member.getWorld().getName(), node, member);
	}
	
	public static void delMember(String world, String node, String member) {
		List<String> newMembers = getMembers(world, node);
		newMembers.remove(member);
		XClaim.claimsYml.get(world).get().set(node+".members", newMembers);
	}
	
}
