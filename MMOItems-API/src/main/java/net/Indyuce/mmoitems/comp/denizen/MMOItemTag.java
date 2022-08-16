package net.Indyuce.mmoitems.comp.denizen;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;

@Deprecated
public class MMOItemTag extends SimpleTag {
    private final ItemTag item;

    @Deprecated
    public MMOItemTag(ItemTag item) {
        this.item = item;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getObjectType() {
        return "MMOItem";
    }

    @Override
    public String identify() {
        return "mmoitem@" + item.identify();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    public static ObjectTagProcessor<MMOItemTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public static void registerTags() {

        /*
         * Used to get the corresponding itemStack
         *
         * Usage:
         * <mmoitemTag.item>
         *
         * Returns:
         * ItemTag
         */
        tagProcessor.registerTag("item", (attribute, object) -> object.item);
    }
}
