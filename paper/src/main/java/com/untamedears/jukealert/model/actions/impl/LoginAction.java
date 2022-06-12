package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.events.PlayerLoginSnitchEvent;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class LoginAction extends LoggablePlayerAction {

	public static final String ID = "LOGIN";

	public LoginAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		return getEnrichedClickableSkullFor(getPlayer());
	}

	@Override
	public void accept(Snitch s) {
		Bukkit.getPluginManager().callEvent(new PlayerLoginSnitchEvent(snitch, Bukkit.getPlayer(player)));
	}

	@Override
	protected Component getChatRepresentationIdentifier() {
		return Component.text()
				.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE)
				.content("Login")
				.build();
	}
}
