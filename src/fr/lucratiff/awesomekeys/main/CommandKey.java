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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.lucratiff.awesomekeys.logs.LogFilters;
import fr.lucratiff.awesomekeys.logs.LogsManager;
import fr.lucratiff.awesomekeys.logs.LogFilters.Group;
import fr.lucratiff.awesomekeys.utils.ChestPlacement;
import fr.lucratiff.awesomekeys.utils.MessageType;
import fr.lucratiff.awesomekeys.utils.MessagesMap;
import fr.lucratiff.awesomekeys.utils.PurgeUnlinkedFiles;
import net.md_5.bungee.api.ChatColor;

public class CommandKey implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (sender instanceof Player) {
			
			if (args.length < 1) {
				return false;
			}

			Player player = (Player) sender;
			
			if (args[0].equalsIgnoreCase("new")) {
				if (args.length != 3) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NEW, null));
					return true;
				}

				if (args[1].equalsIgnoreCase("lock")) {
					if (!player.hasPermission("awesomekeys.lock")) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
						return true;
					}

					if (AwesomeKeys.playerLocksLimitation > 0 && PlayerDatasContainer
							.getPlayerLocksNumber(player.getUniqueId()) >= AwesomeKeys.playerLocksLimitation) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOCKS_LIMITATION, null));
						return true;
					}

					if (args[2].length() > 50) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NAME_LENGTH, null));
						return true;
					}

					if (PlayerDatasContainer.lockNameExists(player.getUniqueId(), args[2])) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOCK_NAME_EXISTS, null));
						return true;
					}

					PlayerDatasContainer.addLock(player.getUniqueId(), args[2]);

					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_LOCK_CREATED,
							new String[] { args[2], args[2] }));
				} else if (args[1].equalsIgnoreCase("key")) {
					if (!player.hasPermission("awesomekeys.key")) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
						return true;
					}

					ItemStack item = player.getInventory().getItemInMainHand();

					if (!couldBeAKey(player, item)) {
						return true;
					}

					if (!PlayerDatasContainer.lockNameExists(player.getUniqueId(), args[2])) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOCK_NAME_NOT_FOUND, null));
						return true;
					}

					if (args[2].length() > 50) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NAME_LENGTH, null));
						return true;
					}

					String[] lockNameAndId = PlayerDatasContainer.getLockNameAndId(player.getUniqueId(), args[2]);
					ArrayList<String> lore = new ArrayList<>();
					lore.add(AwesomeKeys.keyLore[0] + lockNameAndId[1]);
					lore.add(AwesomeKeys.keyLore[1] + "----");
					ItemMeta im = item.getItemMeta();
					im.setDisplayName(AwesomeKeys.keyPrefix + lockNameAndId[0]);
					im.setLore(lore);
					item.setItemMeta(im);

					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_KEY_CREATED, null));
				} else {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NEW, null));
				}
			} else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
				if (!player.hasPermission("awesomekeys.lock")) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
					return true;
				}

				Block block = target(player);

				if (block == null || !AwesomeKeys.lockablesEnabled.contains(block.getType())) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NOT_LOCKABLE, null));
					return true;
				}

				if (!PlayerDatasContainer.lockNameExists(player.getUniqueId(), args[1])) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOCK_NAME_NOT_FOUND, null));
					return true;
				}
				
				String[] lockNameAndId = PlayerDatasContainer.getLockNameAndId(player.getUniqueId(), args[1]);
				boolean add = args[0].equalsIgnoreCase("add");
				
				if (AwesomeKeys.bypassProtection) {
					LocksManager.addOrRemoveLockFromLockable(UUID.fromString(lockNameAndId[1]), lockNameAndId[0], player,
							block, ChestPlacement.getDoubleChestSecondLocation(block), add);
				} else {
					if (Listeners.addOrRemoveLockFromLockablePreparation == null) {
						Listeners.addOrRemoveLockFromLockablePreparation = new HashMap<>();
					}
					Object[] argsList = new Object[] { UUID.fromString(lockNameAndId[1]), lockNameAndId[0],
							ChestPlacement.getDoubleChestSecondLocation(block), add };
					Listeners.addOrRemoveLockFromLockablePreparation.put(player.getUniqueId(), argsList);
					Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, null, block, BlockFace.EAST_NORTH_EAST));
				}
			} else if (args[0].equalsIgnoreCase("greeting")) {
				if (!player.hasPermission("awesomekeys.lock.own")) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
					return true;
				}
				
				Block block = target(player);
				
				if (!AwesomeKeys.lockablesEnabled.contains(block.getType())) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NOT_LOCKABLE, null));
					return true;
				}
				
				if (block.getState() instanceof Container) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_GREETING_ON_CONTAINER, null));
					return true;
				}
				
				LockInfos lock = LocksManager.getLock(block.getLocation());
				
				if (lock == null) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NO_LOCK_ON_LOCKABLE, null));
					return true;
				}
				
				if (!lock.getOwner().equals(player.getUniqueId())) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_BELONGS_TO_ANOTHER_PLAYER, null));
					return true;
				}
				
				if (args.length < 2) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_GREETING, null));
					return true;
				}
				
				if (args[1].equalsIgnoreCase("none")) {
					if (lock.getGreeting() == null) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_ALREADY_NO_GREETING, null));
						return true;
					}
					
					lock.setGreeting(null);
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_GREETING_REMOVED, null));
				} else {
					if (args.length < 3) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_GREETING, null));
						return true;
					}
					
					if (args[1].length() != 1 && !"0123456789abcdef".contains(args[1])) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_GREETING_COLOR_CODE, null));
						return true;
					}
					
					String greeting = "";
					
					for (int i = 2; i < args.length; i++) {
						greeting += args[i] + " ";
					}
					
					greeting = greeting.substring(0, greeting.length() - 1);
					
					if (greeting.length() > 100) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_GREETING_LENGTH_LIMIT, null));
						return true;
					}
					
					lock.setGreeting(greeting);
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_GREETING_ADDED, null));
				}
				LocksManager.saveLockChanges(block.getLocation());
			}
			else if (args[0].equalsIgnoreCase("editkey")) {
				if (args.length < 3) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_EDITKEY, null));
					return true;
				}
				
				ItemStack item = player.getInventory().getItemInMainHand();
				
				if (!AwesomeKeys.isAKey(item)) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_EMPTY_HAND, null));
					return true;
				}
				
				try {
					int line = Integer.valueOf(args[1]);
					
					if (line < 1 || line > 4) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_DESCRIPTION_LINE, null));
						return true;
					}
					
					String description = "";
					
					for (int i = 2; i < args.length; i++) {
						description += args[i] + " ";
					}
					
					if (description.length() > 51) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_DESCRIPTION_LENGTH, null));
						return true;
					}
					
					ItemMeta im = item.getItemMeta();
					List<String> lore = im.getLore();
					if (line == 1) {
						lore.set(line, AwesomeKeys.keyLore[1] + description);
					} else {
						description = ChatColor.YELLOW + description;
						if (lore.size() - 1 < line) {
							lore.add(description);
						} else {
							lore.set(line, description);
						}
					}
					im.setLore(lore);
					item.setItemMeta(im);
					
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_DESCRIPTION_CHANGED, null));
				} catch (NumberFormatException e) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NOT_A_NUMBER, null));
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_EDITKEY, null));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("master")) {
				if (!player.hasPermission("awesomekeys.master")) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
					return true;
				}
				
				ItemStack item = player.getInventory().getItemInMainHand();
				
				if (!couldBeAKey(player, item)) {
					return true;
				}
				
				ItemMeta im = item.getItemMeta();
				ArrayList<String> lore = new ArrayList<>();
				lore.add(AwesomeKeys.keyPrefix + AwesomeKeys.masterKeyName);
				lore.add(AwesomeKeys.keyLore[1] + "----");
				im.setLore(lore);
				im.setDisplayName(AwesomeKeys.masterKeyName);
				item.setItemMeta(im);
				
				player.sendMessage(MessagesMap.getMessage(MessageType.CMD_SUCCESS_MASTER_KEY, null));
			} else if (args[0].equalsIgnoreCase("purge")) {
				if (!player.hasPermission("awesomekeys.purge")) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
					return true;
				}
				
				if (args.length != 2 && args.length != 3) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PURGE, null));
					return true;
				}
				
				if (args[1].equalsIgnoreCase("unlinked")) {
					if (args.length != 2) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PURGE, null));
						return true;
					}
					if (PurgeUnlinkedFiles.prepare(player.getUniqueId())) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_INFO_PURGE_UNLINKED_DURATION, null));
					} else {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PURGE_UNLINKED_ALREADY_STARTED, null));
					}
				} else if (args[1].equalsIgnoreCase("all")) {
					if (args.length != 2) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PURGE, null));
						return true;
					}
					LocksManager.purgeAllFiles(player.getUniqueId());
				} else if (args[1].equalsIgnoreCase("player")) {
					if (args.length != 3) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PURGE, null));
						return true;
					}
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
					if (!target.hasPlayedBefore()) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PLAYER_NOT_FOUND, null));
						return true;
					}
					LocksManager.purgeFromPlayer(player.getUniqueId(), target.getUniqueId());
				} else {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PURGE, null));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("logs")) {
				if (!player.hasPermission("awesomekeys.logs")) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
					return true;
				}
				
				if (args.length == 1) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS, null));
					return true;
				}
				
				String fileDest = null;
				
				if (args.length > 2 && args[args.length - 2].equals(">")) {
					if (!player.hasPermission("awesomekeys.logs.print")) {
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
						return true;
					}
					
					fileDest = AwesomeKeys.dataFolder + args[args.length - 1];
					try {
						File file = new File(fileDest);
						if (file.exists()) {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_FILE_EXISTS, null));
						}
						file.createNewFile();
						String[] tempArray = new String[args.length - 2];
						for (int i = 0; i < tempArray.length; i++) {
							tempArray[i] = args[i];
						}
						args = tempArray;
					} catch (IOException e) {
						e.printStackTrace();
						player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_FILE_EXCEPTION, null));
						return true;
					}
				} else if (!player.hasPermission("awesomekeys.logs.read")) {
					player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PERMISSION, null));
					return true;
				}
				
				LogFilters filters = new LogFilters();
				
				for (int i = 1; i < args.length; i++) {
					String filter = args[i].toLowerCase();
					
					if (filter.equals("block")) {
						if (i + 3 < args.length) {
							try {
								boolean withWorld = !"0123456789-".contains(args[i + 1].substring(0, 1)) && i + 4 < args.length;
								World world = withWorld ? Bukkit.getWorld(args[i + 1]) : player.getWorld();
								if (world == null) {
									player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_WORLD_NOT_FOUND, null));
									return true;
								}
								filters.setLocation(new Location(world,
										Integer.valueOf(args[i + (withWorld ? 2 : 1)]),
										Integer.valueOf(args[i + (withWorld ? 3 : 2)]),
										Integer.valueOf(args[i + (withWorld ? 4 : 3)])));
								i += withWorld ? 5 : 4;
							} catch (NumberFormatException e) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_NOT_A_NUMBER, null));
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_BLOCK, null));
								return true;
							}
						} else {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_BLOCK, null));
							return true;
						}
					} else if (filter.equals("target")) {
						Block block = target(player);

						if (block == null) {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_BLOCK_TARGET, null));
							return true;
						}
						
						filters.setLocation(block.getLocation());
					} else if (filter.equals("group")) {
						if (i + 1 < args.length) {
							Group group = Group.valueOf(args[i + 1].toUpperCase());
							if (group == null) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_GROUP, null));
								return true;
							}
							if (group == Group.WORLD) {
								if (i + 2 < args.length) {
									World world = Bukkit.getWorld(args[i + 2]);
									if (world == null) {
										player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_WORLD_NOT_FOUND, null));
										return true;
									}
									filters.setGroupWorldName(world.getName());
								} else {
									player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_GROUP_WORLD_MISSING, null));
									return true;
								}
								i += 2;
							} else {
								i++;
							}
							filters.setGroup(group);
						} else {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_GROUP, null));
							return true;
						}
					} else if (filter.equals("player")) {
						if (i + 1 < args.length) {
							OfflinePlayer op = null;
							if (args[i + 1].length() == 32) {
								try {
									op = Bukkit.getOfflinePlayer(UUID.fromString(args[i + 1]));
								} catch (IllegalArgumentException e) {
									player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_PLAYER_UUID, null));
									return true;
								}
							} else {
								op = Bukkit.getOfflinePlayer(args[i + 1]);
							}
							
							if (!op.hasPlayedBefore()) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_PLAYER_NOT_FOUND, null));
								return true;
							}
							
							filters.addPlayer(op.getUniqueId());
							i++;
						} else {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_PLAYER, null));
							return true;
						}
					} else if (filter.equals("from")) {
						if (i + 3 < args.length) {
							if (!args[i + 2].equalsIgnoreCase("to")) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_FROM, null));
								return true;
							}
							
							String date1 = args[i + 1];
							String date2 = args[i + 3];
							
							try {
								if (!date1.equalsIgnoreCase("beginning")) {
									filters.setFrom(getDateTime(date1));
								}
								if (!date2.equalsIgnoreCase("now")) {
									filters.setTo(getDateTime(date2));
								}
							} catch (ParseException e) {
								player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_DATE_FORMAT, null));
								return true;
							}
							i += 3;
						} else {
							player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_LOGS_FROM, null));
							return true;
						}
					}
				}
				
				LogsManager.getLogs(player.getUniqueId(), filters, fileDest);
			} else {
				return false;
			}
		}

		return true;
	}
	
	private boolean couldBeAKey(Player player, ItemStack item) {
		if (item == null) {
			player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_EMPTY_HAND, null));
			return false;
		}

		Short[] durabilities = AwesomeKeys.keyItems.get(item.getType());

		if (durabilities == null) {
			player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_WRONG_MATERIAL, null));
			return false;
		}

		if (durabilities.length > 0) {
			short durability = item.getDurability();
			boolean found = false;

			for (short s : durabilities) {
				if (s == durability) {
					found = true;
					break;
				}
			}

			if (!found) {
				player.sendMessage(MessagesMap.getMessage(MessageType.CMD_ERR_WRONG_DURABILITY, null));
				return false;
			}
		}
		
		return true;
	}

	private Block target(Player player) {
		HashSet<Material> set = new HashSet<>(3);
		set.add(Material.AIR);
		set.add(Material.WATER);
		set.add(Material.STATIONARY_WATER);
		
		return player.getTargetBlock(set, player.getGameMode() == GameMode.CREATIVE ? 5 : 3);
	}
	
	private long getDateTime(String date) throws ParseException {
		boolean comma = date.contains(",");
		int min = date.indexOf(":");
		int sec = date.lastIndexOf(":");
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy"
				+ (comma ? ",hh" + (min != -1 ? ":mm" + (sec != min ? ":ss" : "") : "") : ""));
		
		return format.parse(date).getTime();
	}
}
