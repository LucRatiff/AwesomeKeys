/*
 * AwesomeKeys, a Minecraft server plugin wich adds locks and keys management
 * Copyright (C) LucRatiff
 * 
 * This file is part of AwesomeKeys.
 *
 * AwesomeKeys is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * AwesomeKeys is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with AwesomeKeys. If not, see <https://www.gnu.org/licenses/>
 */

package fr.lucratiff.awesomekeys.utils;

import java.io.File;

import org.bukkit.Location;

import fr.lucratiff.awesomekeys.main.AwesomeKeys;

public class LockLocation {
	
	private final String world;
	private final int x;
	private final int y;
	private final int z;
	
	public LockLocation(String world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public LockLocation(Location l) {
		this.world = l.getWorld().getName();
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
	}
	
	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
	
	public String getFileName() {
		return AwesomeKeys.dataFolder + "locks" + File.separator + world + File.separator + (x / 16) + " " + (z / 16)
				+ File.separator + x + " " + y + " " + z + ".lock";
	}
	
	public boolean equals(Location l) {
		return l.getWorld().getName().equals(world) && l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z;
	}
}
