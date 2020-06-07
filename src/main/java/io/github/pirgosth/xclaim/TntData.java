package io.github.pirgosth.xclaim;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TntData{
	private Location l = null;
	private Player p = null;
	
	public TntData(Location l, Player p) {
		this.l = l;
		this.p = p;
	}
	
	public Location getLocation() {
		return l;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public boolean equals(Location l) {
		return (this.l.getX() == l.getBlockX() && this.l.getY() == l.getBlockY() && this.l.getZ() == l.getBlockZ());
	}
	
	public boolean equals(Player p) {
		return this.p == p;
	}
	
	public static TntData getTntData(Set<TntData> Tnts, Location l) {
		for(TntData tnt: Tnts) {
			if(tnt.equals(l)) {
				return tnt;
			}
		}
		return null;
	}
	
}