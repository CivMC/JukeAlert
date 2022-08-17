package com.untamedears.jukealert.model;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;
import com.untamedears.jukealert.model.field.FieldManager;
import com.untamedears.jukealert.model.field.SingleCuboidRangeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SnitchFactoryType {
	public static class ItemInfo {
		public Material material;
		public String name;
		public List<String> lore;
	}

	private final int id;
	private final ItemInfo item;
	private final String name;
	private final Function<Snitch, FieldManager> fieldGenerator;

	private final List<Function<Snitch, AbstractSnitchAppender>> appenders;

	public SnitchFactoryType(ItemInfo item, Function<Snitch, FieldManager> fieldGenerator, String name, int id,
			List<Function<Snitch, AbstractSnitchAppender>> appenders) {
		this.item = item;
		this.name = name;
		this.id = id;
		this.fieldGenerator = fieldGenerator;
		this.appenders = appenders;

		if (this.item.name != null && this.item.name.length() == 0)
			this.item.name = null;

		if (this.item.lore != null && this.item.lore.isEmpty())
			this.item.lore = null;
	}

	public Snitch create(int snitchID, Location location, String name, int groupID, boolean isNew) {
		Snitch snitch = new Snitch(snitchID, location, isNew, groupID, fieldGenerator,
				this, name);
		for(Function<Snitch, AbstractSnitchAppender> appenderFunc : appenders) {
			AbstractSnitchAppender appender = appenderFunc.apply(snitch);
			if (appender != null) {
				snitch.addAppender(appender);
			}
		}
		return snitch;
	}

	/**
	 * @return Identifying id of this config which will identify its instances even
	 *         across config changes
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return Human readable name of this config
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Material of item used to create instances of this snitch
	 */
	public Material getItemMaterial() {
		return this.item.material;
	}

	/**
	 * @return Item used to create instances of this snitch
	 */
	public ItemStack getItemRepresentation() {
		ItemStack item = new ItemStack(this.item.material);

		if (this.item.name != null && this.item.name.length() > 0 || this.item.lore != null && this.item.lore.size() > 0) {
			ItemMeta meta = item.getItemMeta();

			if (this.item.name != null && this.item.name.length() > 0)
				meta.displayName(Component.text(this.item.name));

			if (this.item.lore != null && this.item.lore.size() > 0) {
				List<Component> loreList = new ArrayList<>();
				for (String loreLine : this.item.lore)
					loreList.add(Component.text(loreLine));

				meta.lore(loreList);
			}

			item.setItemMeta(meta);
		}

		return item;
	}

	public boolean isSame(ItemStack itemStack) {
		if (itemStack == null || this.item.material != itemStack.getType())
			return false;

		ItemMeta meta = itemStack.getItemMeta();

		String name = meta.getDisplayName();
		if (name != null && name.length() == 0)
			name = null;

		List<String> lore;
		if (meta.hasLore()) {
			lore = meta.getLore();
			if (lore != null && lore.isEmpty())
				lore = null;
		} else {
			lore = null;
		}

		return (this.item.name == null && name == null || this.item.name != null && this.item.name.equals(name))
				&& (this.item.lore == null && lore == null || this.item.lore != null && this.item.lore.equals(lore));
	}
}
