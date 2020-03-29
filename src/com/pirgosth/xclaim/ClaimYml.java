package com.pirgosth.xclaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.pirgosth.xclaim.Coordinates.CoordinateFormatException;
import com.sk89q.worldedit.math.BlockVector3;

public class ClaimYml {
	public static Set<String> getNodes(String world){
		return main.claimsYml.get(world).get().getConfigurationSection("").getKeys(false);
	}
	public static void addClaim(String world, String node, String name, Location loc, List<String> owners) {
		main.claimsYml.get(world).get().set(node+".name", name);
		main.claimsYml.get(world).get().set(node+".home.x", loc.getBlockX());
		main.claimsYml.get(world).get().set(node+".home.y", loc.getBlockY());
		main.claimsYml.get(world).get().set(node+".home.z", loc.getBlockZ());
		main.claimsYml.get(world).get().set(node+".owners", owners);
	}
	public static void delClaim(String world, String node) {
		main.claimsYml.get(world).get().set(node, null);
	}
	
	public static ClaimData getClaim(String world, String node) {
		try {
			Coordinates co = Coordinates.extractCoordinates(node);
			return new ClaimData(co, node, main.claimsYml.get(world).get().getString(node + ".name"), ClaimYml.getOwners(world, node), ClaimYml.getMembers(world, node));
		}
		catch (CoordinateFormatException e) {
			return null;
		}
	}
	
	public static String getName(String world, String node) {
		if(main.claimsYml.get(world).get().getString(node + ".name") != null) {
			return main.claimsYml.get(world).get().getString(node + ".name");
		}
		else {
			return new String();
		}
	}
	
	public static BlockVector3 getHome(String world, String node) {
		return BlockVector3.at(main.claimsYml.get(world).get().getInt(node+".home.x"),
				main.claimsYml.get(world).get().getInt(node+".home.y"), main.claimsYml.get(world).get().getInt(node+".home.z"));
	}
	
	public static void setHome(String world, String node, Location location) {
		main.claimsYml.get(world).get().set(node + ".home.x", location.getBlockX());
		main.claimsYml.get(world).get().set(node + ".home.y", location.getBlockY());
		main.claimsYml.get(world).get().set(node + ".home.z", location.getBlockZ());
	}
	
	public static List<String> getOwners(String world, String node){
		if(main.claimsYml.get(world).get().getStringList(node+".owners") != null) {
			return main.claimsYml.get(world).get().getStringList(node+".owners");
		}
		else {
			return new ArrayList<String>();
		}
	}
	
	public static void addOwner(String world, String node, Player owner) {
		List<String> newOwners = getOwners(world, node);
		newOwners.add(owner.getName());
		main.claimsYml.get(world).get().set(node+".owners", newOwners);
	}
	
	public static void addOwner(String node, Player owner) {
		addOwner(owner.getWorld().getName(), node, owner);
	}
	
	public static void delOwner(String world, String node, String owner) {
		List<String> newOwners = getOwners(world, node);
		newOwners.remove(owner);
		main.claimsYml.get(world).get().set(node+".owners", newOwners);
	}
	
	public static List<String> getMembers(String world, String node){
		if(main.claimsYml.get(world).get().getStringList(node+".members") != null) {
			return main.claimsYml.get(world).get().getStringList(node+".members");
		}
		else {
			return new ArrayList<String>();
		}
	}
	
	public static void addMember(String world, String node, Player member) {
		List<String> newMembers = getMembers(world, node);
		newMembers.add(member.getName());
		main.claimsYml.get(world).get().set(node+".members", newMembers);
	}
	
	public static void addMember(String node, Player member) {
		addMember(member.getWorld().getName(), node, member);
	}
	
	public static void delMember(String world, String node, String member) {
		List<String> newMembers = getMembers(world, node);
		newMembers.remove(member);
		main.claimsYml.get(world).get().set(node+".members", newMembers);
	}
	
}
