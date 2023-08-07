package net.Indyuce.mmoitems.comp.denizen;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.TagManager;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class DenizenHook {
	/**
	 * Putting this here so that the Depenizen import does not
	 * appear in the main MMOItems class which would cause an issue
	 * for all servers without Denizen
	 */
	public DenizenHook() {
		/*
		 * This registers sub tags in the custom coded denizen tags
		 */
		/* ObjectFetcher.registerWithObjectFetcher(MMOItemTag.class, MMOItemTag.tagProcessor);*/
		ObjectFetcher.registerWithObjectFetcher(MMOItemTemplateTag.class, MMOItemTemplateTag.tagProcessor);

		/*
		 * Implement some properties to the already existing ItemTag
		 */
		PropertyParser.registerProperty(MMOItemsItemProperty.class, ItemTag.class);

		/*
		 * This implements a way to retrieve an MMOItem as itemStack.
		 *
		 * Usage:
		 * <mmoitem_template[type=TYPE;id=ID_HERE]>
		 */
		TagManager.registerTagHandler("mmoitem_template", attribute -> {
			if (!attribute.hasContext(1)) {
				attribute.echoError("Please provide an item type and ID.");
				return null;
			}

			MapTag map = attribute.contextAsType(1, MapTag.class);
			if (map == null) {
				attribute.echoError("Invalid MapTag input");
				return null;
			}

			ObjectTag type = map.getObject("type");
			ObjectTag id = map.getObject("id");
			if (type == null || id == null) {
				attribute.echoError("Invalid MapTag input - missing 'type' or 'id'");
				return null;
			}

			String typeName = type.toString().replace("-", "_").toUpperCase();
			Type parsedType = MMOItems.plugin.getTypes().get(typeName);
			if (parsedType == null) {
				attribute.echoError("Invalid type - cannot find type with name '" + typeName + "'");
				return null;
			}

			// Format ID and return item
			String formattedId = id.toString().replace("-", "_").toUpperCase();
			if (!MMOItems.plugin.getTemplates().hasTemplate(parsedType, formattedId)) {
				attribute.echoError("Invalid template ID - cannot find template with name '" + formattedId + "'");
				return null;
			}

			return new MMOItemTemplateTag(parsedType, formattedId);
		});
	}
}
