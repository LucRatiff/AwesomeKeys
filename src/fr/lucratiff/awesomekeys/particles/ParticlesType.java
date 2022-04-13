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

package fr.lucratiff.awesomekeys.particles;

public enum ParticlesType {
	
	ACCESS_CONTAINER_NO_KEY,
	ACCESS_CONTAINER_WRONG_KEY,
	ACCESS_CONTAINER_ALLOWED,
	ACCESS_CONTAINER_MASTER_KEY,
	ACCESS_DOOR_NO_KEY,
	ACCESS_DOOR_WRONG_KEY,
	ACCESS_DOOR_ALLOWED,
	ACCESS_DOOR_MASTER_KEY,
	
	CREATION_KEY,
	CREATION_MASTER_KEY,
	CREATION_LOCKED_CONTAINER,
	CREATION_LOCKED_DOOR,
	DESTRUCTION_LOCKED_BLOCK,
	PLACE_SHULKER;
}
