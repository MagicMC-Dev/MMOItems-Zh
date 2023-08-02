package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class MaterialData implements StatData, RandomStatData<MaterialData> {
	private Material material;

	/*
	 * material must not be null because it is called directly in the
	 * MMOBuilder constructor.
	 */
	public MaterialData(Material material) {
		Validate.notNull(material, "材质不能为空");
		this.material = material;
	}

	public void setMaterial(Material material) {
		Validate.notNull(material, "材质不能为空");
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public MaterialData randomize(MMOItemBuilder builder) {
		return this;
	}
}