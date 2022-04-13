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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.lucratiff.awesomekeys.logs.LogFilters.Group;
import fr.lucratiff.awesomekeys.main.AwesomeKeys;
import fr.lucratiff.awesomekeys.utils.ConsoleErrorMessage;
import fr.lucratiff.awesomekeys.utils.MessageType;
import fr.lucratiff.awesomekeys.utils.MessagesMap;
import net.md_5.bungee.api.ChatColor;

public class LogsManager {
	
	public static volatile ArrayList<LogsBuffer> logsList = null;
	private static final String logsColor = ChatColor.DARK_AQUA + "";
	private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
	
	@SuppressWarnings("unchecked")
	public static void addLog(final UUID player, final Block block, final MessageType logType, ItemStack[] items) {
		
		Location l = block.getLocation();
		String worldName = l.getWorld().getName();
		String folder = AwesomeKeys.dataFolder + "logs" + File.separator + worldName;
		
		try {
			if (!Files.exists(FileSystems.getDefault().getPath(folder))) {
				new File(folder).mkdir();
			}
			
			String chunk = l.getChunk().getX() + " " + l.getChunk().getZ();
			String name = worldName + File.separator + chunk;
			ArrayList<LogsLocationContainer> list = null;
			File lastFile = null;
			boolean isBuffered = false;
			
			if (logsList != null) {
				for (LogsBuffer lb : logsList) {
					if (lb.getFileName().equals(name)) {
						isBuffered = true;
						list = lb.getLogs();
						break;
					}
				}
			}
			
			if (!isBuffered) {
				String fileName = folder + File.separator + chunk + ".";
				int fileNumber = 0;
				File file = new File(fileName + fileNumber);
				
				if (file.exists()) {
					while ((lastFile = new File(fileName + ++fileNumber)).exists());
					lastFile = new File(fileName + --fileNumber);
					
					ObjectInputStream input = new ObjectInputStream(new FileInputStream(lastFile));
					
					try {
						list = (ArrayList<LogsLocationContainer>) input.readObject();
					} catch (ClassNotFoundException e) {
						Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REPLACE + lastFile.getAbsolutePath());
						list = new ArrayList<>();
					} finally {
						if (input != null) {
							input.close();
						}
					}
				} else {
					file.getParentFile().mkdirs();
					file.createNewFile();
					lastFile = file;
					list = new ArrayList<>();
				}
				
				if (logsList == null) {
					logsList = new ArrayList<>();
				}
				logsList.add(new LogsBuffer(name, fileNumber, list));
				new LogsBufferFlusher().runTaskTimerAsynchronously(AwesomeKeys.plugin,
						AwesomeKeys.bufferDuration, AwesomeKeys.bufferDuration);
			}
			
			int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();
			boolean found = false;
			
			for (LogsLocationContainer llc : list) {
				if (llc.getX() == x && llc.getY() == y && llc.getZ() == z) {
					found = true;
					LogsCollection last = llc.getLockables().get(llc.getLockables().size() - 1);
					
					if (last.sameType(block)) {
						last.addLog(player, logType, items);
					} else {
						LogsCollection lc = new LogsCollection(block);
						lc.addLog(player, logType, items);
						llc.addNewLockable(lc);
					}
					
					break;
				}
			}
			
			if (!found) {
				LogsLocationContainer llc = new LogsLocationContainer(l);
				LogsCollection lc = new LogsCollection(block);
				lc.addLog(player, logType, items);
				llc.addNewLockable(lc);
				list.add(llc);
			}
			
			if (!isBuffered) {
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(lastFile));
				output.writeObject(list);
				output.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getLogs(final UUID sender, final LogFilters filters, final String fileDest) {
		
		new BukkitRunnable() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Location l = null;
				ArrayList<String> worlds = new ArrayList<>();
				String chunk = null;
				
				if (filters.getGroup() != null) {
					if (filters.getGroup() == Group.CHUNK) {
						Location loc = filters.getLocation();
						worlds.add(loc.getWorld().getName());
						chunk = loc.getChunk().getX() + " " + loc.getChunk().getZ();
					} else if (filters.getGroup() == Group.WORLD) {
						worlds.add(filters.getGroupWorldName());
					} else {
						for (World w : Bukkit.getWorlds()) {
							worlds.add(w.getName());
						}
					}
				} else if (filters.getLocation() != null) {
					l = filters.getLocation();
				} else {
					for (World w : Bukkit.getWorlds()) {
						worlds.add(w.getName());
					}
				}
				
				ArrayList<File> files = new ArrayList<>();
				String prefix = AwesomeKeys.dataFolder + "logs" + File.separator;
				
				if (l != null) {
					int i = 0;
					String name = prefix + l.getWorld() + File.separator + l.getChunk().getX() + " " + l.getChunk().getZ() + ".";
					File file = null;
					while ((file = new File(name + i++)).exists()) {
						files.add(file);
					}
				} else if (chunk != null) {
					int i = 0;
					String name = prefix + worlds.get(0) + File.separator + chunk + ".";
					File file = null;
					while ((file = new File(name + i++)).exists()) {
						files.add(file);
					}
				} else if (worlds.size() > 0) {
					for (String w : worlds) {
						File folder = new File(prefix + w);
						if (folder.exists()) {
							for (File f : folder.listFiles()) {
								files.add(f);
							}
						}
					}
				} else {
					for (World w : Bukkit.getWorlds()) {
						File folder = new File(prefix + w.getName());
						if (folder.exists()) {
							for (File f : folder.listFiles()) {
								files.add(f);
							}
						}
					}
				}
				
				int x = l != null ? l.getBlockX() : 0;
				int y = l != null ? l.getBlockY() : 0;
				int z = l != null ? l.getBlockZ() : 0;
				ArrayList<UUID> players = filters.getPlayers();
				long from = filters.getFrom();
				long to = filters.getTo();
				
				ArrayList<String> logs = new ArrayList<>();
				
				for (int i = 0; i < files.size(); i++) {
					File file = files.get(i);
					ArrayList<LogsLocationContainer> list = null;
					try {
						ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
						try {
							list = (ArrayList<LogsLocationContainer>) input.readObject();
						} catch (ClassNotFoundException e) {
							Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
							file.delete();
							continue;
						} finally {
							if (input != null) {
								input.close();
							}
						}
					} catch (FileSystemException e) {
						Bukkit.getLogger().severe(ConsoleErrorMessage.FILE_READ_PERMISSIONS + file.getAbsolutePath());
						e.printStackTrace();
					} catch (IOException e) {
						Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
						file.delete();
						continue;
					}
					
					String world = file.getParentFile().getName();
					Bukkit.getLogger().info(world); //TODO
					int numberPosition = file.getName().indexOf(".");
					int fileNumber = Integer.valueOf(file.getName().substring(numberPosition + 1));
					String shortFileName = file.getName().substring(0, numberPosition);
					int j = fileNumber;
					
					for (LogsLocationContainer llc : list) { //TODO vérifier
						if (l != null) {
							if (llc.getX() == x && llc.getY() == y && llc.getZ() == z) {
								if (filterByPlayersAndTime(players, from, to, world, x + " " + y + " " + z, llc.getLockables(),
										logs, fileDest != null)) {
									while ((new File(shortFileName + j++)).exists());
									if (j - 1 > fileNumber) {
										i += j - 1 - fileNumber;
									}
								}
								break;
							}
						} else {
							if (filterByPlayersAndTime(players, from, to, world, llc.getX() + " " + llc.getY() + " " + llc.getZ(),
									llc.getLockables(), logs, fileDest != null)) {
								while ((new File(shortFileName + j++)).exists());
								if (j - 1 > fileNumber) {
									i += j - 1 - fileNumber;
								}
							}
						}
					}
				}
				
				final String[] logsArray = logs.toArray(new String[logs.size()]);
				
				if (fileDest != null) {
					File file = new File(fileDest);
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
						for (String s : logsArray) {
							bw.append(s);
						}
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				} else {
					for (String s : logsArray) {
						Bukkit.getLogger().info(s);
					}
				}
				
				new BukkitRunnable() {
					@Override
					public void run() {
						Player player = Bukkit.getPlayer(sender);
						
						if (player != null && player.isOnline()) {
							if (fileDest != null) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_LOGS_TO_FILE,
										new String[] { fileDest }));
							} else {
								if (logsArray.length > 0) {
									player.sendMessage(MessagesMap.getMessage(MessageType.LOG_DISPLAY, null));
									player.sendMessage(logsArray);
								} else {
									player.sendMessage(MessagesMap.getMessage(MessageType.LOG_NO_LOG, null));
								}
								
							}
						}
					}
				}.runTask(AwesomeKeys.plugin);
			}
			
		}.runTaskAsynchronously(AwesomeKeys.plugin);
	}
	
	private static boolean filterByPlayersAndTime(ArrayList<UUID> players, long from, long to, String world, String coos,
			ArrayList<LogsCollection> logsToFilter, ArrayList<String> logsToCompile, boolean toFile) {
		
		for (LogsCollection ll : logsToFilter) {
			logsToCompile.add(toFile ? "" : logsColor + "[Block: " + ll.getType() + "]");
			ArrayList<Log> logs = ll.getLogs();
			
			for (Log log : logs) {
				if (from <= log.getDate()) {
					if (to != 0 && to < log.getDate()) {
						return true;
					}
					if (players != null) {
						UUID player = log.getPlayer();
						boolean found = false;
						for (UUID u : players) {
							if (player.equals(u)) {
								found = true;
								break;
							}
						}
						if (!found) {
							continue;
						}
					}
					logsToCompile.add(toFile ? "" : logsColor + "- [" + format.format(new Date(log.getDate())) + "] "
							+ log.getPlayerName() + "[" + log.getPlayer() + "]: "
							+ MessagesMap.getMessage(log.getLogType(), null) + log.getArgs());
				}
			}
		}
		
		return false;
	}
}
