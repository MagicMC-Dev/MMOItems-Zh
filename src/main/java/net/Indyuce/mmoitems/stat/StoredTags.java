package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class StoredTags extends InternalStat {
	private static final List<String> ignoreList = Arrays.asList("Unbreakable", "BlockEntityTag", "display", "Enchantments", "HideFlags", "Damage", "AttributeModifiers", "SkullOwner", "CanDestroy", "PickupDelay", "Age");

	public StoredTags() {
		super("STORED_TAGS", VersionMaterial.OAK_SIGN.toItem(), "Stored Tags", new String[] { "You found a secret dev easter egg", "introduced during the 2020 epidemic!" }, new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		for (ItemTag tag : ((StoredTagsData) data).getTags())
			item.addItemTag(tag);
		return false;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		mmoitem.setData(ItemStat.STORED_TAGS, new StoredTagsData(item));
	}

	public class StoredTagsData extends StatData {
		private final List<ItemTag> tags = new ArrayList<>();

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
	}
}
