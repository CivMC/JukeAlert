package com.untamedears.jukealert.model;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;
import com.untamedears.jukealert.model.appender.BroadcastEntryAppender;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import com.untamedears.jukealert.model.appender.ShowOwnerOnDestroyAppender;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.model.field.FieldManager;
import com.untamedears.jukealert.model.field.SingleCuboidRangeManager;
import com.untamedears.jukealert.model.field.VariableSizeCuboidRangeManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SnitchTypeManager {

	private Map<String, Class<? extends AbstractSnitchAppender>> appenderClasses;
	private List<SnitchFactoryType> configFactories;
	private Map<Integer, SnitchFactoryType> configFactoriesById;

	public SnitchTypeManager() {
		appenderClasses = new HashMap<>();
		configFactories = new ArrayList<>();
		configFactoriesById = new HashMap<>();
		registerAppenderTypes();
	}

	private void registerAppenderTypes() {
		registerAppenderType(BroadcastEntryAppender.ID, BroadcastEntryAppender.class);
		registerAppenderType(SnitchLogAppender.ID, SnitchLogAppender.class);
		registerAppenderType(LeverToggleAppender.ID, LeverToggleAppender.class);
		registerAppenderType(DormantCullingAppender.ID, DormantCullingAppender.class);
		registerAppenderType(ShowOwnerOnDestroyAppender.ID, ShowOwnerOnDestroyAppender.class);
	}

	private void registerAppenderType(String id, Class<? extends AbstractSnitchAppender> clazz) {
		appenderClasses.put(id.toLowerCase(), clazz);
	}

	public boolean parseFromConfig(ConfigurationSection config) {
		Logger logger = JukeAlert.getInstance().getLogger();
		SnitchFactoryType.ItemInfo item = getItemInfo(config);
		StringBuilder sb = new StringBuilder();
		if (item == null) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no item specified");
			return false;
		}
		if (!config.isInt("id")) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no id specified");
			return false;
		}
		int id = config.getInt("id");
		if (!config.isString("name")) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no name specified");
			return false;
		}
		String name = config.getString("name");
		Function<Snitch, FieldManager> fieldGenerator = getFieldInstanciation(config);
		if (fieldGenerator == null) {
			logger.warning("Snitch type at " + config.getCurrentPath() + " had no valid field specified");
			return false;
		}
		sb.append("Successfully parsed type ");
		sb.append(name);
		sb.append(" with id: ");
		sb.append(id);
		sb.append(", item.material: ");
		sb.append(item.material);
		sb.append(", item.name: ");
		sb.append(item.name);
		sb.append(", item.lore: ");
		sb.append(item.lore);
		sb.append(", appenders: ");
		List<Function<Snitch, AbstractSnitchAppender>> appenderInstanciations = new ArrayList<>();
		if (config.isConfigurationSection("appender")) {
			ConfigurationSection appenderSection = config.getConfigurationSection("appender");
			for (String key : appenderSection.getKeys(false)) {
				if (!appenderSection.isConfigurationSection(key)) {
					logger.warning("Ignoring invalid entry " + key + " at " + appenderSection);
					continue;
				}
				Class<? extends AbstractSnitchAppender> appenderClass = appenderClasses.get(key.toLowerCase());
				if (appenderClass == null) {
					logger.warning("Appender " + key + " at " + appenderSection + " is of an unknown type");
					// this is not something we should just ignore, disregard entire config in this
					// case
					return false;
				}
				ConfigurationSection entrySection = appenderSection.getConfigurationSection(key);
				Function<Snitch, AbstractSnitchAppender> instanciation = getAppenderInstantiation(
						appenderClass, entrySection);
				appenderInstanciations.add(instanciation);
				sb.append(appenderClass.getSimpleName());
				sb.append("   ");
			}
		}
		if (appenderInstanciations.isEmpty()) {
			logger.warning("Snitch config at "  + config.getCurrentPath() + " has no appenders, this is likely not what you intended");
		}
		SnitchFactoryType configFactory = new SnitchFactoryType(item, fieldGenerator, name, id, appenderInstanciations);
		configFactoriesById.put(configFactory.getID(), configFactory);
		configFactories.add(configFactory);
		logger.info(sb.toString());
		return true;
	}

	private static SnitchFactoryType.ItemInfo getItemInfo(ConfigurationSection config) {
		String type = config.getString("item.material");
		String name = config.getString("item.name");
		List<String> lore = config.getStringList("item.lore");

		Material material = Material.getMaterial(type);

		SnitchFactoryType.ItemInfo item = new SnitchFactoryType.ItemInfo();
		item.material = material;
		item.name = name;
		item.lore = lore;

		return item;
	}
	
	private Function<Snitch, FieldManager> getFieldInstanciation(ConfigurationSection config) {
		Logger logger = JukeAlert.getInstance().getLogger();
		if (config.isInt("range")) {
			int range = config.getInt("range");
			return (s -> new SingleCuboidRangeManager(range, s));
		}
		ConfigurationSection rangeSection = config.getConfigurationSection("range");
		if (rangeSection == null) {
			logger.warning("Snitch config at "  + config.getCurrentPath() + " had no range config");
			return null;
		}
		String type = rangeSection.getString("type", "none");
		switch(type) {
		case "cube":
			int range = rangeSection.getInt("range", 11);
			logger.info("Parsed cube FieldManager with range " + range);
			return (s -> new SingleCuboidRangeManager(range, s));
		case "cuboid":
			int width = rangeSection.getInt("width");
			int height = rangeSection.getInt("height");
			logger.info("Parsed cuboid FieldManager with width " + width + " and height " + height);
			return (s -> new VariableSizeCuboidRangeManager(width, height, s));
		default:
			logger.warning("Unknown range type " + type + " at " + rangeSection.getCurrentPath());
			return null;
		}
		
	}

	/**
	 * Creates a function which will instanciate the appender based on the
	 * ConfigurationSection give to it
	 * 
	 * @param clazz Class of the appender
	 * @return Function to instanciate appenders of the given class or null if the
	 *         appender has no appropriate constructor
	 */
	private Function<Snitch, AbstractSnitchAppender> getAppenderInstantiation(
			Class<? extends AbstractSnitchAppender> clazz, ConfigurationSection config) {
		try {
			Constructor<? extends AbstractSnitchAppender> constructor = clazz
					.getConstructor(Snitch.class, ConfigurationSection.class);
			return s -> {
				try {
					return constructor.newInstance(s,config);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
					return null;
				}
			};
		} catch (NoSuchMethodException | SecurityException e) {
			// no config section constructor, which is fine if the appender does not have
			// any parameter, in which case it only has a constructor with the snitch as parameter
			try {
				Constructor<? extends AbstractSnitchAppender> constructor = clazz.getConstructor(Snitch.class);
				return s -> {
					try {
						return constructor.newInstance(s);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e1) {
						e1.printStackTrace();
						return null;
					}
				};
			} catch (NoSuchMethodException | SecurityException e1) {
				// No appropriate constructor, the appender has a bug
				e1.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Gets the configuration tied to the given ItemStack
	 * 
	 * @param is ItemStack to get configuration for
	 * @return Configuration with the given ItemStack or null if no such config
	 *         exists
	 */
	public SnitchFactoryType getConfig(ItemStack is) {
		for (SnitchFactoryType f : this.configFactories) {
			if (f.isSame(is))
				return f;
		}

		return null;
	}

	public SnitchFactoryType getConfig(int id) {
		return configFactoriesById.get(id);
	}

}
