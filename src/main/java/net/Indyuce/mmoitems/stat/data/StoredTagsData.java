package net.Indyuce.mmoitems.stat.data;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoredTagsData implements StatData, Mergeable {
	private final List<ItemTag> tags = new ArrayList<>();

	private static final List<String> ignoreList = Arrays.asList("Unbreakable", "BlockEntityTag", "display", "Enchantments", "HideFlags", "Damage",
			"AttributeModifiers", "SkullOwner", "CanDestroy", "PickupDelay", "Age");

	@Deprecated
	public StoredTagsData(ItemStack stack) {
		this(NBTItem.get(stack));
	}
	public StoredTagsData(List<ItemTag> tgs) { tags.addAll(tgs); }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StoredTagsData)) { return false; }

		if (((StoredTagsData) obj).getTags().size() != getTags().size()) { return false; }

		for (ItemTag tag : ((StoredTagsData) obj).getTags()) {

			if (tag == null) { continue; }

			boolean unmatched = true;
			for (ItemTag tg : getTags()) {
				if (tag.equals(tg)) { unmatched = false; break; } }
			if (unmatched) { return false; } }
		return true;
	}

	public StoredTagsData(NBTItem nbt) {
		for (String tag : nbt.getTags()) {
			// Any vanilla or MMOItem tag should be ignored as those are
			// automatically handled. Same for the History stat ones.
			if (ignoreList.contains(tag) || tag.startsWith("MMOITEMS_") || tag.startsWith(ItemStackBuilder.history_keyword))
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

	@Override
	public @NotNull
	StatData cloneData() { return new StoredTagsData(getTags()); }

	@Override
	public boolean isClear() {
		return getTags().size() == 0;
	}
}