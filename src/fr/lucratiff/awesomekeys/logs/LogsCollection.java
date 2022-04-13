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
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import fr.lucratiff.awesomekeys.utils.MessageType;

public class LogsCollection implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final String type;
	private final ArrayList<Log> logs;
	
	public LogsCollection(Block block) {
		
		this.type = block.getType().name();
		this.logs = new ArrayList<>();
	}
	
	public String getType() {
		
		return type.toLowerCase().replace('_', ' ');
	}
	
	public ArrayList<Log> getLogs() {
		return logs;
	}
	
	public boolean sameType(Block block) {
		
		return block.getType().name().equals(type);
	}
	
	public void addLog(UUID player, MessageType logType, ItemStack[] items) {
		
		String args = null;
		
		if (items != null) {
			args = "";
			
			for (ItemStack item : items) {
				String name = item.getItemMeta().getDisplayName();
				args += "\n    " + item.getAmount() + " x " + item.getType().name().replace('_', ' ')
						+ ":" + item.getDurability() + (name != null ? " [" + name + "]" : "");
			}
		}
		
		logs.add(new Log(player, Bukkit.getOfflinePlayer(player).getName(), logType, args));
	}
}
