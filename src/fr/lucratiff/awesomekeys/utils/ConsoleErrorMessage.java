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

public enum ConsoleErrorMessage {
	
	CONFIG_FILE_READ ("Unable to read configuration file, please correct or delete it : "),
	FILE_NOT_FOUND ("Unable to find file : "),
	LANGUAGE_FILE_READ ("Unable to read language file, please correct or delete it : "),
	CORRUPT_FILE_REPLACE ("File is corrupted, replacing it : "),
	CORRUPT_FILE_REMOVE ("File is corrupted, deleting it : "),
	FILE_READ_PERMISSIONS ("A problem occured on reading the file, please check its permissions : ");
	
	private final String message;
	
	ConsoleErrorMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
