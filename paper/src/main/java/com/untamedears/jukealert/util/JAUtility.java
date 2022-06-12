package com.untamedears.jukealert.util;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.world.WorldUtils;
import vg.civcraft.mc.namelayer.permission.PermissionType;

// Static methods only
public final class JAUtility {

	private JAUtility() {

	}
	
	private static double tanPiDiv = Math.sqrt(2.0) - 1.0;

	public static Snitch findClosestSnitch(Location loc, PermissionType perm, UUID player) {
		Snitch closestSnitch = null;
		double closestDistance = Double.MAX_VALUE;
		Collection<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().getSnitchesCovering(loc);
		for (Snitch snitch : snitches) {
			if (snitch.hasPermission(player, perm)) {
				double distance = snitch.getLocation().distanceSquared(loc);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestSnitch = snitch;
				}
			}
		}
		return closestSnitch;
	}

	public static Snitch findLookingAtOrClosestSnitch(Player player, PermissionType perm) {

		Snitch cursorSnitch = getSnitchUnderCursor(player);
		if (cursorSnitch != null && cursorSnitch.hasPermission(player, perm)) {
			return cursorSnitch;
		}
		return findClosestSnitch(player.getLocation(), perm, player.getUniqueId());
	}

	public static Snitch getSnitchUnderCursor(Player player) {
		SnitchManager snitchMan = JukeAlert.getInstance().getSnitchManager();
		Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
		while (itr.hasNext()) {
			Block block = itr.next();
			Snitch found = snitchMan.getSnitchAt(block.getLocation());
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public static Component genTextComponent(final @NotNull Snitch snitch) {
		final Location location = snitch.getLocation();
		final TextComponent.Builder hoverText = Component.text()
				.append(
						Component.text("[Location: ", NamedTextColor.GOLD),
						Component.text("(%s) [%d %d %d]".formatted(
								location.getWorld().getName(),
								location.getBlockX(),
								location.getBlockY(),
								location.getBlockZ()
						), NamedTextColor.AQUA)
				);
		final String snitchName = snitch.getName();
		if (StringUtils.isNotBlank(snitchName)) {
			hoverText.append(
					Component.newline(),
					Component.text("Name: ", NamedTextColor.GOLD),
					Component.text(snitchName, NamedTextColor.AQUA)
			);
		}
		hoverText.append(
				Component.newline(),
				Component.text("Group: ", NamedTextColor.GOLD),
				Component.text(snitch.getGroup().getName(), NamedTextColor.AQUA)
		);
		return Component.text()
				.color(NamedTextColor.AQUA)
				.content(StringUtils.isNotBlank(snitchName) ? snitchName : snitch.getType().getName())
				.hoverEvent(HoverEvent.showText(hoverText))
				.build();
	}

	public static Component genDirections(Snitch snitch, Player player) {
		final Location snitchLocation = snitch.getLocation();
		final Location playerLocation = player.getLocation();
		if (WorldUtils.doLocationsHaveSameWorld(snitchLocation, playerLocation)) {
			return Component.text()
					.color(NamedTextColor.GREEN)
					.append(
							Component.text("[" + Math.round(playerLocation.distance(snitchLocation)) + "m "),
							Component.text(getCardinal(playerLocation, snitchLocation), NamedTextColor.RED),
							Component.text("]")
					)
					.build();
		}
		return Component.empty();
	}

	public static String getCardinal(Location start, Location end) {
		double dX = start.getBlockX() - end.getBlockX();
		double dZ = start.getBlockZ() - end.getBlockZ();

		if (Math.abs(dX) > Math.abs(dZ)) {
			if (Math.abs(dZ / dX) <= tanPiDiv) {
				return dX > 0 ? "West" : "East";
			} else if (dX > 0) {
				return dZ > 0 ? "North West" : "South West";
			} else {
				return dZ > 0 ? "North East" : "South East";
			}
		} else if (Math.abs(dZ) > 0) {
			if (Math.abs(dX / dZ) <= tanPiDiv) {
				return dZ > 0 ? "North" : "South";
			} else if (dZ > 0) {
				return dX > 0 ? "North West" : "North East";
			} else {
				return dX > 0 ? "South West" : "South East";
			}
		} else {
			return "";
		}
	}

	public static Material parseMaterial(String id) {
		try {
			return Material.valueOf(id);
		} catch (IllegalArgumentException e) {
			return Material.STONE;
		}
	}
	
	public static boolean isSameWorld(Location loc1, Location loc2) {
		return loc1.getWorld().getUID().equals(loc2.getWorld().getUID());
	}

	public static Component formatLocation(Location location, boolean includeWorld) {
		return Component.text(includeWorld ?
				"[%s %d %d %d]".formatted(
						location.getWorld().getName(),
						location.getBlockX(),
						location.getBlockY(),
						location.getBlockZ()) :
				"[%d %d %d]".formatted(
						location.getBlockX(),
						location.getBlockY(),
						location.getBlockZ()));
	}

	public static Material getVehicle(String vehicle) {
		switch (vehicle) {
			case "DONKEY":
				return Material.DONKEY_SPAWN_EGG;
			case "LLAMA":
				return Material.LLAMA_SPAWN_EGG;
			case "PIG":
				return Material.PIG_SPAWN_EGG;
			case "HORSE":
				return Material.HORSE_SPAWN_EGG;
			case "MINECART":
				return Material.MINECART;
			case "MINECART_CHEST":
				return Material.CHEST_MINECART;
			case "MINECART_FURNACE":
				return Material.FURNACE_MINECART;
			case "MINECART_TNT":
				return Material.TNT_MINECART;
			case "MINECART_HOPPER":
				return Material.HOPPER_MINECART;
			case "STRIDER":
				return Material.STRIDER_SPAWN_EGG;
			case "BOAT":
			case "OAK_BOAT":
				return Material.OAK_BOAT;
			case "BIRCH_BOAT":
				return Material.BIRCH_BOAT;
			case "SPRUCE_BOAT":
				return Material.SPRUCE_BOAT;
			case "JUNGLE_BOAT":
				return Material.JUNGLE_BOAT;
			case "ACACIA_BOAT":
				return Material.ACACIA_BOAT;
			case "DARK_OAK_BOAT":
				return Material.DARK_OAK_BOAT;
			default:
				JukeAlert.getInstance().getLogger().info("Failed to parse vehicle into material: " + vehicle);
				return Material.STONE;
		}
	}
}
