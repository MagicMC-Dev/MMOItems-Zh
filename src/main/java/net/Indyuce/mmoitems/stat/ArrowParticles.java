package net.Indyuce.mmoitems.stat;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.ArrowParticlesEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ArrowParticlesData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ArrowParticles extends ItemStat {
	public ArrowParticles() {
		super("ARROW_PARTICLES", VersionMaterial.LIME_STAINED_GLASS.toItem(), "Arrow Particles",
				new String[] { "Particles that display around", "the arrows your bow fires." }, new String[] { "bow", "crossbow" });
	}

	@Override
	public ArrowParticlesData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specifiy a valid config section");
		ConfigurationSection config = (ConfigurationSection) object;

		Validate.isTrue(config.contains("particle"), "Could not find arrow particle");

		Particle particle = Particle.valueOf(config.getString("particle").toUpperCase().replace("-", "_").replace(" ", "_"));
		int amount = config.getInt("amount");
		double offset = config.getDouble("offset");

		return ParticleData.isColorable(particle)
				? new ArrowParticlesData(particle, amount, offset, config.getInt("color.red"), config.getInt("color.green"),
						config.getInt("color.blue"))
				: new ArrowParticlesData(particle, amount, offset, config.getDouble("speed"));
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("MMOITEMS_ARROW_PARTICLES", data.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("MMOITEMS_ARROW_PARTICLES"))
			try {
				JsonObject json = new JsonParser().parse(mmoitem.getNBT().getString("MMOITEMS_ARROW_PARTICLES")).getAsJsonObject();

				Particle particle = Particle.valueOf(json.get("Particle").getAsString());
				int amount = json.get("Amount").getAsInt();
				double offset = json.get("Offset").getAsDouble();

				mmoitem.setData(ItemStat.ARROW_PARTICLES,
						ParticleData.isColorable(particle)
								? new ArrowParticlesData(particle, amount, offset, json.get("Red").getAsInt(), json.get("Green").getAsInt(),
										json.get("Blue").getAsInt())
								: new ArrowParticlesData(particle, amount, offset, json.get("Speed").getAsDouble()));
			} catch (JsonSyntaxException exception) {
				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
	}

	@Override
	public void whenClicked(EditionInventory inv, InventoryClickEvent event) {
		new ArrowParticlesEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
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

			config.getConfig().set(inv.getEdited().getId() + ".arrow-particles.color.red", red);
			config.getConfig().set(inv.getEdited().getId() + ".arrow-particles.color.green", green);
			config.getConfig().set(inv.getEdited().getId() + ".arrow-particles.color.blue", blue);
			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle color successfully set to "
					+ ChatColor.translateAlternateColorCodes('&', "&c&l" + red + "&7 - &a&l" + green + "&7 - &9&l" + blue));
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

			config.getConfig().set(inv.getEdited().getId() + ".arrow-particles.particle", particle.name());
			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle successfully set to " + ChatColor.GOLD
					+ MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + ChatColor.GRAY + ".");
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

			config.getConfig().set(inv.getEdited().getId() + ".arrow-particles.amount", value);
			inv.registerTemplateEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + "Amount" + ChatColor.GRAY + " set to " + ChatColor.GOLD + value
					+ ChatColor.GRAY + ".");
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

		config.getConfig().set(inv.getEdited().getId() + ".arrow-particles." + edited, value);
		inv.registerTemplateEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY
				+ " set to " + ChatColor.GOLD + value + ChatColor.GRAY + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<RandomStatData> optional) {

		if (!optional.isPresent())
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		else {
			ArrowParticlesData cast = (ArrowParticlesData) optional.get();
			lore.add(ChatColor.GRAY + "Current Value:");

			lore.add(ChatColor.GRAY + "* Particle: " + ChatColor.GOLD
					+ MMOUtils.caseOnWords(cast.getParticle().name().replace("_", " ").toLowerCase()));
			lore.add(ChatColor.GRAY + "* Amount: " + ChatColor.WHITE + cast.getAmount());
			lore.add(ChatColor.GRAY + "* Offset: " + ChatColor.WHITE + cast.getOffset());
			lore.add("");

			if (ParticleData.isColorable(cast.getParticle()))
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&7* Color: &c&l" + cast.getRed() + "&7 - &a&l" + cast.getGreen() + "&7 - &9&l" + cast.getBlue()));
			else
				lore.add(ChatColor.GRAY + "* Speed: " + ChatColor.WHITE + cast.getSpeed());
		}

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit.");
	}
}
