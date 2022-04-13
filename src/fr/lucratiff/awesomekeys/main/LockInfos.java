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

package fr.lucratiff.awesomekeys.main;

import java.io.Serializable;
import java.util.UUID;

public class LockInfos implements Serializable {

	private static final long serialVersionUID = 1L;
	private final UUID lockId;
	private final UUID owner;
	private String greeting;

	public LockInfos(UUID lockId, UUID owner) {
		this.lockId = lockId;
		this.owner = owner;
		this.greeting = null;
	}

	public UUID getLockId() {
		return lockId;
	}

	public UUID getOwner() {
		return owner;
	}
	
	public String getGreeting() {
		return greeting;
	}
	
	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}
}
