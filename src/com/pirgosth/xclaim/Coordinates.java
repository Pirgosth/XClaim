package com.pirgosth.xclaim;

import com.sk89q.worldedit.math.BlockVector3;

public class Coordinates {
	
	@SuppressWarnings("serial")
	public static class CoordinateFormatException extends Exception{
		public CoordinateFormatException(String message) {
			super(message);
		}
	}
	
	public static Coordinates extractCoordinates(String line) throws CoordinateFormatException {
		String[] lines = line.split("/");
		if(lines.length != 2) {
			throw new CoordinateFormatException("Invalid Coordinates format");
		}
		if(lines[0].length() < 2 || lines[1].length() < 2) {
			throw new CoordinateFormatException("Invalid Coordinates format");
		}
		lines[0] = lines[0].substring(1, lines[0].length()-1);
		lines[1] = lines[1].substring(1, lines[1].length()-1);
		String[] pos1Str = lines[0].split(",");
		String[] pos2Str = lines[1].split(",");
		if(pos1Str.length != 3 || pos2Str.length != 3) {
			throw new CoordinateFormatException("Invalid Coordinates format");
		}
		Coordinates result = null;
		try {
			result = new Coordinates(BlockVector3.at(Integer.parseInt(pos1Str[0]), Integer.parseInt(pos1Str[1]), Integer.parseInt(pos1Str[2])),
					BlockVector3.at(Integer.parseInt(pos2Str[0]), Integer.parseInt(pos2Str[1]), Integer.parseInt(pos2Str[2])));
			return result;
		}
		catch(NumberFormatException e){
			throw new CoordinateFormatException("Invalid Coordinates format");
		}
	}
	
	private BlockVector3 pos1;
	private BlockVector3 pos2;
	public Coordinates(BlockVector3 pos1, BlockVector3 pos2) {
		this.pos1 = pos1;
		this.pos2 = pos2;
	}
	
	public BlockVector3 pos1() {
		return pos1;
	}
	public BlockVector3 pos2() {
		return pos2;
	}
}
