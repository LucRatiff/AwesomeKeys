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

import fr.lucratiff.awesomekeys.main.AwesomeKeys;

public class LogsBuffer {
	
	private final String fileName; //<world>/<chunk> without .<number>
	private final int fileNumber;
	private long expire;
	private final ArrayList<LogsLocationContainer> logs;
	
	public LogsBuffer(String fileName, int fileNumber, ArrayList<LogsLocationContainer> logs) {
		this.fileName = fileName;
		this.fileNumber = fileNumber;
		this.expire = System.currentTimeMillis() + AwesomeKeys.bufferDuration;
		this.logs = logs;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileNumber() {
		return fileNumber;
	}

	public long getExpire() {
		return expire;
	}

	public ArrayList<LogsLocationContainer> getLogs() {
		this.expire = System.currentTimeMillis() + AwesomeKeys.bufferDuration;
		return logs;
	}
}
