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

package fr.lucratiff.awesomekeys.logs;

import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.Location;

public class LogsLocationContainer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final int x;
	private final int y;
	private final int z;
	private final ArrayList<LogsCollection> lockables;
	
	public LogsLocationContainer(Location l) {
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
		this.lockables = new ArrayList<>();
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

	public ArrayList<LogsCollection> getLockables() {
		return lockables;
	}
	
	public void addNewLockable(LogsCollection lockable) {
		lockables.add(lockable);
	}
}
