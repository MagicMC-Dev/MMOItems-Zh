package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.utils.Schedulers;
import io.lumine.mythic.utils.events.extra.ArmorEquipEvent;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.SoulboundInfo;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.AbilityUseEvent;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.util.InteractItem;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.inventory.EditableEquippedItem;
import net.Indyuce.mmoitems.api.player.inventory.EquippedPlayerItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerListener implements Listener {
    private final Map<Player, ArrayList<ItemStack>> deathItems = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void loadPlayerData(PlayerJoinEvent event) {
        MMOItems.plugin.getRecipes().refreshRecipeBook(event.getPlayer());
        PlayerData.load(event.getPlayer()); }

    @EventHandler(priority = EventPriority.HIGH)
    public void savePlayerData(PlayerQuitEvent event) { PlayerData.get(event.getPlayer()).save(); }


    /**
     * If the player dies, its time to roll the death-downgrade stat!
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeathForUpgradeLoss(PlayerDeathEvent event) {

        // No
        if (event instanceof Cancellable) { if (((Cancellable) event).isCancelled()) { return; } }

        // Get Player
        PlayerData data = PlayerData.get(event.getEntity());

        // Get total downgrade chance, anything less than zero is invalid
        double deathChance = data.getStats().getStat(ItemStats.DOWNGRADE_ON_DEATH_CHANCE);
        //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Current chance:\u00a7b " + deathChance);
        if (deathChance <= 0) { return; }

        List<EquippedPlayerItem> items = data.getInventory().getEquipped();
        ArrayList<EditableEquippedItem> equipped = new ArrayList<>();

        // Equipped Player Items yeah...
        for (EquippedPlayerItem playerItem : items) {

            // Null
            if (playerItem == null) { continue; }
            //DET//playerItem.getItem().hasData(ItemStats.NAME);
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Item:\u00a7b " + playerItem.getItem().getData(ItemStats.NAME));

            // Cannot perform operations of items that are uneditable
            if (!(playerItem.getEquipped() instanceof EditableEquippedItem)) {
                //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Not equippable. \u00a7cCancel");
                continue; }

            // Not downgradeable on death? Snooze
            if (!playerItem.getItem().hasData(ItemStats.DOWNGRADE_ON_DEATH)) {
                //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Not Downgradeable. \u00a7cCancel");
                continue; }

            // No upgrade template no snooze
            if(!playerItem.getItem().hasData(ItemStats.UPGRADE)) {
                //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Not Upgradeable. \u00a7cCancel");
                continue; }
            if (!playerItem.getItem().hasUpgradeTemplate()) {
                //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Null Template. \u00a7cCancel");
                continue; }

            // If it can be downgraded by one level...
            UpgradeData upgradeData = (UpgradeData) playerItem.getItem().getData(ItemStats.UPGRADE);
            if (upgradeData.getLevel() <= upgradeData.getMin()) {
                //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Too downgraded. \u00a7cCancel");
                continue; }

            // Okay explore stat
            equipped.add((EditableEquippedItem) playerItem.getEquipped());
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Yes. \u00a7aAccepted");
        }

        // Nothing to perform operations? Snooze
        if (equipped.size() == 0) {
            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 No items to downgrade. ");
            return; }
        Random random = new Random();

        // Degrade those items!
        while (deathChance >= 100 && equipped.size() > 0) {

            // Decrease
            deathChance -= 100;

            // Downgrade random item
            int d = random.nextInt(equipped.size());

            /*
             * The item was chosen, we must downgrade it by one level.
             */
            EditableEquippedItem equip = equipped.get(d);
            LiveMMOItem mmo = new LiveMMOItem(equip.getItem());
            mmo.getUpgradeTemplate().upgradeTo(mmo, mmo.getUpgradeLevel() - 1);

            // Build NBT
            ItemStack bakedItem = mmo.newBuilder().build();

            // Set durability to zero (full repair)
            DurabilityItem dur = new DurabilityItem(event.getEntity(), mmo.newBuilder().buildNBT());

            if (dur.getDurability() != dur.getMaxDurability()) {
                dur.addDurability(dur.getMaxDurability());
                bakedItem.setItemMeta(dur.toItem().getItemMeta());}

            // AH
            equip.setItem(bakedItem);
            equipped.remove(d);

            Message.DEATH_DOWNGRADING.format(ChatColor.RED, "#item#", SilentNumbers.getItemName(equip.getItem().getItem(), false))
                    .send(event.getEntity());

            //DET//MMOItems.log("\u00a78DETH \u00a7cDG\u00a77 Autodegrading\u00a7a " + mmo.getData(ItemStats.NAME));
        }

        // If there is chance, and there is size, and there is chance success
        if (deathChance > 0 && equipped.size() > 0 && random.nextInt(100) < deathChance) {

            // Downgrade random item
            int d = random.nextInt(equipped.size());

            /*
             * The item was chosen, we must downgrade it by one level.
             */
            EditableEquippedItem equip = equipped.get(d);
            LiveMMOItem mmo = new LiveMMOItem(equip.getItem());
            mmo.getUpgradeTemplate().upgradeTo(mmo, mmo.getUpgradeLevel() - 1);

            // Build NBT
            ItemStack bakedItem = mmo.newBuilder().build();

            // Set durability to zero (full repair)
            DurabilityItem dur = new DurabilityItem(event.getEntity(), mmo.newBuilder().buildNBT());

            if (dur.getDurability() != dur.getMaxDurability()) {
                dur.addDurability(dur.getMaxDurability());
                bakedItem.setItemMeta(dur.toItem().getItemMeta());}

            // AH
            equip.setItem(bakedItem);
            equipped.remove(d);

            Message.DEATH_DOWNGRADING.format(ChatColor.RED, "#item#", SilentNumbers.getItemName(equip.getItem().getItem(), false))
                    .send(event.getEntity());
        }
    }

    /**
     * Prevent players from dropping items which are bound to them with a
     * soulbound. Items are cached inside a map waiting for the player to
     * respawn. If he does not respawn the items are dropped on the ground, this
     * way there don't get lost
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeathForSoulbound(PlayerDeathEvent event) {
        if (event.getKeepInventory() || !MMOItems.plugin.getLanguage().keepSoulboundOnDeath)
            return;

        Player player = event.getEntity();
        SoulboundInfo soulboundInfo = new SoulboundInfo(player);

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            NBTItem nbt = NBTItem.get(item);

            if (nbt.hasTag("MMOITEMS_DISABLE_DEATH_DROP") && nbt.getBoolean("MMOITEMS_DISABLE_DEATH_DROP")) {
                iterator.remove();
                if (!deathItems.containsKey(player))
                    deathItems.put(player, new ArrayList<>());

                deathItems.get(player).add(item);
            }

            /*
             * not a perfect check but it's very sufficient and so we avoid
             * using a JsonParser followed by map checkups in the SoulboundData
             * constructor
             */
            else if (nbt.hasTag("MMOITEMS_SOULBOUND") && nbt.getString("MMOITEMS_SOULBOUND").contains(player.getUniqueId().toString())) {
                iterator.remove();
                soulboundInfo.add(item);
            }
        }

        if (soulboundInfo.hasItems())
            soulboundInfo.setup();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (MMOItems.plugin.getLanguage().keepSoulboundOnDeath)
            SoulboundInfo.read(player);

        if (deathItems.containsKey(player)) {
            Schedulers.sync().runLater(() -> {
                player.getInventory().addItem(deathItems.get(player).toArray(new ItemStack[0]));
                deathItems.remove(player);
            }, 10);
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        NBTItem item = NBTItem.get(event.getNewArmorPiece());
        if (!PlayerData.get(player).getRPG().canUse(item, true))
            event.setCancelled(true);
    }

    /**
     * This handler listens to ALL trident shootings, including both
     * custom tridents from MMOItems AND vanilla tridents, since MMOItems
     * needs to apply on-hit effects like crits, elemental damage... even
     * if the player is using a vanilla trident.
     * <p>
     * Fixing commit 6cf6f741
     */
    @EventHandler(ignoreCancelled = true)
    public void registerTridents(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident) || !(event.getEntity().getShooter() instanceof Player))
            return;

        InteractItem item = new InteractItem((Player) event.getEntity().getShooter(), Material.TRIDENT);
        if (!item.hasItem())
            return;

        NBTItem nbtItem = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item.getItem());
        Type type = Type.get(nbtItem.getType());
        PlayerData playerData = PlayerData.get((Player) event.getEntity().getShooter());

        if (type != null) {
            Weapon weapon = new Weapon(playerData, nbtItem);
            if (!weapon.checkItemRequirements() || !weapon.checkAndApplyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }
        }

        MMOItems.plugin.getEntities().registerCustomProjectile(nbtItem, playerData.getStats().newTemporary(EquipmentSlot.fromBukkit(item.getSlot())), event.getEntity(), type != null, 1);
    }

    /**
     * Fixes an issue where quickly swapping items in hand just
     * does not update the player's inventory which can make the
     * player cast abilities or attacks with not the correct stats
     *
     * @deprecated This does cost some performance and that update
     *         method NEEDS some improvement in the future
     */
    @Deprecated
    @EventHandler
    public void registerInventoryUpdates1(PlayerSwapHandItemsEvent event) {
        PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    /**
     * Fixes an issue where quickly swapping items in hand just
     * does not update the player's inventory which can make the
     * player cast abilities or attacks with not the correct stats
     *
     * @deprecated This does cost some performance and that update
     *         method NEEDS some improvement in the future
     */
    @Deprecated
    @EventHandler
    public void registerInventoryUpdates2(PlayerItemHeldEvent event) {
        PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
    }

    @Deprecated
    @EventHandler
    public void registerOldEvent(PlayerCastSkillEvent event) {

        // Find caster
        PlayerData playerData = PlayerData.get(event.getPlayer().getUniqueId());

        // Create registered skill
        RegisteredSkill registeredSkill = new RegisteredSkill(event.getCast().getHandler(), event.getCast().getHandler().getId());
        for (String mod : event.getCast().getHandler().getModifiers()) {
            registeredSkill.setDefaultValue(mod, event.getMetadata().getModifier(mod));
            registeredSkill.setName(mod, MMOUtils.caseOnWords(mod.toLowerCase().replace("-", " ").replace("_", " ")));
        }

        // Create ability data
        AbilityData abilityData = new AbilityData(registeredSkill, TriggerType.CAST);
        for (String mod : event.getCast().getHandler().getModifiers())
            abilityData.setModifier(mod, event.getMetadata().getModifier(mod));

        // Find ability target
        LivingEntity target = event.getMetadata().hasTargetEntity() && event.getMetadata().getTargetEntityOrNull() instanceof LivingEntity ?
                (LivingEntity) event.getMetadata().getTargetEntityOrNull() : null;

        // Call event for compatibility
        Bukkit.getPluginManager().callEvent(new AbilityUseEvent(playerData, abilityData, target));
    }
}
