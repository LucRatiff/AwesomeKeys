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

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;

public class LogFilters {
	
	public enum Group {
		CHUNK, WORLD, ALL
	}
	
	private Location l;
	private Group group;
	private String groupWorldName;
	private ArrayList<UUID> players = new ArrayList<>();
	private long from;
	private long to;
	
	public Location getLocation() {
		return l;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public String getGroupWorldName() {
		return groupWorldName;
	}
	
	public ArrayList<UUID> getPlayers() {
		return players;
	}
	
	public long getFrom() {
		return from;
	}
	
	public long getTo() {
		return to;
	}
	
	public void setLocation(Location l) {
		this.l = l;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public void setGroupWorldName(String name) {
		this.groupWorldName = name;
	}
	
	public void addPlayer(UUID player) {
		this.players.add(player);
	}
	
	public void setFrom(long from) {
		this.from = from;
	}
	
	public void setTo(long to) {
		this.to = to;
	}
}
