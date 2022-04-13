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
import java.util.UUID;

import fr.lucratiff.awesomekeys.utils.MessageType;

public class Log implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final long date;
	private final UUID player;
	private final String playerName;
	private final MessageType logType;
	private final String args;
	
	public Log(UUID player, String playerName, MessageType logType, String args) {
		this.date = System.currentTimeMillis();
		this.player = player;
		this.playerName = playerName;
		this.logType = logType;
		this.args = args;
	}

	public long getDate() {
		return date;
	}

	public UUID getPlayer() {
		return player;
	}

	public String getPlayerName() {
		return playerName;
	}

	public MessageType getLogType() {
		return logType;
	}

	public String getArgs() {
		return args;
	}
}
