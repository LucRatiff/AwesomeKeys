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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.bukkit.scheduler.BukkitRunnable;

import fr.lucratiff.awesomekeys.main.AwesomeKeys;

public class LogsBufferFlusher extends BukkitRunnable {
	
	private static final int softLimitLogLinesPerFile = 1000;
	
	@Override
	public void run() {
		flush();
	}
	
	@SuppressWarnings("unchecked")
	public void flush() {
		if (LogsManager.logsList == null) {
			return;
		}
		
		long now = System.currentTimeMillis();
		
		ArrayList<LogsBuffer> buffers = (ArrayList<LogsBuffer>) LogsManager.logsList.clone();
		LogsManager.logsList.clear();
		
		for (int i = 0; i < buffers.size(); i++) {
			LogsBuffer lb = buffers.get(i);
			if (lb.getExpire() <= now) {
				int size = 0;
				for (LogsLocationContainer lc : lb.getLogs()) {
					for (LogsCollection ll : lc.getLockables()) {
						size += ll.getLogs().size();
					}
				}
				
				try {
					File file = new File(AwesomeKeys.dataFolder + File.separator + "logs" + File.separator
							+ lb.getFileName() + "." + lb.getFileNumber());
					ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
					output.writeObject(lb.getLogs());
					output.close();
					
					if (size >= softLimitLogLinesPerFile) {
						new File(AwesomeKeys.dataFolder + File.separator + "logs" + File.separator
								+ lb.getFileName() + "." + (lb.getFileNumber() + 1)).createNewFile();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				buffers.remove(i--);
			}
		}
		
		if (buffers.size() == 0 && LogsManager.logsList.size() == 0) {
			LogsManager.logsList = null;
			this.cancel();
		}
	}
}
