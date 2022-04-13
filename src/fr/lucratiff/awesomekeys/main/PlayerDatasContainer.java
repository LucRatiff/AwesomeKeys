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
import java.io.Serializable;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import fr.lucratiff.awesomekeys.utils.ConsoleErrorMessage;
import fr.lucratiff.awesomekeys.utils.LockLocation;

public class PlayerDatasContainer implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Random random = new Random();
	private static final HashMap<UUID, PlayerDatasContainer> playersMap = new HashMap<>();
	private static boolean cleanerIsActive = false;
	private final UUID uuid;
	private HashMap<String, UUID> locks;
	private HashMap<UUID, ArrayList<LockLocation>> locksLocations;

	public PlayerDatasContainer(UUID uuid) {
		this.uuid = uuid;
		this.locks = new HashMap<>();
		this.locksLocations = new HashMap<>();
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public int getChestsNumber() {
		int nb = 0;
		for (ArrayList<LockLocation> list : locksLocations.values()) {
			nb += list.size();
		}
		return nb;
	}

	public HashMap<String, UUID> getLocks() {
		return locks;
	}

	public void setLocks(HashMap<String, UUID> locks) {
		this.locks = locks;
		savePlayerDatas(this);
	}
	
	public HashMap<UUID, ArrayList<LockLocation>> getLocksLocations() {
		return locksLocations;
	}
	
	public void setLocksLocation(HashMap<UUID, ArrayList<LockLocation>> locksLocations) {
		this.locksLocations = locksLocations;
		savePlayerDatas(this);
	}
	
	public static HashMap<UUID, PlayerDatasContainer> getPlayersMap() {
		return playersMap;
	}

	public static int getPlayerChestsNumber(UUID uuid) {
		return getPlayerDatas(uuid).getChestsNumber();
	}

	public static int getPlayerLocksNumber(UUID uuid) {
		return getPlayerDatas(uuid).getLocks().size();
	}

	public static boolean lockNameExists(UUID uuid, String name) {
		HashMap<String, UUID> locks = getPlayerDatas(uuid).getLocks();

		if (locks.size() == 0) {
			return false;
		}

		for (String s : locks.keySet()) {
			if (s.equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	public static void addLock(UUID uuid, String name) {
		UUID lockId = UUID.nameUUIDFromBytes(
				Bytes.concat(Longs.toByteArray(System.currentTimeMillis()), Longs.toByteArray(random.nextLong())));
		HashMap<String, UUID> locks = getPlayerDatas(uuid).getLocks();
		locks.put(name, lockId);
		getPlayerDatas(uuid).setLocks(locks);
	}
	
	public static void removeLockFromLockable(UUID owner, UUID lockId, Location l) {
		PlayerDatasContainer datas = getPlayerDatas(owner);
		HashMap<UUID, ArrayList<LockLocation>> map = datas.getLocksLocations();
		ArrayList<LockLocation> locations = map.get(lockId);
		for (int i = 0; i < locations.size(); i++) {
			if (locations.get(i).equals(l)) {
				locations.remove(i);
				datas.setLocksLocation(map);
				break;
			}
		}
	}

	public static String[] getLockNameAndId(UUID uuid, String name) {
		HashMap<String, UUID> locks = getPlayerDatas(uuid).getLocks();
		UUID lockId = locks.get(name);

		if (lockId == null) {
			for (Map.Entry<String, UUID> m : locks.entrySet()) {
				if (m.getKey().equalsIgnoreCase(name)) {
					name = m.getKey();
					lockId = m.getValue();
					break;
				}
			}
		}

		return new String[] { name, lockId.toString() };
	}

	private static PlayerDatasContainer getPlayerDatas(UUID uuid) {
		
		PlayerDatasContainer datas = playersMap.get(uuid);

		if (datas != null) {
			return datas;
		}

		File file = new File(AwesomeKeys.dataFolder + "players" + File.separator + uuid.toString());

		try {
			if (file.exists()) {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
				try {
					datas = (PlayerDatasContainer) input.readObject();
				} catch (ClassNotFoundException e) {
					Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
					file.delete();
					return null;
				} finally {
					if (input != null) {
						input.close();
					}
				}
			} else {
				file.createNewFile();
				datas = new PlayerDatasContainer(uuid);
				ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
				output.writeObject(datas);
				output.close();
			}
		} catch (FileSystemException e) {
			Bukkit.getLogger().severe(ConsoleErrorMessage.FILE_READ_PERMISSIONS + file.getAbsolutePath());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			Bukkit.getLogger().severe(ConsoleErrorMessage.CORRUPT_FILE_REMOVE + file.getAbsolutePath());
			file.delete();
			return null;
		}

		playersMap.put(uuid, datas);

		if (!cleanerIsActive) {
			cleanerIsActive = true;

			new BukkitRunnable() {
				@Override
				public void run() {
					if (playersMap.size() == 0) {
						cleanerIsActive = false;
						this.cancel();
						return;
					} else {
						playersMap.clear();
					}
				}
			}.runTaskTimer(AwesomeKeys.plugin, 36000L, 36000L);
		}

		return datas;
	}
	
	private static void savePlayerDatas(PlayerDatasContainer datas) {
		try {
			File file = new File(AwesomeKeys.dataFolder + "players" + File.separator + datas.getUuid());
			
			if (!file.exists()) {
				file.createNewFile();
			}
			
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
			output.writeObject(datas);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
