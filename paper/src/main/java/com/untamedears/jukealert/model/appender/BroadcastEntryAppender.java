package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.config.LimitedActionTriggerConfig;
import com.untamedears.jukealert.util.JASettingsManager;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BroadcastEntryAppender extends ConfigurableSnitchAppender<LimitedActionTriggerConfig> {

	public static final String ID = "broadcast";

	public BroadcastEntryAppender(Snitch snitch, ConfigurationSection config) {
		super(snitch, config);
	}

	@Override
	public void acceptAction(SnitchAction action) {
		if (action.isLifeCycleEvent() || !action.hasPlayer()) {
			return;
		}
		LoggablePlayerAction log = (LoggablePlayerAction) action;
		if (snitch.hasPermission(log.getPlayer(), JukeAlertPermissionHandler.getSnitchImmune())) {
			return;
		}
		if (!config.isTrigger(action.getIdentifier())) {
			return;
		}
		for (UUID uuid : snitch.getGroup().getAllMembers()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			JASettingsManager settings = JukeAlert.getInstance().getSettingsManager();
			if (settings.doesIgnoreAllAlerts(uuid)) {
				continue;
			}
			if (settings.doesIgnoreAlert(snitch.getGroup().getName(), uuid)) {
				continue;
			}
			if (snitch.hasPermission(uuid, JukeAlertPermissionHandler.getSnitchAlerts())) {

				final TextComponent.Builder comp = Component.text().append(
						log.getChatRepresentation(player.getLocation(), true)
				);

				if (settings.shouldShowDirections(uuid)) {
					comp.append(
							Component.text("  "),
							JAUtility.genDirections(this.snitch, player)
					);
				}
				if (settings.monocolorAlerts(uuid)) {
					player.sendMessage(Component.text(
							PlainTextComponentSerializer.plainText().serialize(comp.build()),
							NamedTextColor.AQUA
					));
				}
				else {
					player.sendMessage(comp);
				}
			}
		}
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

	@Override
	public Class<LimitedActionTriggerConfig> getConfigClass() {
		return LimitedActionTriggerConfig.class;
	}

}
