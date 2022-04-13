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

import fr.lucratiff.awesomekeys.utils.LockLocation;

public class LockBuffer {
	
	private final LockInfos lock;
	private final String absolutePath;
	private final LockLocation location;
	private long expire;
	
	public LockBuffer(LockInfos lock, LockLocation location, String absolutePath) {
		this.lock = lock;
		this.location = location;
		this.absolutePath = absolutePath;
		this.expire = System.currentTimeMillis() + AwesomeKeys.bufferDuration;
	}

	public LockInfos getLock() {
		updateExpire();
		return lock;
	}

	public String getAbsolutePath() {
		updateExpire();
		return absolutePath;
	}
	
	public LockLocation getLockLocation() {
		updateExpire();
		return location;
	}
	
	public long getExpire() {
		return expire;
	}
	
	private void updateExpire() {
		expire = System.currentTimeMillis() + AwesomeKeys.bufferDuration;
	}
}
