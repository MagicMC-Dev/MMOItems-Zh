package net.Indyuce.mmoitems.comp.denizen;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;

import java.util.Random;

public class MMOItemTemplateTag extends SimpleTag {
	private final Type type;
	private final String id;

	private static final Random random = new Random();

	public MMOItemTemplateTag(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	public MMOItemTemplate getTemplate() {
		return MMOItems.plugin.getTemplates().getTemplate(type, id);
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public String getObjectType() {
		return "MMOItemTemplate";
	}

	@Override
	public String identify() {
		return "mmoitem_template@" + type.getId() + "." + id;
	}

	@Override
	public String identifySimple() {
		return identify();
	}

	public static ObjectTagProcessor<MMOItemTemplateTag> tagProcessor = new ObjectTagProcessor<>();

	@Override
	public ObjectTag getObjectAttribute(Attribute attribute) {
		return tagProcessor.getObjectAttribute(this, attribute);
	}

	public static void registerTags() {

		// Display template type name
		tagProcessor.registerTag("item_type", ((attribute, object) -> new ElementTag(object.type.getName())));

		// Display template id
		tagProcessor.registerTag("item_id", ((attribute, object) -> new ElementTag(object.id)));

		/*
		 * Used to generate an item with custom tier and level.
		 *
		 * Usage:
		 * <mmoitemTemplateTag.generate[player=playerTagHere;level=10;matchlevel=true;tier=TIER_NAME]
		 * All arguments are optional. Level overrides the match-level option.
		 *
		 * Return:
		 * MMOItemTag of the generated item.
		 */
		tagProcessor.registerTag("generate", (attribute, object) -> {
			if (!attribute.hasContext(1)) return new ItemTag(object.getTemplate().newBuilder().build().newBuilder().build());

			MapTag map = attribute.contextAsType(1, MapTag.class);
			if (map == null) {
				attribute.echoError("Invalid MapTag input");
				return null;
			}

			ObjectTag playerTag = map.getObject("player");
			if (playerTag != null && !(playerTag instanceof PlayerTag)) {
				attribute.echoError("Bad player input type");
				return null;
			}

			// Specified level
			ObjectTag levelTag = map.getObject("level");
			int level = -1;
			if (levelTag != null) try {
				level = Integer.parseInt(levelTag.toString());
			} catch (IllegalArgumentException exception) {
				attribute.echoError("Bad level input: " + levelTag + " is not a valid integer");
				return null;
			}

			// Match level
			ObjectTag matchLevelTag = map.getObject("match-level");
			boolean matchLevel = matchLevelTag != null && Boolean.parseBoolean(matchLevelTag.toString());

			// Item tier param
			ObjectTag tierTag = map.getObject("tier");
			ItemTier tier = null;
			if (tierTag != null) try {
				tier = MMOItems.plugin.getTiers().getOrThrow(tierTag.toString().toUpperCase().replace("-", "_"));
			} catch (IllegalArgumentException exception) {
				attribute.echoError(exception.getMessage());
			}

			// Find item level
			int itemLevel = level >= 0 ? level : (matchLevel && playerTag != null ? MMOItems.plugin.getTemplates()
					.rollLevel(PlayerData.get(((PlayerTag) playerTag).getPlayerEntity()).getRPG().getLevel()) : 1 + random.nextInt(100));

			// Find item tier
			ItemTier itemTier = tier != null ? tier : MMOItems.plugin.getTemplates().rollTier();

			// Build item
			return new ItemTag(object.getTemplate().newBuilder(itemLevel, itemTier).build().newBuilder().build());
		});
	}

	public static MMOItemTemplateTag valueOf(String string, TagContext context) {
		if (string == null) return null;

		try {
			String[] split = string.substring("mmoitem_template@".length()).split("\\.");
			String typeId = split[0];
			String itemId = split[1];

			Type type = MMOItems.plugin.getTypes().getOrThrow(typeId);
			MMOItems.plugin.getTemplates().getTemplateOrThrow(type, itemId);

			return new MMOItemTemplateTag(type, itemId);
		} catch (Exception exception) {
			return null;
		}
	}
}
