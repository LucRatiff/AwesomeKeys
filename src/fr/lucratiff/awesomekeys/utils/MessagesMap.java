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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import fr.lucratiff.awesomekeys.main.AwesomeKeys;

public class MessagesMap {

	private static final String version = "1";
	private static String[] messages;

	@SuppressWarnings("unchecked")
	public static boolean initialize(File file) {
		boolean tempCreated = false;
		try {
			Gson gson = new Gson();

			Map<String, String> map = (Map<String, String>) gson.fromJson(new FileReader(file), Map.class);
			Map<String, String> lang_enMap = null;

			if (!map.get("version").equals(version)) {
				Bukkit.getLogger().warning("Language file is outdated ! Some messages will be displayed as default");
				Bukkit.getLogger().warning("Please download or submit a new one here : https://github.com/LucRatiff/AwesomeKeys");

				File lang_en = new File(AwesomeKeys.dataFolder + "temp.lang_en.json");
				lang_en.createNewFile();
				tempCreated = true;
				AwesomeKeys.defaultFileExtract("lang_en.json", true);
				lang_enMap = (Map<String, String>) gson.fromJson(new FileReader(lang_en), Map.class);
			}
			
			map.remove("__comment");
			map.remove("version");
			messages = new String[MessageType.values().length];
			int i = 0;

			for (MessageType mt : MessageType.values()) {
				String message = map.get(mt.name());

				if (message == null) {
					message = lang_enMap.get(mt.name());
				}

				messages[i++] = message;
			}
		} catch (IOException | JsonSyntaxException | JsonIOException | NullPointerException e) {
			Bukkit.getLogger().severe(ConsoleErrorMessage.LANGUAGE_FILE_READ
					+ AwesomeKeys.dataFolder + file.getName());
			return false;
		} finally {
			if (tempCreated) {
				new File(AwesomeKeys.dataFolder + "temp.lang_en.json").delete();
			}
		}

		HashMap<Integer, String[]> argsToMap = new HashMap<>();
		argsToMap.put(MessageType.CMD_ERR_NEW.ordinal(),
				new String[] { AwesomeKeys.commandKeyName, "new lock|key <", ">" });
		argsToMap.put(MessageType.CMD_ERR_LOCKS_LIMITATION.ordinal(),
				new String[] { AwesomeKeys.playerLocksLimitation + "" });
		argsToMap.put(MessageType.CMD_SUCCESS_LOCK_CREATED.ordinal(),
				new String[] { AwesomeKeys.commandKeyName + " new key " });
		argsToMap.put(MessageType.CMD_ERR_EDITKEY.ordinal(),
				new String[] { AwesomeKeys.commandKeyName + " editkey <", "> <", ">" });
		argsToMap.put(MessageType.CMD_ERR_LOGS.ordinal(),
				new String[] { AwesomeKeys.commandKeyName + " <", "> (> <", ">" });
		argsToMap.put(MessageType.CMD_ERR_LOGS_BLOCK.ordinal(),
				new String[] { "<", "> <x> <y> <z>" });
		argsToMap.put(MessageType.CMD_ERR_LOGS_PLAYER.ordinal(),
				new String[] { "player <", "|uuid>" });
		argsToMap.put(MessageType.CMD_ERR_LOGS_FROM.ordinal(),
				new String[] { "from <dd/mm/yy(,hh(:mm(:ss)))>|beginning to <date...>|now" });
		argsToMap.put(MessageType.CMD_ERR_LOGS_DATE_FORMAT.ordinal(),
				new String[] { "<dd/mm/yy(,hh(:mm(:ss)))>", "\"beginning\"", "\"now\"" });
		argsToMap.put(MessageType.CMD_ERR_PURGE.ordinal(),
				new String[] { AwesomeKeys.commandKeyName + " unlinked|player (<", ">)" });
		argsToMap.put(MessageType.CMD_ERR_GREETING.ordinal(),
				new String[] { AwesomeKeys.commandKeyName + " none|<", "> <", ">" });
		argsToMap.put(MessageType.CMD_ERR_GREETING_COLOR_CODE.ordinal(),
				new String[] { "0123456789abcedf" });

		for (Map.Entry<Integer, String[]> m : argsToMap.entrySet()) {
			StringBuilder sb = new StringBuilder(messages[m.getKey()]);

			for (String s : m.getValue()) {
				int index = sb.indexOf("%");
				sb = sb.replace(index, index + 1, s);
			}

			messages[m.getKey()] = sb.toString();
		}

		return true;
	}

	public static String getMessage(MessageType type, String[] args) {
		String message = messages[type.ordinal()];

		if (args != null) {
			StringBuilder sb = new StringBuilder(message);

			for (String s : args) {
				int index = sb.indexOf("#");
				sb = sb.replace(index, index + 1, s);
			}

			message = sb.toString();
		}

		return message;
	}
}
