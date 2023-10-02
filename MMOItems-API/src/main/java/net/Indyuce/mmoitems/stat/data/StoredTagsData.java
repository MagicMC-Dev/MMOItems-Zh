package net.Indyuce.mmoitems.stat.data;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.interaction.ItemSkin;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoredTagsData implements StatData, Mergeable<StoredTagsData> {
    private final List<ItemTag> tags = new ArrayList<>();

    private static final List<String> IGNORED_TAGS = Arrays.asList(
            "Unbreakable", "BlockEntityTag", "display", "Enchantments", "HideFlags", "Damage",
            "AttributeModifiers", "SkullOwner", "CanDestroy", "PickupDelay", "Age");

    /**
     * TODO Make this list publically accessible to other plugins
     */
    @Deprecated
    private static final List<String> SAVED_MMOITEMS_TAGS = Arrays.asList(
            ItemSkin.SKIN_ID_TAG,
            ItemSkin.SKIN_TYPE_TAG);

    @Deprecated
    public StoredTagsData(ItemStack stack) {
        this(NBTItem.get(stack));
    }

    public StoredTagsData(List<ItemTag> tgs) {
        tags.addAll(tgs);
    }

    public StoredTagsData(NBTItem nbt) {
        for (String tag : nbt.getTags()) {

            // Usually ignore mmoitems
            if (tag.startsWith("MMOITEMS_") && !SAVED_MMOITEMS_TAGS.contains(tag))

                // Must be handled by its respective stat.
                continue;

            // Any vanilla or MMOItem tag should be ignored as those are
            // automatically handled. Same for the History stat ones.
            if (IGNORED_TAGS.contains(tag) || tag.startsWith(ItemStackBuilder.history_keyword))
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
    public void merge(StoredTagsData data) {
        tags.addAll(data.tags);
    }

    @Override
    public StoredTagsData cloneData() {
        return new StoredTagsData(getTags());
    }

    @Override
    public boolean isEmpty() {
        return tags.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StoredTagsData)) {
            return false;
        }

        if (((StoredTagsData) obj).getTags().size() != getTags().size()) {
            return false;
        }

        for (ItemTag tag : ((StoredTagsData) obj).getTags()) {

            if (tag == null) {
                continue;
            }

            boolean unmatched = true;
            for (ItemTag tg : getTags()) {
                if (tag.equals(tg)) {
                    unmatched = false;
                    break;
                }
            }
            if (unmatched) {
                return false;
            }
        }
        return true;
    }
}