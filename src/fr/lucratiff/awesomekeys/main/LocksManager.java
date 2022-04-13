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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.lucratiff.awesomekeys.logs.LogsManager;
import fr.lucratiff.awesomekeys.utils.ConsoleErrorMessage;
import fr.lucratiff.awesomekeys.utils.LockLocation;
import fr.lucratiff.awesomekeys.utils.MessageType;
import fr.lucratiff.awesomekeys.utils.MessagesMap;

public class LocksManager {
	
	private static ArrayList<LockBuffer> locksList = null;
	
	public static void addOrRemoveLockFromLockable(UUID lockId, String lockName, Player player, Block block, Location doubleChest,
			boolean addLock) {
		UUID owner = player.getUniqueId();
		Location l = block.getLocation();
		String folder = AwesomeKeys.dataFolder + "locks" + File.separator + l.getWorld().getName();
		File file = new File(folder + File.separator + l.getChunk().getX() + " " + l.getChunk().getZ()
				+ File.separator + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + ".lock");
		File[] files = doubleChest == null ? new File[] { file } : new File[] { file, new File(
				folder + File.separator + doubleChest.getChunk().getX() + " " + doubleChest.getChunk().getZ()
				+ File.separator + doubleChest.getBlockX() + " " + doubleChest.getBlockY() + " "
				+ doubleChest.getBlockZ() + ".lock") };
		int iterationLength = doubleChest == null ? 1 : 2;
		
		for (int i = 0; i < iterationLength; i++) {
			
			file = files[i];
			
			if (file.exists()) {
				try {
					ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
					try {
						LockInfos lock = (LockInfos) input.readObject();
						if (!lock.getOwner().equals(owner)) {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_BELONGS_TO_ANOTHER_PLAYER, null));
							return;
						}
					} catch (ClassNotFoundException e) {
						Bukkit.getLogger().severe((addLock ? ConsoleErrorMessage.CORRUPT_FILE_REPLACE :
								ConsoleErrorMessage.CORRUPT_FILE_REMOVE) + file.getAbsolutePath());
					} finally {
						if (input != null) {
							input.close();
						}
					}
				} catch (IOException e) {
					Bukkit.getLogger().severe((addLock ? ConsoleErrorMessage.CORRUPT_FILE_REPLACE :
						ConsoleErrorMessage.CORRUPT_FILE_REMOVE) + file.getAbsolutePath());
				}
			} else if (!addLock && i == 0) {
				player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NO_LOCK_TO_REMOVE, null));
				return;
			}
			
			file.delete();
			BlockState bs = block.getState();

			if (bs instanceof Nameable) {
				Nameable container = (Nameable) bs;
				container.setCustomName(addLock ? AwesomeKeys.lockPrefix + lockName : null);
				bs.update();
			}
			
			if (!addLock) {
				PlayerDatasContainer.removeLockFromLockable(owner, lockId, i == 0 ? l : doubleChest);
				if (AwesomeKeys.logsEnabled) {
					LogsManager.addLog(player.getUniqueId(), block, MessageType.LOG_LOCK_REMOVED, null);
				}
				
			} else {
				try {
					file.getParentFile().mkdirs();
					file.createNewFile();
					LockInfos lock = new LockInfos(lockId, owner);
					ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
					output.writeObject(lock);
					output.close();
					if (AwesomeKeys.logsEnabled) {
						LogsManager.addLog(player.getUniqueId(), block, MessageType.LOG_LOCK_ADDED, null);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
		
		player.sendMessage(MessagesMap.getMessage((addLock ? MessageType.CMD_SUCCESS_BLOCK_LOCKED :
			MessageType.CMD_SUCCESS_LOCK_REMOVED), new String[] { lockName }));
	}
	
	public static void purgeFromPlayer(final UUID sender, final UUID target) {
		
		final PlayerDatasContainer playerDatas = PlayerDatasContainer.getPlayersMap().remove(target);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				File file = new File(AwesomeKeys.dataFolder + "players" + File.separator + target);
				if (!file.exists()) {
					Bukkit.getPlayer(sender).sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PLAYER_DATAS_NOT_FOUND, null));
					return;
				}
				PlayerDatasContainer datas = playerDatas;
				if (datas == null) {
					try {
						ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
						try {
							datas = (PlayerDatasContainer) input.readObject();
						} catch (ClassNotFoundException e) {
							Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
							Bukkit.getPlayer(sender).sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PLAYER_DATAS_NOT_FOUND, null));
							file.delete();
							return;
						} finally {
							if (input != null) {
								input.close();
							}
						}
					} catch (IOException e) {
						Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
						Bukkit.getPlayer(sender).sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PLAYER_DATAS_NOT_FOUND, null));
						file.delete();
						return;
					}
				}
				file.delete();
				datas.getLocksLocations().values().forEach((list) -> list.forEach((location) -> new File(location.getFileName()).delete()));
				
				new BukkitRunnable() {
					@Override
					public void run() {
						Player player = Bukkit.getPlayer(sender);
						if (player != null && player.isOnline()) {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_FILES_FROM_PLAYER_PURGED,
									new String[] { Bukkit.getOfflinePlayer(target).getName() }));
						}
					}
				}.runTask(AwesomeKeys.plugin);
			}
		}.runTaskAsynchronously(AwesomeKeys.plugin);
	}
	
	public static void purgeAllFiles(UUID sender) {
		
		PlayerDatasContainer.getPlayersMap().clear();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					AwesomeKeys.deleteFolder(AwesomeKeys.dataFolder + "locks");
					AwesomeKeys.deleteFolder(AwesomeKeys.dataFolder + "players");
					
					new BukkitRunnable() {
						@Override
						public void run() {
							Player player = Bukkit.getPlayer(sender);
							if (player != null && player.isOnline()) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_FILES_PURGED, null));
							}
						}
					}.runTask(AwesomeKeys.plugin);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(AwesomeKeys.plugin);
	}
	
	public static LockInfos getLock(Location l, File file) {
		if (locksList == null) {
			initializeLocksBuffers();
		}
		
		for (LockBuffer lb : locksList) {
			if (lb.getLockLocation().equals(l)) {
				return lb.getLock();
			}
		}
		
		if (file == null) {
			file = new File(AwesomeKeys.dataFolder + "locks" + File.separator + l.getWorld().getName() + File.separator
					+ l.getChunk().getX() + " " + l.getChunk().getZ() + File.separator
					+ l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + ".lock");
		}
		
		if (file.exists()) {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
				try {
					LockInfos lock = (LockInfos) input.readObject();
					locksList.add(new LockBuffer(lock, new LockLocation(l), file.getAbsolutePath()));
					return lock;
				} catch (ClassNotFoundException e) {
					Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
					file.delete();
				} finally {
					if (input != null) {
						input.close();
					}
				}
			} catch (IOException e) {
				Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
				file.delete();
			}
		}
		
		return null;
	}
	
	public static LockInfos getLock(Location l) {
		return getLock(l, null);
	}
	
	public static void saveLockChanges(Location l) {
		if (locksList == null) {
			initializeLocksBuffers();
		}
		
		LockLocation lockLocation = new LockLocation(l);
		
		for (LockBuffer lb : locksList) {
			if (lb.getLockLocation().equals(lockLocation)) {
				try {
					ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(new File(lb.getAbsolutePath())));
					output.writeObject(lb.getLock());
					output.close();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		File file = new File(AwesomeKeys.dataFolder + "locks" + l.getWorld().getName() + File.separator
				+ l.getChunk().getX() + " " + l.getChunk().getZ() + File.separator
				+ l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + ".lock");
		
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			try {
				LockInfos lock = (LockInfos) input.readObject();
				locksList.add(new LockBuffer(lock, lockLocation, file.getAbsolutePath()));
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
				output.writeObject(lock);
				output.close();
			} catch (ClassNotFoundException e) {
				Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
				file.delete();
			} finally {
				if (input != null) {
					input.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void initializeLocksBuffers() {
		locksList = new ArrayList<>();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				
				for (int i = 0; i < locksList.size(); i++) {
					if (now > locksList.get(i).getExpire()) {
						locksList.remove(i--);
					}
				}
				
				if (locksList.size() == 0) {
					locksList = null;
					this.cancel();
				}
			}
		}.runTaskTimer(AwesomeKeys.plugin, AwesomeKeys.bufferDuration, AwesomeKeys.bufferDuration);
	}
	
	public static void access(Player player, Block block, ItemStack key, PlayerInteractEvent event) {
		LockInfos lock = getLock(block.getLocation());
		if (lock == null) {
			return;
		}
		
		if (!AwesomeKeys.isAKey(key)) {
			event.setCancelled(true);
			player.sendMessage(MessagesMap.getMessage(MessageType.ACTION_NO_KEY, null));
			return;
		}
		
		String lore1 = key.getItemMeta().getLore().get(0);
		if (lore1.equals(AwesomeKeys.masterKeyName)) {
			if (AwesomeKeys.bypassProtection) {
				event.setCancelled(false);
			}
			//TODO particules pour la master key
			return;
		}
		if (lore1.length() < AwesomeKeys.keyLore[0].length() + 1
				|| !lore1.substring(0, AwesomeKeys.keyLore[0].length()).equals(AwesomeKeys.keyLore[0])) {
			event.setCancelled(true);
			player.sendMessage(MessagesMap.getMessage(MessageType.ACTION_WRONG_KEY, null));
			return;
		}
		
		UUID keyId = UUID.fromString(lore1.substring(AwesomeKeys.keyLore[0].length()));
		
		if (lock.getLockId().equals(keyId)) {
			if (AwesomeKeys.bypassProtection) {
				event.setCancelled(false);
			}
			//TODO générer des particules
		} else {
			event.setCancelled(true);
			//TODO générer des particules
			player.sendMessage(MessagesMap.getMessage(MessageType.ACTION_WRONG_KEY, null));
		}
	}
	
	public static void destroy(Location l) {
		File file = new File(AwesomeKeys.dataFolder + "locks" + l.getWorld().getName() + File.separator
				+ l.getChunk().getX() + " " + l.getChunk().getZ() + File.separator
				+ l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ());
		if (file.exists()) {
			LockInfos lock = getLock(l, file);
			UUID lockId = lock.getLockId();
			PlayerDatasContainer.removeLockFromLockable(lock.getOwner(), lockId, l);
			file.delete();
			
			if (locksList != null) {
				for (int i = 0; i < locksList.size(); i++) {
					if (locksList.get(i).getLock().getLockId().equals(lockId)) {
						locksList.remove(i);
						break;
					}
				}
			}
		}
	}
	
	public static void placeShulker(UUID owner, Location l, Nameable nameable) {
		if (nameable.getCustomName().substring(0, AwesomeKeys.lockPrefix.length()).equals(AwesomeKeys.lockPrefix)) {
			String[] lockNameAndId = PlayerDatasContainer.getLockNameAndId(owner,
					nameable.getCustomName().substring(AwesomeKeys.lockPrefix.length()));
			if (lockNameAndId == null) {
				return;
			}
			
			LockInfos lock = new LockInfos(UUID.fromString(lockNameAndId[1]), owner);
			File file = new File(AwesomeKeys.dataFolder + "locks" + l.getWorld().getName() + File.separator
				+ l.getChunk().getX() + " " + l.getChunk().getZ() + File.separator
				+ l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ() + ".lock");
			
			try {
				file.createNewFile();
				
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
				output.writeObject(lock);
				output.close();
				
				if (locksList == null) {
					locksList = new ArrayList<LockBuffer>();
				}
				
				locksList.add(new LockBuffer(lock, new LockLocation(l), file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//TODO particules
		}
	}
}
