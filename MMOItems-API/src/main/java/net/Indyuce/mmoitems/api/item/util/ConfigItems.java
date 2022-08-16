package net.Indyuce.mmoitems.api.item.util;

import net.Indyuce.mmoitems.api.item.util.crafting.CraftingRecipeDisplay;
import net.Indyuce.mmoitems.api.item.util.crafting.QueueItemDisplay;
import net.Indyuce.mmoitems.api.item.util.crafting.UpgradingRecipeDisplay;
import io.lumine.mythic.lib.version.VersionMaterial;

public class ConfigItems {
	public static final ConfigItem CONFIRM = new ConfigItem("CONFIRM", VersionMaterial.GREEN_STAINED_GLASS_PANE.toMaterial(), "&aConfirm");
	public static final ConfigItem FILL = new ConfigItem("FILL", VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial(), "&8");
	public static final CustomSkull PREVIOUS_PAGE = new CustomSkull("PREVIOUS_PAGE",
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
			"&aPrevious Page");
	public static final CustomSkull NEXT_PAGE = new CustomSkull("NEXT_PAGE",
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19",
			"&aNext Page");
	public static final CustomSkull PREVIOUS_IN_QUEUE = new CustomSkull("PREVIOUS_IN_QUEUE",
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
			"&aPrevious");
	public static final CustomSkull NEXT_IN_QUEUE = new CustomSkull("NEXT_IN_QUEUE",
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19",
			"&aNext");
	public static final CustomSkull BACK = new CustomSkull("BACK",
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
			"&aBack");
	public static final CraftingRecipeDisplay CRAFTING_RECIPE_DISPLAY = new CraftingRecipeDisplay();
	public static final UpgradingRecipeDisplay UPGRADING_RECIPE_DISPLAY = new UpgradingRecipeDisplay();
	public static final QueueItemDisplay QUEUE_ITEM_DISPLAY = new QueueItemDisplay();

	public static final ConfigItem[] values = { ConfigItems.CONFIRM, ConfigItems.FILL, ConfigItems.PREVIOUS_PAGE,
			ConfigItems.NEXT_PAGE, ConfigItems.PREVIOUS_IN_QUEUE, ConfigItems.NEXT_IN_QUEUE, ConfigItems.BACK,
			ConfigItems.CRAFTING_RECIPE_DISPLAY, ConfigItems.UPGRADING_RECIPE_DISPLAY, ConfigItems.QUEUE_ITEM_DISPLAY };
}
