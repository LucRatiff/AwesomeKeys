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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.lucratiff.awesomekeys.utils.ChestPlacement;
import fr.lucratiff.awesomekeys.utils.MessageType;
import fr.lucratiff.awesomekeys.utils.MessagesMap;

public class Listeners implements Listener {
	
	public static HashMap<UUID, Object[]> addOrRemoveLockFromLockablePreparation = null;
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void accessLockable(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.isCancelled() && !AwesomeKeys.bypassProtection) {
				return;
			}
			if (event.getBlockFace() == BlockFace.EAST_NORTH_EAST && event.getItem() == null
					&& addOrRemoveLockFromLockablePreparation != null) {
				Object[] argsList = addOrRemoveLockFromLockablePreparation.remove(event.getPlayer().getUniqueId());
				if (argsList != null) {
					event.setCancelled(true);
					LocksManager.addOrRemoveLockFromLockable((UUID) argsList[0], (String) argsList[1], event.getPlayer(),
							event.getClickedBlock(), (Location) argsList[2], (boolean) argsList[3]);
					if (addOrRemoveLockFromLockablePreparation.size() == 0) {
						addOrRemoveLockFromLockablePreparation = null;
					}
				}
			} else if (AwesomeKeys.lockablesEnabled.contains(event.getClickedBlock().getType())) {
				LocksManager.access(event.getPlayer(), event.getClickedBlock(), event.getItem(), event);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void destroyLockableByPlayer(BlockBreakEvent event) {
		if (!event.isCancelled() && AwesomeKeys.lockablesEnabled.contains(event.getBlock().getType())) {
			LocksManager.destroy(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void destroyLockableByEntity(EntityChangeBlockEvent event) {
		if (!event.isCancelled() && event.getTo() == Material.AIR
				&& AwesomeKeys.lockablesEnabled.contains(event.getBlock().getType())) {
			LocksManager.destroy(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void placeChestAndShulker(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			if (ChestPlacement.isAShulker(event.getBlock().getType())) {
				LocksManager.placeShulker(event.getPlayer().getUniqueId(), event.getBlock().getLocation(),
						(Nameable)event.getBlock().getState());
			} else if ((event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.TRAPPED_CHEST)
					&& ChestPlacement.couldBeADoubleChest(event.getBlock())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(MessagesMap.getMessage(MessageType.ACTION_ERR_DOUBLECHEST_FROM_LOCKED_CHEST, null));
			}
		}
	}
	
	@EventHandler
	public void entityDrops(EntityDeathEvent event) {
		if (AwesomeKeys.receivingKeyItemTypesIsDenied || AwesomeKeys.receivingKeyExactItemsIsDenied) {
			List<ItemStack> drops = event.getDrops();
			if (drops != null) {
				for (int i = 0; i < drops.size(); i++) {
					ItemStack item = drops.get(i);
					Short[] durabilities = AwesomeKeys.keyItems.get(item.getType());
					if (durabilities != null) {
						if (AwesomeKeys.receivingKeyExactItemsIsDenied && durabilities.length > 0) {
							short durability = item.getDurability();
							for (short s : durabilities) {
								if (s == durability) {
									drops.remove(i--);
									break;
								}
							}
						} else {
							drops.remove(i--);
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void receivingKeyItemFromLivingEntity(InventoryClickEvent event) {
		if (!event.isCancelled() && (AwesomeKeys.receivingKeyItemTypesIsDenied || AwesomeKeys.receivingKeyExactItemsIsDenied)
				&& !(event.getClickedInventory().getHolder() instanceof Player)) {
			ItemStack item = event.getCursor();
			if (item != null) {
				Short[] durabilities = AwesomeKeys.keyItems.get(item.getType());
				if (durabilities != null) {
					if (AwesomeKeys.receivingKeyExactItemsIsDenied && durabilities.length > 0) {
						short durability = item.getDurability();
						for (short s : durabilities) {
							if (s == durability) {
								event.setCancelled(true);
								event.getWhoClicked().sendMessage(MessagesMap.getMessage(
										MessageType.ACTION_ERR_PICK_KEY_EXACT_ITEM, null));
								break;
							}
						}
					} else {
						event.setCancelled(true);
						event.getWhoClicked().sendMessage(MessagesMap.getMessage(
								MessageType.ACTION_ERR_PICK_KEY_ITEM_TYPE, null));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void renamingKey(PrepareAnvilEvent event) {
		if (AwesomeKeys.renamingKeyItemIsDenied) {
			ItemStack item = event.getResult();
			if (item != null) {
				Short[] durabilities = AwesomeKeys.keyItems.get(item.getType());
				if (durabilities != null) {
					if (durabilities.length > 0) {
						short durability = item.getDurability();
						for (short s : durabilities) {
							if (s == durability) {
								event.setResult(null);
							}
						}
					} else {
						event.setResult(null);
					}
				}
			}
		}
	}
}
