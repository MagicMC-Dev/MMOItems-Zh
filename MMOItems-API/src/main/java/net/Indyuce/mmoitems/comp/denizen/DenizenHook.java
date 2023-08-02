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
				attribute.echoError("请提供物品类型和 ID");
				return null;
			}

			MapTag map = attribute.contextAsType(1, MapTag.class);
			if (map == null) {
				attribute.echoError("地图标签输入无效");
				return null;
			}

			ObjectTag type = map.getObject("type");
			ObjectTag id = map.getObject("id");
			if (type == null || id == null) {
				attribute.echoError("输入的地图标签无效 - 缺少 'type' 或 'id'");
				return null;
			}

			String typeName = type.toString().replace("-", "_").toUpperCase();
			Type parsedType = MMOItems.plugin.getTypes().get(typeName);
			if (parsedType == null) {
				attribute.echoError("类型无效 - 找不到名称为 '" + typeName + "' 的类型");
				return null;
			}

			// Format ID and return item
			String formattedId = id.toString().replace("-", "_").toUpperCase();
			if (!MMOItems.plugin.getTemplates().hasTemplate(parsedType, formattedId)) {
				attribute.echoError("模板 ID 无效 - 找不到名称为 '" + formattedId + "' 的模板");
				return null;
			}

			return new MMOItemTemplateTag(parsedType, formattedId);
		});
	}
}
