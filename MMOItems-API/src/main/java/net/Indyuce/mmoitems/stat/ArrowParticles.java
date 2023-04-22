package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.ArrowParticlesEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ArrowParticlesData;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArrowParticles extends ItemStat<ArrowParticlesData, ArrowParticlesData> {
	public ArrowParticles() {
		super("ARROW_PARTICLES", VersionMaterial.LIME_STAINED_GLASS.toMaterial(), "Arrow Particles",
				new String[] { "Particles that display around", "the arrows your bow fires." }, new String[] { "bow", "crossbow" });
	}

	@Override
	public ArrowParticlesData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "Must specify a valid config section");
		ConfigurationSection config = (ConfigurationSection) object;

		Validate.isTrue(config.contains("particle"), "Could not find arrow particle");

		Particle particle = Particle.valueOf(config.getString("particle").toUpperCase().replace("-", "_").replace(" ", "_"));
		int amount = config.getInt("amount");
		double offset = config.getDouble("offset");

		return MMOUtils.isColorable(particle)
				? new ArrowParticlesData(particle, amount, offset, config.getInt("color.red"), config.getInt("color.green"),
						config.getInt("color.blue"))
				: new ArrowParticlesData(particle, amount, offset, config.getDouble("speed"));
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ArrowParticlesData data) {
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull ArrowParticlesData data) {
		ArrayList<ItemTag> tags = new ArrayList<>();
		tags.add(new ItemTag(getNBTPath(), data.toString()));
		return tags;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get relvant tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// Get Data
		StatData data = getLoadedNBT(relevantTags);

		// Valid?
		if (data != null) {

			// Set item stat
			mmoitem.setData(this, data);
		}
	}

	@Nullable
	@Override
	public ArrowParticlesData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Get tag
		ItemTag tagS = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		 if (tagS != null) {
			 try {
				 // Parse as Json Object
				 JsonObject json = new JsonParser().parse((String) tagS.getValue()).getAsJsonObject();

				 // Build
				 Particle particle = Particle.valueOf(json.get("Particle").getAsString());
				 int amount = json.get("Amount").getAsInt();
				 double offset = json.get("Offset").getAsDouble();

				 // Ist it colorable'
				 if (MMOUtils.isColorable(particle)) {

					 // Return as colourable
					 return new ArrowParticlesData(particle, amount, offset, json.get("Red").getAsInt(), json.get("Green").getAsInt(), json.get("Blue").getAsInt());

					 // Not colourable
				 } else {

					 // Return as speedy
					 return new ArrowParticlesData(particle, amount, offset, json.get("Speed").getAsDouble());
				 }

			 } catch (JsonSyntaxException|IllegalStateException exception) {
				 /*
				  * OLD ITEM WHICH MUST BE UPDATED.
				  */
			 }
		 }

		 // Fail
		return null;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		new ArrowParticlesEdition(inv.getPlayer(), inv.getEdited()).open(inv.getPage());
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String edited = (String) info[0];

		if (edited.equals("color")) {
			String[] split = message.split(" ");
			int red = Integer.parseInt(split[0]), green = Integer.parseInt(split[1]), blue = Integer.parseInt(split[2]);
			inv.getEditedSection().set("arrow-particles.color.red", red);
			inv.getEditedSection().set("arrow-particles.color.green", green);
			inv.getEditedSection().set("arrow-particles.color.blue", blue);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle color successfully set to "
					+ ChatColor.translateAlternateColorCodes('&', "&c&l" + red + "&7 - &a&l" + green + "&7 - &9&l" + blue));
			return;
		}

		if (edited.equals("particle")) {
			Particle particle = Particle.valueOf(message.toUpperCase().replace("-", "_").replace(" ", "_"));
			inv.getEditedSection().set("arrow-particles.particle", particle.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Particle successfully set to " + ChatColor.GOLD
					+ MMOUtils.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + ChatColor.GRAY + ".");
			return;
		}

		if (edited.equals("amount")) {
			int value = Integer.parseInt(message);
			inv.getEditedSection().set("arrow-particles.amount", value);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + "Amount" + ChatColor.GRAY + " set to " + ChatColor.GOLD + value
					+ ChatColor.GRAY + ".");
			return;
		}

		// offset & speed
		double value = MMOUtils.parseDouble(message);
		inv.getEditedSection().set("arrow-particles." + edited, value);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY
				+ " set to " + ChatColor.GOLD + value + ChatColor.GRAY + ".");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<ArrowParticlesData> statData) {
		if (statData.isPresent()) {
			ArrowParticlesData cast = statData.get();
			lore.add(ChatColor.GRAY + "Current Value:");

			lore.add(ChatColor.GRAY + "* Particle: " + ChatColor.GOLD
					+ MMOUtils.caseOnWords(cast.getParticle().name().replace("_", " ").toLowerCase()));
			lore.add(ChatColor.GRAY + "* Amount: " + ChatColor.WHITE + cast.getAmount());
			lore.add(ChatColor.GRAY + "* Offset: " + ChatColor.WHITE + cast.getOffset());
			lore.add("");

			if (MMOUtils.isColorable(cast.getParticle()))
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&7* Color: &c&l" + cast.getRed() + "&7 - &a&l" + cast.getGreen() + "&7 - &9&l" + cast.getBlue()));
			else
				lore.add(ChatColor.GRAY + "* Speed: " + ChatColor.WHITE + cast.getSpeed());
		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit.");
	}

	@NotNull
	@Override
	public ArrowParticlesData getClearStatData() {
		return new ArrowParticlesData(Particle.EXPLOSION_LARGE, 1, 0, 1);
	}
}
