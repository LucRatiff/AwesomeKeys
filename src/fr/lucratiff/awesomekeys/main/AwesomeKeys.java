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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import fr.lucratiff.awesomekeys.logs.LogsBufferFlusher;
import fr.lucratiff.awesomekeys.utils.ChestPlacement;
import fr.lucratiff.awesomekeys.utils.ConsoleErrorMessage;
import fr.lucratiff.awesomekeys.utils.MessagesMap;
import net.md_5.bungee.api.ChatColor;

public class AwesomeKeys extends JavaPlugin {
	public static final EnumSet<Material> lockablesEnabled = EnumSet.noneOf(Material.class);
	public static final String keyPrefix = ChatColor.AQUA + "Key: ";
	public static final String lockPrefix = ChatColor.RED + "" + ChatColor.DARK_BLUE + "#";
	public static final String masterKeyName = ChatColor.DARK_PURPLE + "Master key";
	public static final String[] keyLore =
		{ ChatColor.GREEN + "Key uuid: " + ChatColor.ITALIC, ChatColor.AQUA + "Description: " + ChatColor.YELLOW, ChatColor.YELLOW + "" };
	public static HashMap<Material, Short[]> keyItems;
	public static AwesomeKeys plugin;
	public static String dataFolder;
	public static String commandKeyName;
	public static boolean logsEnabled;
	public static long bufferDuration;
	public static boolean receivingKeyExactItemsIsDenied;
	public static boolean receivingKeyItemTypesIsDenied;
	public static boolean renamingKeyItemIsDenied;
	public static int playerContainersLimitation;
	public static int playerDoorsLimitation;
	public static int playerLocksLimitation;
	public static boolean particlesEnabled;
	public static boolean bypassProtection;

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public void onEnable() {
		plugin = this;
		getDataFolder().mkdir();
		dataFolder = getDataFolder() + File.separator;

		try {
			FileSystem fs = FileSystems.getDefault();

			if (!Files.exists(fs.getPath(dataFolder + "locks"))) {
				new File(dataFolder + "locks").mkdir();
			}
			if (!Files.exists(fs.getPath(dataFolder + "players"))) {
				new File(dataFolder + "players").mkdir();
			}
			if (!Files.exists(fs.getPath(dataFolder + "logs"))) {
				new File(dataFolder + "logs").mkdir();
			}

			File file = new File(dataFolder + "config.json");
			String language = "en";
			
			if (!file.exists()) {
				getLogger().info("Extracting default config file config.json");
				file.createNewFile();
				defaultFileExtract(file.getName(), false);
			}
			
			getLogger().info("Loading config file config.json");

			try {
				Gson gson = new Gson();

				Map<String, Object> config = gson.fromJson(new FileReader(file), Map.class);

				ArrayList<Map<String, Object>> items = (ArrayList<Map<String, Object>>) config.get("keys");
				int i = 0, size = items.size();
				keyItems = new HashMap<>(size);
				do {
					ArrayList<Double> durabilityList = (ArrayList<Double>) items.get(i).get("durability");
					Short[] durabilities = new Short[durabilityList.size()];

					for (int j = 0; j < durabilityList.size(); j++) {
						durabilities[j] = (short) (double) durabilityList.get(j);
					}

					Material type = new ItemStack((int) (double) items.get(i).get("item")).getType();
					keyItems.put(type, durabilities);
					i++;
				} while (i < size);

				language = (String) config.get("language");

				if ((boolean) config.get("alias-k-enabled")) {
					commandKeyName = "/k";
				} else {
					commandKeyName = "/key";
				}

				logsEnabled = (boolean) config.get("enable-logs");
				bufferDuration = (int) (double) config.get("buffer-duration");
				
				boolean craftingKeyItemTypesIsDenied = (boolean) config.get("deny-crafting-key-item-types");
				boolean craftingKeyExactItemsIsDenied = (boolean) config.get("deny-crafting-key-exact-items");
				int k = 0, keyItemsSize = keyItems.size();
				if (craftingKeyItemTypesIsDenied || craftingKeyExactItemsIsDenied) {
					Iterator<Recipe> recipes = getServer().recipeIterator();
					while (recipes.hasNext() && k < keyItemsSize) {
						ItemStack item = recipes.next().getResult();
						Short[] durabilities = keyItems.get(item.getType());
						if (durabilities != null) {
							if (craftingKeyItemTypesIsDenied) {
								recipes.remove();
							} else {
								short durability = item.getDurability();
								for (short s : durabilities) {
									if (s == durability) {
										recipes.remove();
										break;
									}
								}
							}
							k++;
						}
					}
				}
				receivingKeyExactItemsIsDenied = (boolean) config.get("deny-receiving-key-exact-items");
				receivingKeyItemTypesIsDenied = (boolean) config.get("deny-receiving-key-item-types");
				renamingKeyItemIsDenied = (boolean) config.get("deny-renaming-key-items");
				playerContainersLimitation = (int) (double) config.get("per-player-containers-limitation");
				playerLocksLimitation = (int) (double) config.get("per-player-locks-limitation");
				if ((boolean) config.get("enable-wooden-doors")) {
					lockablesEnabled.add(Material.ACACIA_DOOR);
					lockablesEnabled.add(Material.BIRCH_DOOR);
					lockablesEnabled.add(Material.DARK_OAK_DOOR);
					lockablesEnabled.add(Material.JUNGLE_DOOR);
					lockablesEnabled.add(Material.SPRUCE_DOOR);
					lockablesEnabled.add(Material.WOODEN_DOOR);
				}
				if ((boolean) config.get("enable-iron-doors")) {
					lockablesEnabled.add(Material.IRON_DOOR_BLOCK);
				}
				if ((boolean) config.get("enable-fence-gates")) {
					lockablesEnabled.add(Material.ACACIA_FENCE_GATE);
					lockablesEnabled.add(Material.BIRCH_FENCE_GATE);
					lockablesEnabled.add(Material.DARK_OAK_FENCE_GATE);
					lockablesEnabled.add(Material.FENCE_GATE);
					lockablesEnabled.add(Material.JUNGLE_FENCE_GATE);
					lockablesEnabled.add(Material.SPRUCE_FENCE_GATE);
				}
				if ((boolean) config.get("enable-chests")) {
					lockablesEnabled.add(Material.CHEST);
				}
				if ((boolean) config.get("enable-trapped-chests")) {
					lockablesEnabled.add(Material.TRAPPED_CHEST);
				}
				if ((boolean) config.get("enable-shulkers")) {
					ChestPlacement.loadShulkerMaterials();
				}
				if ((boolean) config.get("enable-other-containers")) {
					lockablesEnabled.add(Material.DISPENSER);
					lockablesEnabled.add(Material.DROPPER);
					lockablesEnabled.add(Material.BLACK_SHULKER_BOX);
					lockablesEnabled.add(Material.FURNACE);
					lockablesEnabled.add(Material.BURNING_FURNACE);
				}
				particlesEnabled = (boolean) config.get("enable-particles");
				bypassProtection = (boolean) config.get("bypass-protection");
			} catch (JsonSyntaxException | JsonIOException | NullPointerException | ArrayIndexOutOfBoundsException
					| ClassCastException e) {
				getLogger().severe(ConsoleErrorMessage.CONFIG_FILE_READ + dataFolder + "config.json");
				setEnabled(false);
				return;
			}

			file = new File(dataFolder + "lang_" + language + ".json");

			if (!file.exists()) {
				getLogger().warning(ConsoleErrorMessage.FILE_NOT_FOUND + file.getName());

				file = new File(dataFolder + "lang_en.json");

				if (!file.exists()) {
					getLogger().info("Extracting default language file lang_en.json");
					file.createNewFile();
					defaultFileExtract(file.getName(), false);
				}
			}

			getLogger().info("Loading language file " + file.getName());

			if (!MessagesMap.initialize(file)) {
				setEnabled(false);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			setEnabled(false);
		}
		
		this.getCommand("key").setExecutor(new CommandKey());
		this.getCommand("akverifylang").setExecutor(new CommandAkVerifyLang());
		this.getServer().getPluginManager().registerEvents(new Listeners(), plugin);
	}
	
	@Override
	public void onDisable() {
		new LogsBufferFlusher().flush();
	}

	public static void defaultFileExtract(String name, boolean temp) throws IOException {
		InputStream input = AwesomeKeys.plugin.getClass()
				.getResourceAsStream(File.separator + "resources" + File.separator + name);
		FileOutputStream output = new FileOutputStream(
				new File(dataFolder + File.separator + (temp ? "temp." : "") + name));
		byte[] b = new byte[1024];
		int i = 0;
		while ((i = input.read(b)) != -1) {
			output.write(b, 0, i);
		}
		input.close();
		output.close();
	}
	
	public static boolean isAKey(ItemStack item) {
		if (item != null) {
			ItemMeta im = item.getItemMeta();
			if (im == null) {
				return false;
			}
			String name = im.getDisplayName();
			if (name != null && name.length() >= AwesomeKeys.keyPrefix.length()
					&& name.substring(0, AwesomeKeys.keyPrefix.length()).equals(AwesomeKeys.keyPrefix)) {
				Short[] durabilities = AwesomeKeys.keyItems.get(item.getType());
				if (durabilities != null) {
					if (durabilities.length > 0) {
						short durability = item.getDurability();
						for (short s : durabilities) {
							if (s == durability) {
								return true;
							}
						}
					} else {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static void deleteFolder(String folder) throws IOException {
		Stream<Path> files = Files.walk(FileSystems.getDefault().getPath(folder));
		files.sorted().map(Path::toFile).forEach(File::deleteOnExit);
		files.close();
	}
}
