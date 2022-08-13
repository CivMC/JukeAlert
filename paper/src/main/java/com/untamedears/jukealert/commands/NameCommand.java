package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NameCommand extends BaseCommand {
	private record CommandInfo(Integer x, Integer y, Integer z, String snitchName){}

	@CommandAlias("janame")
	@Syntax("[<x> <y> <z>] <name> - enclose name to the double quotes if it contains space symbol(s)")
	@Description("Name a snitch")
	public void execute(Player player, String[] args) throws InvalidCommandArgument {
		if (args.length == 0) {
			throw new InvalidCommandArgument();
		}

		CommandInfo commandInfo = parseArgs(args);
		if (commandInfo == null) {
			throw new InvalidCommandArgument("Invalid arguments");
		}

		renameSnitch(player, commandInfo);
	}

	private static CommandInfo parseArgs(String[] args) {
		if (args.length > 1 && !args[0].startsWith("\"")) {
			return parseExtendedSyntax(args);
		}

		String snitchName = getSnitchName(args, 0);
		return snitchName != null
				? new CommandInfo(null, null, null, snitchName)
				: null;
	}

	private static CommandInfo parseExtendedSyntax(String[] args) {
		Integer x = null;
		Integer y = null;
		Integer z = null;
		String snitchName = null;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (z == null && isInteger(arg)) {
				int value = Integer.parseInt(arg);
				switch (i) {
					case 0 -> x = value;
					case 1 -> y = value;
					case 2 -> z = value;
				}
			} else {
				snitchName = getSnitchName(args, i);
				break;
			}
		}

		return snitchName != null && (x == null || z != null)
				? new CommandInfo(x, y, z, snitchName)
				: null;
	}

	private static String getSnitchName(String[] args, int index) {
		if (args[index].startsWith("\"") && !args[args.length - 1].endsWith("\"")
				|| args.length - 1 > index && !args[index].startsWith("\"")
		) {
			return null;
		}

		String line = index == 0
				? String.join(" ", args)
				: Stream.of(args).skip(index).collect(Collectors.joining(" "));

		return line.startsWith("\"")
				? line.substring(1, line.length() - 1)
				: line;
	}

	private static boolean isInteger(String arg) {
		try {
			Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static void renameSnitch(Player player, CommandInfo commandInfo) {
		Snitch snitch;

		if (commandInfo.x == null) {
			snitch = findLookingAtOrClosestSnitch(player, getPermission());
		} else {
			World world = player.getLocation().getWorld();
			Location location = new Location(world, commandInfo.x, commandInfo.y, commandInfo.z);

			snitch = JukeAlert.getInstance().getSnitchManager().getSnitchAt(location);
			if (snitch != null && !snitch.hasPermission(player, getPermission())) {
				snitch = null;
			}
		}

		if (snitch == null) {
			player.sendMessage(
					ChatColor.RED + "You do not own a snitch at those coordinates or lack permission to rename it!");
			return;
		}

		renameSnitch(player, commandInfo.snitchName, snitch);
	}

	private static void renameSnitch(Player player, String name, Snitch snitch) {
		String newName = name.length() > 40
			? name.substring(0, 40)
			: name;

		String prevName = snitch.getName();
		JukeAlert.getInstance().getSnitchManager().renameSnitch(snitch, newName);
		TextComponent lineText = new TextComponent(ChatColor.AQUA + " Changed snitch name to ");
		lineText.addExtra(JAUtility.genTextComponent(snitch));
		lineText.addExtra(ChatColor.AQUA + " from " + ChatColor.GOLD + prevName);
		player.spigot().sendMessage(lineText);
	}

	private static PermissionType getPermission() {
		return JukeAlertPermissionHandler.getRenameSnitch();
	}
}
