package net.Indyuce.mmoitems.comp.mythicmobs.crafting;

import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MythicItemIngredient extends Ingredient {
	private final MythicItem mythicitem;

	private final String display;

	public MythicItemIngredient(MMOLineConfig config) {
		super("mythicitem", config);

		config.validate("item");
		Optional<MythicItem> mmitem = MythicMobs.inst().getItemManager().getItem(config.getString("item"));
		Validate.isTrue(mmitem.isPresent(), "Could not find MM Item with ID '" + config.getString("item") + "'");

		display = config.contains("display") ? config.getString("display") : mmitem.get().getDisplayName();
		mythicitem = mmitem.get();
	}

	@Override
	public String getKey() {
		return "mythicitem:" + mythicitem.getInternalName().toLowerCase();
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#item#", display).replace("#amount#", "" + getAmount());
	}

	@Override
	public ItemStack generateItemStack(RPGPlayer player) {
		return BukkitAdapter.adapt(mythicitem.generateItemStack(getAmount()));
	}

	@Override
	public String toString() {
		return getKey();
	}
}
