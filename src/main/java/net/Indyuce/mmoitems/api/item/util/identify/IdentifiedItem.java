package net.Indyuce.mmoitems.api.item.util.identify;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import net.mmogroup.mmolib.api.item.NBTItem;

public class IdentifiedItem {
	private NBTItem item;

	public IdentifiedItem(NBTItem item) {
		this.item = item;
	}

	/*
	 * the identified item is stored in an item NBTTag, identifying the item
	 * basically replaces the item for the one saved in the NBT
	 */
	public ItemStack identify() {
		return deserialize(item.getString("MMOITEMS_UNIDENTIFIED_ITEM"));
	}

	private ItemStack deserialize(String data) {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			ItemStack item = (ItemStack) dataInput.readObject();
			dataInput.close();
			return item;
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
