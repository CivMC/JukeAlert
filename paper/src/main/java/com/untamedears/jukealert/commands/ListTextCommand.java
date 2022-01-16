package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;

public class ListTextCommand extends BaseCommand {

	private final String COMMAND_ALIAS = "jalisttext";

	@CommandAlias(COMMAND_ALIAS)
	@Syntax("[page_number]")
	@Description("Lists all snitches and their time until dormancy or culling")
	public void execute(Player player, @Optional String pageNumber) {
		var groupIds = new IntArrayList();
		List<String> groups = NameAPI.getGroupManager().getAllGroupNames(player.getUniqueId());
		for (String group : groups) {
			groupIds.add(GroupManager.getGroup(group).getGroupId());
		}
		int offset = 1;
		if (pageNumber != null) {
			try {
				offset = Integer.parseInt(pageNumber);
				if (offset == 0) {
					offset = 1;
				}
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + pageNumber + " is not a number");
				return;
			}
		}
		int pageLength = JukeAlert.getInstance().getSettingsManager().getJaListLength(player.getUniqueId());
		//This is needed for the lambda below on buildSnitchList
		int finalOffset = offset;
		JukeAlert.getInstance().getTaskChainFactory().newChain()
				.async((unused) -> JukeAlert.getInstance().getDAO().loadSnitchesByGroupID(groupIds).parallel()
						.map((snitch) -> {
							final DormantCullingAppender appender = snitch.getAppender(DormantCullingAppender.class);
							if (appender == null) {
								return null;
							}
							return new SnitchCache(snitch, appender.getTimeUntilCulling());
						})
						.filter(Objects::nonNull)
						.sorted(Comparator.comparingLong((entry) -> entry.timeUntilCulling))
						.map((entry) -> entry.snitch)
						.collect(Collectors.toList()))
				.syncLast((snitches) -> buildList(player, snitches, finalOffset, pageLength))
				.execute();
	}

	private void buildList(Player player, List<Snitch> snitchList, int offset, int pageLength) {
		int initialOffset = (offset - 1) * pageLength;
		if (initialOffset >= snitchList.size()) {
			player.sendMessage(Component.text("There are only " + (snitchList.size() / pageLength) + 1 + " pages for you to look at").color(NamedTextColor.RED));
			return;
		}
		int currentPageSize = Math.min(pageLength, snitchList.size() - initialOffset);
		ListIterator<Snitch> iter = snitchList.listIterator(initialOffset);
		int currentSlot = 0;
		player.sendMessage(Component.text("--- Page " + offset + " of Snitch List ---").color(NamedTextColor.AQUA));
		while (currentSlot++ < currentPageSize) {
			player.sendMessage(formatSnitch(iter.next()));
		}
		player.sendMessage(getFooter(offset));
	}

	private Component formatSnitch(Snitch snitch) {
		DormantCullingAppender appender = snitch.getAppender(DormantCullingAppender.class);
		return Component.text(snitch.getName().isEmpty() ?
						String.format("%-17s", snitch.getType().getName().substring(0, Math.min(snitch.getType().getName().length(), 16)))  :
						String.format("%-17s", snitch.getName().substring(0, Math.min(snitch.getName().length(), 16))))
				.append(Component.text("["
						+ snitch.getLocation().getWorld().getName() + ","
						+ String.format("%5s", snitch.getLocation().getBlockX()) + ","
						+ String.format("%3s", snitch.getLocation().getBlockY()) + ","
						+ String.format("%5s", snitch.getLocation().getBlockZ()) + "] "))
				.append(Component.text(TextUtil.formatDuration(appender.getTimeUntilCulling())));
	}

	private Component getFooter(int currentPage) {
		return Component.text("------")
				.color(NamedTextColor.AQUA)
				.append(Component.text("❰❰")
								.hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage - 1))))
								.clickEvent(ClickEvent.runCommand("/" + COMMAND_ALIAS + " " + (currentPage - 1))))
				.append(Component.text("---"))
				.append(Component.text("❯❯")
						.hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage + 1))))
						.clickEvent(ClickEvent.runCommand("/" + COMMAND_ALIAS + " " + (currentPage + 1))))
				.append(Component.text("------").color(NamedTextColor.AQUA));
	}

	private static class SnitchCache {
		private final Snitch snitch;
		private final long timeUntilCulling;
		public SnitchCache(@Nonnull final Snitch snitch,
						   final long timeUntilCulling) {
			this.snitch = snitch;
			this.timeUntilCulling = timeUntilCulling;
		}
	}
}
