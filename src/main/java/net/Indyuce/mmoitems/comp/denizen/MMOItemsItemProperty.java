package net.Indyuce.mmoitems.comp.denizen;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.util.identify.IdentifiedItem;

public class MMOItemsItemProperty implements Property {
    private final ItemTag item;

    public MMOItemsItemProperty(ItemTag item) {
        this.item = item;
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "MMOItemsItem";
    }

    public boolean isMMOItem() {
        return NBTItem.get(item.getItemStack()).hasTag("MMOITEMS_TYPE");
    }

    public MMOItemTag getMMOItem() {
        return new MMOItemTag(item);
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }

    public static boolean describes(ObjectTag object) {
        return object instanceof ItemTag;
    }

    public static MMOItemsItemProperty getFrom(ObjectTag object) {
        return object instanceof ItemTag ? new MMOItemsItemProperty((ItemTag) object) : null;
    }

    public static final String[] handledMechs = new String[]{}; // None

    public static void registerTags() {

        /*
         * Used to check if an ItemTag is an item generated
         * using MMOItems or not
         *
         * Usage:
         * <player.item_in_hand.is_mmoitem>
         *
         * Returns:
         * Element tag containing a boolean
         */
        PropertyParser.<MMOItemsItemProperty>registerTag("is_mmoitem", (attribute, object) -> new ElementTag(object.isMMOItem()));

        /*
         * Used to check if an item is an unidentified item from MMOItems
         *
         * Usage:
         * <player.item_in_hand.is_unidentified_item>
         *
         * Returns:
         * Element tag containing a boolean
         */
        PropertyParser.<MMOItemsItemProperty>registerTag("is_unidentified_item", (attribute, object) -> new ElementTag(NBTItem.get(object.item.getItemStack()).hasTag("MMOITEMS_UNIDENTIFIED_ITEM")));

        /*
         * Used to identify an item
         *
         * Usage:
         * <player.item_in_hand.identify>
         *
         * Returns:
         * MMOItemTag corresponding to the identified item
         */
        PropertyParser.<MMOItemsItemProperty>registerTag("identify", (attribute, object) -> new MMOItemTag(new ItemTag(new IdentifiedItem(NBTItem.get(object.item.getItemStack())).identify())));

        /*
         * Used to access utility methods for manipulating
         * mmoitems
         *
         * Usage:
         * <player.item_in_hand.mmoitem>
         *
         * Returns:
         * MMOItemTag used to manipulate the item
         */
       /* PropertyParser.<MMOItemsItemProperty>registerTag("mmoitem", (attribute, object) -> object.isMMOItem() ? object.getMMOItem() : null);*/
    }
}
