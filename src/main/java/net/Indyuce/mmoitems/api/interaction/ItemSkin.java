package net.Indyuce.mmoitems.api.interaction;

import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.Skull_Texture.SkullTextureData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ItemSkin extends UseItem {
	public ItemSkin(Player player, NBTItem item, Type type) {
		super(player, item, type);
	}

	public ApplyResult applyOntoItem(NBTItem target, Type targetType) {
		if(targetType == Type.SKIN)
			return new ApplyResult(ResultType.NONE);
		
		if(MMOItems.plugin.getConfig().getBoolean("locked-skins") && target.getBoolean("MMOITEMS_HAS_SKIN")) {
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
			Message.SKIN_REJECTED.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
			return new ApplyResult(ResultType.NONE);
		}

		boolean compatible = false;
		if(getMMOItem().hasData(ItemStat.COMPATIBLE_TYPES)) {
			for(String type : ((StringListData) getMMOItem().getData(ItemStat.COMPATIBLE_TYPES)).getList()) {
				if(type.equalsIgnoreCase(targetType.getId())) {
					compatible = true;
					break;	
				}
			}
			
			if(!compatible){
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				Message.SKIN_INCOMPATIBLE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
				return new ApplyResult(ResultType.NONE);
			}
		}
		
		// check for success rate
		double successRate = getNBTItem().getStat(ItemStat.SUCCESS_RATE);
		if (successRate != 0)
			if (random.nextDouble() < 1 - successRate / 100) {
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
				Message.SKIN_BROKE.format(ChatColor.RED, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);
				return new ApplyResult(ResultType.FAILURE);
			}
		
		// Apply skin
		target.addTag(new ItemTag("MMOITEMS_HAS_SKIN", true));
		if(getNBTItem().getInteger("CustomModelData") != 0) target.addTag(new ItemTag("CustomModelData", getNBTItem().getInteger("CustomModelData")));
		if(!getNBTItem().getString("MMOITEMS_ITEM_PARTICLES").isEmpty()) target.addTag(new ItemTag("MMOITEMS_ITEM_PARTICLES", getNBTItem().getString("MMOITEMS_ITEM_PARTICLES")));

		ItemStack item = target.toItem();
		if(item.getType() != getNBTItem().getItem().getType())
			item.setType(getNBTItem().getItem().getType());

		ItemMeta meta = item.getItemMeta();
		if (getMMOItem().hasData(ItemStat.SKULL_TEXTURE) && item.getType() == VersionMaterial.PLAYER_HEAD.toMaterial() &&
			getNBTItem().getItem().getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
			try {
				Field profileField = meta.getClass().getDeclaredField("profile"); profileField.setAccessible(true);
				profileField.set(meta, ((SkullTextureData) getMMOItem().getData(ItemStat.SKULL_TEXTURE)).getGameProfile());
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				MMOItems.plugin.getLogger().warning("Could not read skull texture");
			}
		}
		item.setItemMeta(meta);
		
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		Message.SKIN_APPLIED.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(target.getItem())).send(player);

		return new ApplyResult(item);
	}

	public class ApplyResult {
		private ResultType type;
		private ItemStack result;

		public ApplyResult(ResultType type) {
			this(null, type);
		}

		public ApplyResult(ItemStack result) {
			this(result, ResultType.SUCCESS);
		}

		public ApplyResult(ItemStack result, ResultType type) {
			this.type = type;
			this.result = result;
		}

		public ResultType getType() {
			return type;
		}

		public ItemStack getResult() {
			return result;
		}
	}

	public enum ResultType {
		FAILURE,
		NONE,
		SUCCESS;
	}
}
