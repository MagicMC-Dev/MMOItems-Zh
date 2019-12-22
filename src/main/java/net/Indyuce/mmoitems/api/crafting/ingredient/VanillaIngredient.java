package net.Indyuce.mmoitems.api.crafting.ingredient;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.mmogroup.mmolib.api.item.NBTItem;

public class VanillaIngredient extends Ingredient {
	private Material material;
	private String displayName;

	public VanillaIngredient() {
		super("vanilla");
		setDisplay(new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"));
	}

	@Override
	public Ingredient load(String[] args) {
		try {
			VanillaIngredient ingredient = new VanillaIngredient();
			ingredient.material = Material.valueOf(args[0]);
			ingredient.displayName = args.length > 1 ? ((displayName = args[1].replace("_", " ")).equals(".") ? null : displayName) : null;

			ingredient.setAmount(args.length > 2 ? Math.max(1, Integer.parseInt(args[2])) : 1);
			ingredient.setName(args.length > 3 ? args[3].replace("_", " ") : (ingredient.displayName == null ? MMOUtils.caseOnWords(ingredient.material.name().toLowerCase().replace("_", " ")) : ingredient.displayName));
			ingredient.setKey(ingredient.material.name().toLowerCase() + "_" + ingredient.displayName);
			ingredient.setDisplay(getDisplay());

			return ingredient;
		} catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
			return null;
		}
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#item#", getName()).replace("#amount#", "" + getAmount());
	}

	@Override
	public boolean isValid(NBTItem item) {
		return true;
	}

	@Override
	public String readKey(NBTItem item) {
		return item.getItem().getType().name().toLowerCase() + "_" + (item.getItem().hasItemMeta() ? item.getItem().getItemMeta().getDisplayName() : null);
	}

	@Override
	public ItemStack generateItemStack() {
		ItemStack item = new ItemStack(material, getAmount());
		if (displayName != null) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(displayName);
			item.setItemMeta(meta);
		}
		return item;
	}
}
