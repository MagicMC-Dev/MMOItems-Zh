package net.Indyuce.mmoitems.comp.mythicmobs;

import java.util.logging.Level;

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
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.drop.DropItem;

public class MythicMobsHook implements Listener {
	public MythicMobsHook() {
		MythicMobs.inst().getPlaceholderManager().register("mmoitems.skill", Placeholder.meta((metadata, arg) -> String.valueOf(PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getSkillData().getCachedModifier(arg))));
		MMOItems.plugin.getDamage().registerHandler(new MythicMobsDamageHandler());
	}

	@EventHandler
	public void a(MythicDropLoadEvent event) {
		if (event.getDropName().equalsIgnoreCase("mmoitems") || event.getDropName().equalsIgnoreCase("mmoitem"))
			event.register(new MMOItemsDrop(event.getConfig()));
	}

	/*
	 * register placeholders when MM is reloaded.
	 */
	@EventHandler
	public void b(MythicReloadedEvent event) {
		new MythicMobsHook();
	}

	public class MMOItemsDrop extends Drop implements IMultiDrop {
		private Type type;
		private String id;
		private double unidentification = 0;

		public MMOItemsDrop(MythicLineConfig config) {
			super(config.getLine(), config);

			try {
				type = MMOItems.plugin.getTypes().get(config.getString("type").toUpperCase().replace("-", "_"));
			} catch (Exception e) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Wrong type name in a MM drop table at: " + config.getString("type"));
				return;
			}

			id = config.getString("id");
			unidentification = config.getDouble("unidentified", 0);
		}

		@SuppressWarnings("deprecation")
		@Override
		public LootBag get(DropMetadata metadata) {
			LootBag loot = new LootBag(metadata);
			loot.add(new ItemDrop(this.getLine(), (MythicLineConfig) this.getConfig(), new BukkitItemStack(new DropItem(type, id, unidentification).getItem((int) getAmount()))));
			return loot;
		}
	}
}