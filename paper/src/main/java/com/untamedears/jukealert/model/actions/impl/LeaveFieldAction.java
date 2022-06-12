package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class LeaveFieldAction extends LoggablePlayerAction {

	public static final String ID = "LEAVE";

	public LeaveFieldAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public IClickable getGUIRepresentation() {
		return getEnrichedClickableSkullFor(getPlayer());
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	protected Component getChatRepresentationIdentifier() {
		return Component.text("Leave");
	}

}
