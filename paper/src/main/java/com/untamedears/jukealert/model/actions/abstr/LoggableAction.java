package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public interface LoggableAction {

	IClickable getGUIRepresentation();

	/**
	 * Creates a chat representation of this action to show to players
	 * 
	 * @param reference Current location of the player to show the output to
	 * @param live      Whether the action is happening right now or being retrieved
	 *                  as a record
	 * @return TextComponent representing this instance ready for sending to a
	 *         player
	 */
	Component getChatRepresentation(Location reference, boolean live);

	LoggedActionPersistence getPersistence();

	void setID(int id);

	int getID();

	void setCacheState(ActionCacheState state);

	ActionCacheState getCacheState();

}
