package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class StoredTagsData implements StatData, Mergeable {
	private final List<ItemTag> tags = new ArrayList<>();

	private static final List<String> ignoreList = Arrays.asList("Unbreakable", "BlockEntityTag", "display", "Enchantments", "HideFlags", "Damage",
			"AttributeModifiers", "SkullOwner", "CanDestroy", "PickupDelay", "Age");

	@Deprecated
	public StoredTagsData(ItemStack stack) {
		this(NBTItem.get(stack));
	}

	public StoredTagsData(NBTItem nbt) {
		for (String tag : nbt.getTags()) {
			// Any vanilla or MMOItem tag should be ignored as those are
			// automatically handled
			if (ignoreList.contains(tag) || tag.startsWith("MMOITEMS_"))
				continue;

			// As more methods are added we can add more types here
			switch (getTagType(nbt.getTypeId(tag))) {
			case "double":
				tags.add(new ItemTag(tag, nbt.getDouble(tag)));
				break;
			case "int":
				tags.add(new ItemTag(tag, nbt.getInteger(tag)));
				break;
			case "byte":
				tags.add(new ItemTag(tag, nbt.getBoolean(tag)));
				break;
			case "string":
				tags.add(new ItemTag(tag, nbt.getString(tag)));
				break;
			// default:
			// tags.add(new ItemTag(tag, "UNSUPPORTED TAG TYPE!"));
			}
		}
	}
	
	public void addTag(ItemTag tag) {
		tags.add(tag);
	}

	public List<ItemTag> getTags() {
		return tags;
	}

	private String getTagType(int id) {
		switch (id) {
		case 0:
			return "end";
		case 1:
			return "byte";
		case 2:
			return "short";
		case 3:
			return "int";
		case 4:
			return "long";
		case 5:
			return "float";
		case 6:
			return "double";
		case 7:
			return "bytearray";
		case 8:
			return "string";
		case 9:
			return "list";
		case 10:
			return "compound";
		case 11:
			return "intarray";
		default:
			return "unknown";
		}
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof StoredTagsData, "Cannot merge two different stat data types");
		tags.addAll(((StoredTagsData) data).tags);
	}
}