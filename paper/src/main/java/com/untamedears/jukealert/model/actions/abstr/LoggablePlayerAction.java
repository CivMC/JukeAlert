package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.util.JAUtility;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.NameAPI;

public abstract class LoggablePlayerAction extends PlayerAction implements LoggableAction {
	
	private ActionCacheState state;
	private int id;

	public LoggablePlayerAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
		state = ActionCacheState.NEW;
	}
	
	
	@Override
	public LoggedActionPersistence getPersistence() {
		return new LoggedActionPersistence(player, null, time, null);
	}
	
	@Override
	public void setID(int id) {
		this.id = id;
		state = ActionCacheState.NORMAL;
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public void setCacheState(ActionCacheState state) {
		this.state = state;
	}
	
	@Override
	public ActionCacheState getCacheState() {
		return state;
	}

	@Override
	public boolean isLifeCycleEvent() {
		return false;
	}
	
	@Override
	public Component getChatRepresentation(Location reference, boolean live) {
		final Location referenceLoc = getLocationForStringRepresentation();
		final boolean sameWorld = JAUtility.isSameWorld(referenceLoc, reference);
		final TextComponent.Builder component = Component.text().append(
				Component.text().color(NamedTextColor.GOLD).append(getChatRepresentationIdentifier()),
				Component.text("  "),
				Component.text(NameAPI.getCurrentName(getPlayer()), NamedTextColor.GREEN),
				Component.text("  ")
		);
		if (live) {
			component.append(
					JAUtility.genTextComponent(this.snitch),
					Component.text()
							.color(NamedTextColor.YELLOW)
							.append(JAUtility.formatLocation(referenceLoc, !sameWorld))
			);
		}
		else {
			// don't need to explicitly list location when retrieving logs and its the snitch location
			if (referenceLoc != this.snitch.getLocation()) {
				component.append(Component.text()
						.color(NamedTextColor.YELLOW)
						.append(JAUtility.formatLocation(referenceLoc, !sameWorld)));
			}
			component.append(Component.text(getFormattedTime(), NamedTextColor.AQUA));
		}
		return component.build();
	}
	
	protected void enrichGUIItem(ItemStack item) {
		if (item.getType().isAir()) {
			JukeAlert.getInstance().getLogger().info("Tried to enrich air");
			item = new ItemStack(Material.STONE);
		}
		ItemUtils.handleItemMeta(item, (final ItemMeta meta) -> {
			meta.displayName(Component.text().color(NamedTextColor.GOLD).append(getGUIName()).build());
			meta.lore(List.of(
					Component.text("Player: " + getPlayerName(), NamedTextColor.GOLD),
					Component.text("Time: " + getFormattedTime(), NamedTextColor.LIGHT_PURPLE)
			));
			return true;
		});
	}
	
	protected Component getGUIName() {
		return getChatRepresentationIdentifier();
	}
	
	protected Location getLocationForStringRepresentation() {
		return snitch.getLocation();
	}
	
	protected abstract Component getChatRepresentationIdentifier();

	protected IClickable getEnrichedClickableSkullFor(UUID uuid) {
		CompletableFuture<ItemStack> itemReadyFuture = new CompletableFuture<>();
		ItemStack is = CivModCorePlugin.getInstance().getSkinCache().getHeadItem(getPlayer(),
				() -> new ItemStack(Material.PLAYER_HEAD),
				itemReadyFuture::complete);
		enrichGUIItem(is);
		return new DecorationStack(is) {
			@Override
			public void addedToInventory(ClickableInventory inv, int slot) {
				itemReadyFuture.thenAccept(newItem -> {
					LoggablePlayerAction.this.enrichGUIItem(newItem);
					this.item = newItem;
					inv.setItem(newItem, slot);
				});
				super.addedToInventory(inv, slot);
			}
		};
	}
}
