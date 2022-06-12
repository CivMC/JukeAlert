package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

public class ListCommand extends BaseCommand {

	@CommandAlias("jalist")
	@Syntax("[group]")
	@Description("Lists all snitches you have access to if given no arguments or the ones on the given groups")
	public void execute(final Player player, @Optional final String targetGroup1, @Optional final String targetGroup2, @Optional final String targetGroup3) {
		boolean playerProvidedGroups = true;
		List<String> groupNames = null;
		if (!(targetGroup1 == null) && !(targetGroup2 == null) && !(targetGroup3 == null)) {
			groupNames.add(targetGroup1);
			groupNames.add(targetGroup2);
			groupNames.add(targetGroup3);
		}
		if (CollectionUtils.isEmpty(groupNames)) {
			groupNames = NameAPI.getGroupManager().getAllGroupNames(player.getUniqueId());
			playerProvidedGroups = false;
		}
		final var groupIds = new IntArrayList();
		for (final String groupName : groupNames) {
			final Group group = GroupManager.getGroup(groupName);
			if (group == null) {
				if (playerProvidedGroups) {
					player.sendMessage(ChatColor.RED + "The group " + groupName + " does not exist");
				}
				continue;
			}
			if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
					JukeAlertPermissionHandler.getListSnitches())) {
				if (playerProvidedGroups) {
					player.sendMessage(ChatColor.RED + "You do not have permission to list snitches "
							+ "for the group " + group.getName());
				}
				continue;
			}
			groupIds.addAll(group.getGroupIds());
		}
		if (groupIds.isEmpty()) {
			player.sendMessage(ChatColor.GREEN + "You do not have access to any group's snitches.");
			return;
		}
		player.sendMessage(ChatColor.GREEN + "Retrieving snitches for a total of " + groupNames.size()
				+ " group instances. This may take a moment.");
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
				.syncLast((snitches) -> new SnitchOverviewGUI(player, snitches, "Your snitches",
						player.hasPermission("jukealert.admin")).showScreen())
				.execute();
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

	public List<String> tabComplete(final CommandSender sender, final String[] arguments) {
		final String last = ArrayUtils.isEmpty(arguments) ? "" : arguments[arguments.length - 1];
		return GroupTabCompleter.complete(last, JukeAlertPermissionHandler.getListSnitches(), (Player) sender);
	}

}
