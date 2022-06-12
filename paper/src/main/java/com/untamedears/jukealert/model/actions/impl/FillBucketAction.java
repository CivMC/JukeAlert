package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;

public class FillBucketAction extends LoggableBlockAction {
	
	public static final String ID = "FILL_BUCKET";

	public FillBucketAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	protected Component getChatRepresentationIdentifier() {
		return Component.text("Filled bucket");
	}


	@Override
	public String getIdentifier() {
		return ID;
	}

}
