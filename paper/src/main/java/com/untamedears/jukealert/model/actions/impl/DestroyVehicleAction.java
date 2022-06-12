package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class DestroyVehicleAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "DESTROY_VEHICLE";

	public DestroyVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = new ItemStack(getVehicle());
		super.enrichGUIItem(is);
		return new DecorationStack(is);
	}
	
	/**
	 * @return Material of the vehicle destroyed
	 */
	public Material getVehicle() {
		return JAUtility.getVehicle(victim);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	protected Component getChatRepresentationIdentifier() {
		return Component.text()
				.content("Broke ")
				.append(Component.translatable(getVehicle().translationKey()))
				.build();
	}

}
