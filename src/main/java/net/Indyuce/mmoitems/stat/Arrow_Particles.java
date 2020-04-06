package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.ArrowParticlesEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Arrow_Particles extends StringStat {
	public Arrow_Particles() {
		super("ARROW_PARTICLES", VersionMaterial.LIME_STAINED_GLASS.toItem(), "Arrow Particles", new String[] { "Particles that display around", "the arrows your bow fires." }, new String[] { "bow", "crossbow" });
	}

	@Override
	public void whenLoaded(MMOItem item, ConfigurationSection config) {
		Validate.isTrue(config.getConfigurationSection("arrow-particles").contains("particle"), "Could not find arrow particle");

		ArrowParticlesData data = new ArrowParticlesData();
		String particleFormat = config.getString("arrow-particles.particle").toUpperCase().replace("-", "_").replace(" ", "_");
		data.particle = Particle.valueOf(particleFormat);

		data.amount = config.getInt("arrow-particles.amount");
		data.offset = config.getDouble("arrow-particles.offset");

		if (ParticleData.isColorable(data.particle)) {
			data.red = config.getInt("arrow-particles.color.red");
			data.green = config.getInt("arrow-particles.color.green");
			data.blue = config.getInt("arrow-particles.color.blue");
			data.colored = true;
		} else
			data.speed = config.getDouble("arrow-particles.speed");

		item.setData(ItemStat.ARROW_PARTICLES, data);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((ArrowParticlesData) data).isValid())
			item.addItemTag(new ItemTag("MMOITEMS_ARROW_PARTICLES", data.toString()));
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem nbtItem) {
		if (nbtItem.hasTag("MMOITEMS_ARROW_PARTICLES"))
			try {
				JsonObject json = new JsonParser().parse(nbtItem.getString("MMOITEMS_ARROW_PARTICLES")).getAsJsonObject();
				ArrowParticlesData data = new ArrowParticlesData();

				data.particle = Particle.valueOf(json.get("Particle").getAsString());
				data.amount = json.get("Amount").getAsInt();
				data.offset = json.get("Offset").getAsDouble();

				if (data.colored = json.get("Colored").getAsBoolean()) {
					data.red = json.get("Red").getAsInt();
					data.green = json.get("Green").getAsInt();
					data.blue = json.get("Blue").getAsInt();
				} else
					data.speed = json.get("Speed").getAsDouble();

				mmoitem.setData(ItemStat.ARROW_PARTICLES, data);
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new ArrowParticlesEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String edited = (String) info[0];

		if (edited.equals("color")) {
			String[] split = message.split("\\ ");
			int red = 0, green = 0, blue = 0;

			try {
				red = Integer.parseInt(split[0]);
				green = Integer.parseInt(split[1]);
				blue = Integer.parseInt(split[2]);
			} catch (Exception e) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Make sure you enter 3 valid numbers.");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".arrow-particles.color.red", red);
			config.getConfig().set(inv.getItemId() + ".arrow-particles.color.green", green);
			config.getConfig().set(inv.getItemId() + ".arrow-particles.color.blue", blue);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle color successfully set to " + ChatColor.translateAlternateColorCodes('&', "&c&l" + red + "&7 - &a&l" + green + "&7 - &9&l" + blue));
			return true;
		}

		if (edited.equals("particle")) {
			String format = message.toUpperCase().replace("-", "_").replace(" ", "_");
			Particle particle;
			try {
				particle = Particle.valueOf(format);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid particle!");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".arrow-particles.particle", particle.name());
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle successfully set to " + ChatColor.GOLD + MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + ChatColor.GRAY + ".");
			return true;
		}

		if (edited.equals("amount")) {
			int value = 0;
			try {
				value = Integer.parseInt(message);
			} catch (Exception e) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".arrow-particles.amount", value);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + "Amount" + ChatColor.GRAY + " set to " + ChatColor.GOLD + value + ChatColor.GRAY + ".");
			return true;
		}

		// offset & speed
		double value = 0;
		try {
			value = Double.parseDouble(message);
		} catch (Exception e) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number.");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".arrow-particles." + edited, value);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY + " set to " + ChatColor.GOLD + value + ChatColor.GRAY + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");

		try {
			Particle particle = Particle.valueOf(config.getString(path + ".arrow-particles.particle").toUpperCase().replace("-", "_").replace(" ", "_"));
			lore.add(ChatColor.GRAY + "* Particle: " + ChatColor.GOLD + MMOUtils.caseOnWords(particle.name().replace("_", " ").toLowerCase()));
			lore.add(ChatColor.GRAY + "* Amount: " + ChatColor.WHITE + config.getInt(path + ".arrow-particles.amount"));
			lore.add(ChatColor.GRAY + "* Offset: " + ChatColor.WHITE + config.getDouble(path + ".arrow-particles.offset"));
			lore.add("");
			if (ParticleData.isColorable(particle)) {
				double red = config.getDouble(path + ".arrow-particles.red"), green = config.getDouble(path + ".arrow-particles.green"), blue = config.getDouble(path + ".arrow-particles.blue");
				lore.add(ChatColor.translateAlternateColorCodes('&', "&7* Color: &c&l" + red + "&7 - &a&l" + green + "&7 - &9&l" + blue));
			} else
				lore.add(ChatColor.GRAY + "* Speed: " + ChatColor.WHITE + config.getDouble(path + ".arrow-particles.speed"));
		} catch (Exception e) {
			lore.add(ChatColor.RED + "No particle selected.");
			lore.add(ChatColor.RED + "Click to setup.");
		}

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit.");
	}

	public class ArrowParticlesData extends StatData {
		private Particle particle;
		private int amount, red, green, blue;
		private double speed, offset;
		private boolean colored = false, valid = true;

		public ArrowParticlesData() {
		}

		public ArrowParticlesData(Particle particle, int amount, double offset, double speed) {
			this.particle = particle;
			this.amount = amount;
			this.offset = offset;

			this.speed = speed;
		}

		public ArrowParticlesData(Particle particle, int amount, double offset, int red, int green, int blue) {
			this.particle = particle;
			this.amount = amount;
			this.offset = offset;

			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		public Particle getParticle() {
			return particle;
		}

		public boolean isColored() {
			return colored;
		}

		public int getAmount() {
			return amount;
		}

		public double getOffset() {
			return offset;
		}

		public double getSpeed() {
			return speed;
		}

		public int getRed() {
			return red;
		}

		public int getGreen() {
			return green;
		}

		public int getBlue() {
			return blue;
		}

		public boolean isValid() {
			return valid;
		}

		@Override
		public String toString() {
			JsonObject object = new JsonObject();
			object.addProperty("Particle", particle.name());
			object.addProperty("Amount", amount);
			object.addProperty("Offset", offset);
			object.addProperty("Colored", colored);
			if (colored) {
				object.addProperty("Red", red);
				object.addProperty("Green", green);
				object.addProperty("Blue", blue);
			} else
				object.addProperty("Speed", speed);

			return object.toString();
		}
	}
}
