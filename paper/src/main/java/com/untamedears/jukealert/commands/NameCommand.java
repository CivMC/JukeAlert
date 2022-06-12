package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NameCommand extends BaseCommand {

	@CommandAlias("janame")
	@Syntax("<name>")
	@Description("Name a snitch")
	public void execute(Player player, String targetName) {
		String name = "";
		if (targetName.length() > 40) {
			name = targetName.substring(0, 40);
		} else {
			name = targetName;
		}
		Snitch snitch = findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getRenameSnitch());
		if (snitch == null) {
			player.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
			return;
		}
		String prevName = snitch.getName();
		snitch.setName(name);
		player.sendMessage(Component.text()
				.color(NamedTextColor.AQUA)
				.content("Changed snitch name to ")
				.append(
						JAUtility.genTextComponent(snitch),
						Component.text(" from "),
						Component.text(prevName, NamedTextColor.GOLD)
				)
		);
	}
}
