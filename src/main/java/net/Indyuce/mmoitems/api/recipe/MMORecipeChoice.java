package net.Indyuce.mmoitems.api.recipe;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class MMORecipeChoice {
	private final Material material;
	private final int data;

	private final Type type;
	private final String id;

	public MMORecipeChoice(Material material, int data) {
		this(material, data, null, null);
	}

	public MMORecipeChoice(Type type, String id) {
		this(null, 0, type, id);
	}

	public MMORecipeChoice(String input) {
		if (input.contains(".")) {
			String[] typeId = input.split("\\.");
			String typeFormat = typeId[0].toUpperCase().replace("-", "_").replace(" ", "_");
			Validate.isTrue(MMOItems.plugin.getTypes().has(typeFormat), "Could not find type " + typeFormat);

			type = MMOItems.plugin.getTypes().get(typeFormat);
			id = typeId[1];

			data = 0;
			material = null;
		}

		else {
			String[] split = input.split("\\:");

			material = Material.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
			data = split.length > 1 ? Integer.parseInt(split[1]) : 0;

			type = null;
			id = null;
		}
	}

	private MMORecipeChoice(Material material, int data, Type type, String id) {
		this.type = type;
		this.id = id;
		this.material = material;
		this.data = data;
	}

	@SuppressWarnings("deprecation")
	public ItemStack generateStack() {
		return material != null ? (data > 0 ? new ItemStack(material, 1, (short) data) : new ItemStack(material)) : MMOItems.plugin.getItems().getItem(type, id);
	}

	public boolean isAir() {
		return material == Material.AIR;
	}

	public Material getMaterial() {
		return material;
	}

	public Type getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public int getMeta() {
		return data;
	}

	public static List<MMORecipeChoice> getFromShapedConfig(List<String> list) {
		List<MMORecipeChoice> choices = new ArrayList<>();

		for (String key : list)
			for (String subkey : key.split("\\ "))
				choices.add(new MMORecipeChoice(subkey));

		return choices;
	}
}
