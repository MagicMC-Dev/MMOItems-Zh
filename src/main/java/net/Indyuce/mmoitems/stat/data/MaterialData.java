package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class MaterialData implements StatData, RandomStatData {
	private Material material;

	/*
	 * material must not be null because it is called directly in the
	 * MMOBuilder constructor.
	 */
	public MaterialData(Material material) {
		Validate.notNull(material, "Material must not be null");
		this.material = material;
	}

	public void setMaterial(Material material) {
		Validate.notNull(material, "Material must not be null");
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return this;
	}
}