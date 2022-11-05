package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TierManager implements Reloadable{
	private final Map<String, ItemTier> tiers = new HashMap<>();

	public TierManager() {
		reload();
	}

	public void reload() {
		tiers.clear();

		// For logging
		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
		ffp.activatePrefix(true, "Tiers");

		ConfigFile config = new ConfigFile("item-tiers");
		for (String tierName : config.getConfig().getKeys(false)) {

			// Get section (Using RecipeMakerGUI for @NotNull attribute)
			ConfigurationSection tierSection = RecipeMakerGUI.getSection(config.getConfig(), tierName);

			// Attempt to register
			try {
				register(new ItemTier(tierSection));

			// Any errors?
			} catch (IllegalArgumentException exception) {

				// Log error
				ffp.log(FriendlyFeedbackCategory.ERROR, "Cannot register tier '$u{0}$b';$f {1}", tierName, exception.getMessage());
			}
		}

		// Log relevant messages
		ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
		ffp.sendTo(FriendlyFeedbackCategory.FAILURE, MMOItems.getConsole());
	}

	/**
	 * Set a tier live to be used everywhere in the plugin.
	 *
	 * @param tier Tier to register
	 */
	public void register(@NotNull ItemTier tier) { tiers.put(tier.getId(), tier); }

	/**
	 * @param id Tier name
	 *
	 * @return If a tier of this name is loaded
	 */
	public boolean has(@Nullable String id) {

		/*
		 * No null tiers, but for flexibility of use, just
		 * make it @Nullable and make it return false always.
		 */
		if (id == null) { return false; }

		// Is the tier loaded?
		return tiers.containsKey(id);
	}

	/**
	 *
	 * @param id Tier name, technically Nullable but this guarantees
	 *           that an IllegalArgumentException will be thrown.
	 *
	 * @throws IllegalArgumentException When there is no tier of such name loaded.
	 *
	 * @return Tier of this name.
	 */
	@NotNull public ItemTier getOrThrow(@Nullable String id) throws IllegalArgumentException {

		// Well, is it loaded?
		Validate.isTrue(tiers.containsKey(id), FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Could not find tier with ID '$r{0}$b'", id));

		// Well its guaranteed to not be null.
		return tiers.get(id);
	}

	/**
	 * Will get the tier of this name, if there is one
	 *
	 * @param id Name of the tier.
	 */
	@Nullable public ItemTier get(@Nullable String id) {
		if (id == null) { return null; }
		return tiers.get(id);
	}

	/**
	 * @return An iterable of all the tiers loaded
	 */
	@NotNull public Collection<ItemTier> getAll() {
		return tiers.values();
	}

	/**
	 * @param item Item you seek the tier of
	 * @return The tier of this item, it it has any
	 * @deprecated Use {@link MMOItem#getTier()}
	 */
	@Nullable
	@Deprecated
	public ItemTier findTier(@NotNull MMOItem item) {
		return item.getTier();
	}
}
