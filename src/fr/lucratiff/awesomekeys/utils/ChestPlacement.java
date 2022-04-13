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

package fr.lucratiff.awesomekeys.utils;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import fr.lucratiff.awesomekeys.main.AwesomeKeys;

public class ChestPlacement {
	private static final EnumSet<BlockFace> fourDirections = EnumSet.noneOf(BlockFace.class);
	private static final EnumSet<Material> shulkers = EnumSet.noneOf(Material.class);

	static {
		fourDirections.add(BlockFace.NORTH);
		fourDirections.add(BlockFace.EAST);
		fourDirections.add(BlockFace.SOUTH);
		fourDirections.add(BlockFace.WEST);
		shulkers.add(Material.BLACK_SHULKER_BOX);
		shulkers.add(Material.BLUE_SHULKER_BOX);
		shulkers.add(Material.BROWN_SHULKER_BOX);
		shulkers.add(Material.CYAN_SHULKER_BOX);
		shulkers.add(Material.GRAY_SHULKER_BOX);
		shulkers.add(Material.GREEN_SHULKER_BOX);
		shulkers.add(Material.LIGHT_BLUE_SHULKER_BOX);
		shulkers.add(Material.LIME_SHULKER_BOX);
		shulkers.add(Material.MAGENTA_SHULKER_BOX);
		shulkers.add(Material.ORANGE_SHULKER_BOX);
		shulkers.add(Material.PINK_SHULKER_BOX);
		shulkers.add(Material.PURPLE_SHULKER_BOX);
		shulkers.add(Material.RED_SHULKER_BOX);
		shulkers.add(Material.SILVER_SHULKER_BOX);
		shulkers.add(Material.WHITE_SHULKER_BOX);
		shulkers.add(Material.YELLOW_SHULKER_BOX);
	}

	public static boolean couldBeADoubleChest(Block block) {
		boolean chest = block.getType() == Material.CHEST;
		for (BlockFace face : fourDirections) {
			Block b = block.getRelative(face);
			if ((chest && b.getType() == Material.CHEST) || (!chest && b.getType() == Material.TRAPPED_CHEST)) {
				if (!(((Chest) b.getState()).getInventory() instanceof DoubleChestInventory)) {
					return true;
				}
			}
		}
		
		return false;
	}

	public static Location getDoubleChestSecondLocation(Block block) {
		Inventory inv = ((Chest) block.getState()).getInventory();

		if (inv instanceof DoubleChestInventory) {
			DoubleChest doubleChest = (DoubleChest) inv.getHolder();
			Location l = block.getLocation();
			Location left = doubleChest.getLeftSide().getInventory().getLocation();

			if (left.getBlockX() != l.getBlockX() || left.getBlockZ() != l.getBlockZ()) {
				return left;
			} else {
				return doubleChest.getRightSide().getInventory().getLocation();
			}
		}

		return null;
	}
	
	public static void loadShulkerMaterials() {
		AwesomeKeys.lockablesEnabled.addAll(shulkers);
	}
	
	public static boolean isAShulker(Material shulker) {
		return shulkers.contains(shulker);
	}
}
