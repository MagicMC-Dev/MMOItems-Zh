package net.Indyuce.mmoitems.comp.mythicmobs;

import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitItemStack;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IMultiDrop;
import io.lumine.xikage.mythicmobs.drops.LootBag;
import io.lumine.xikage.mythicmobs.drops.droppables.ItemDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.droptable.item.DropItem;
import net.Indyuce.mmoitems.api.droptable.item.MMOItemDropItem;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class MythicMobsHook implements Listener {

	/*
	 * has been moved over to MythicMobs because MMOItems needs access to MythicMobs
	 * abilities and therefore must be enabled after MythicMobs
	 */

	/*
	 * MUST NOT BE REMOVED - this class must be kept as vitam aeternam in case we
	 * need to change something in MM compatibility and sent it back to MM devs
	 */
	public MythicMobsHook() {
		MythicMobs.inst().getPlaceholderManager().register("mmoitems.skill",
				Placeholder.meta((metadata, arg) -> String.valueOf(PlayerData
						.get(metadata.getCaster().getEntity().getUniqueId()).getAbilityData().getCachedModifier(arg))));
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
//		MMOItems.plugin.getCrafting().registerIngredient("mythicitem", config -> new MythicItemIngredient(config),
//				new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
//				nbt -> nbt.hasTag("MYTHIC_TYPE"), nbt -> nbt.getString("MYTHIC_TYPE").toLowerCase());
	}

	/*
	 * registers custom drop types
	 */
	@EventHandler
	public void a(MythicDropLoadEvent event) {
		if (event.getDropName().equalsIgnoreCase("mmoitems") || event.getDropName().equalsIgnoreCase("mmoitem"))
			event.register(new MMOItemsDrop(event.getConfig()));
	}

	/*
	 * register placeholders when MM is reloaded. the skill placeholder let players
	 * retrieve cached ability values.
	 */
	@EventHandler
	public void b(MythicReloadedEvent event) {
		MythicMobs.inst().getPlaceholderManager().register("mmoitems.skill",
				Placeholder.meta((metadata, arg) -> String.valueOf(PlayerData
						.get(metadata.getCaster().getEntity().getUniqueId()).getAbilityData().getCachedModifier(arg))));
	}

	public static class MMOItemsDrop extends Drop implements IMultiDrop {
		private DropItem dropItem;

		public MMOItemsDrop(MythicLineConfig config) {
			super(config.getLine(), config);

			/*
			 * TODO move try-catch to the MythicDropLoadEvent method and make the dropItem
			 * field final
			 */
			try {
				String typeFormat = config.getString("type").toUpperCase().replace("-", "_");
				Validate.isTrue(MMOItems.plugin.getTypes().has(typeFormat),
						"Could not find type with ID " + typeFormat);

				Type type = MMOItems.plugin.getTypes().get(typeFormat);
				String id = config.getString("id");

				Validate.notNull(id, "MMOItems ID cannot be null");

				dropItem = new MMOItemDropItem(type, id, 1, config.getDouble("unidentified", 0), 1, 1);

			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load drop item: " + exception.getMessage());
			}
		}

		/*
		 * TODO remove null check with extra method from MythicDropLoadEvent
		 */
		@SuppressWarnings("deprecation")
		@Override
		public LootBag get(DropMetadata metadata) {
			LootBag loot = new LootBag(metadata);
			if (dropItem != null)
				// TODO fix null
				loot.add(new ItemDrop(this.getLine(), (MythicLineConfig) this.getConfig(),
						new BukkitItemStack(dropItem.getItem(null, 1))));
			return loot;
		}
	}
}