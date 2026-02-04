package fr.siroz.cariboustonks.core.skyblock.data.hypixel;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.mod.ModDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.ElectionResult;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.fetcher.BazaarFetcher;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.fetcher.ElectionFetcher;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.fetcher.ItemsFetcher;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Class responsible for managing data from the official Hypixel SkyBlock API.
 * <h3>Fetchers:</h3>
 * <li>
 * {@code Items}: Retrieves SkyBlock Items from the API resource.
 * Processed and mapped via internal mapping in the Mod to convert Hypixel materials
 * into Minecraft materials from the current version.
 * </li>
 * <li>
 * {@code Bazaar}: Retrieves SkyBlock Products from the Bazaar Endpoint.
 * Retrieve in raw form, the products are processed and pre-calculated
 * to get statistics and other data at each fetch cycle.
 * </li>
 * <li>
 * {@code Election}: Retrieves SkyBlock Election results from the API resource.
 * Retrieves the results of the current election, for the current mayor,
 * his perks, as well as the minister and his optional perk.
 * </li>
 * <p>
 * The data is retrieved with the {@link ClientLifecycleEvents#CLIENT_STARTED}
 * once or repeatedly according to a given time, failure cases are handled as retries for single fetches.
 *
 * @see ItemsFetcher
 * @see BazaarFetcher
 * @see ElectionFetcher
 */
public final class HypixelDataSource {

	private final HypixelAPIFixer apiFixer = new HypixelAPIFixer();
	private final ModDataSource modDataSource;

	private final ItemsFetcher itemsFetcher;
	private final BazaarFetcher bazaarFetcher;
	private final ElectionFetcher electionFetcher;

	private boolean hasCalledFixMissing = false;

	@ApiStatus.Internal
	public HypixelDataSource() {
		this.modDataSource = CaribouStonks.mod().getModDataSource();
		// Fetchers
		this.itemsFetcher = new ItemsFetcher(this, modDataSource, apiFixer);
		this.bazaarFetcher = new BazaarFetcher(this, 5, () -> ConfigManager.getConfig().general.internal.fetchBazaarData);
		this.electionFetcher = new ElectionFetcher();
		// Event listener
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> onClientStarted());
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STARTED")
	private void onClientStarted() {
		itemsFetcher.start();
		bazaarFetcher.start();
		electionFetcher.start();
	}

	/**
	 * Returns {@link ElectionResult} of the current election.
	 * May be null if the fetcher was unable to retrieve or process the data.
	 *
	 * @return the {@link ElectionResult} or {@code null}
	 */
	@Nullable
	public ElectionResult getElection() {
		return electionFetcher.getCachedElection();
	}

	/**
	 * Check if the Bazaar fetch is in progress
	 *
	 * @return {@code true} if the fetch is in progress
	 */
	public boolean isBazaarInUpdate() {
		return bazaarFetcher.isFetching();
	}

	/**
	 * Check if the given item is present in the Products of the Bazaar.
	 *
	 * @param skyBlockItemId the ID of the SkyBlock item to check for.
	 * @return {@code true} if the item is present in the Bazaar Products.
	 */
	public boolean hasBazaarItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isBlank()) {
			return false;
		}
		return bazaarFetcher.getBazaarSnapshot().containsKey(skyBlockItemId);
	}

	/**
	 * Returns an Optional of {@link BazaarProduct} according to the given item.
	 *
	 * @param skyBlockItemId the ID of the SkyBlock item to check for
	 * @return an Optional of {@link BazaarProduct}
	 */
	public Optional<BazaarProduct> getBazaarItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(bazaarFetcher.getBazaarSnapshot().get(skyBlockItemId));
	}

	/**
	 * Returns an {@link ItemStack} that corresponds to the ID of the specified SkyBlock item.
	 * <p>
	 * This correspondence depends on whether the SkyBlock item is stored internally, with a “material” returned
	 * by Hypixel's SkyBlock API, and is mapped with an ID from {@link net.minecraft.core.registries.BuiltInRegistries#ITEM}.
	 * <p>
	 * If the specified item ID is not recognized in the list of registered SkyBlock items,
	 * <b>OR</b> if it is impossible to retrieve the list of SkyBlock items / retrieve the mapping according to
	 * the version, {@link Items#BARRIER} will be returned.
	 * <p>
	 * If the item is a “SKULL_ITEM” (Hypixel Material) and a skin is found,
	 * a {@link Items#PLAYER_HEAD} is returned with the texture
	 * <p>
	 * In Hypixel's SkyBlock API, the items returned have a {@code material} that is a string,
	 * which is based <b>arbitrarily by Hypixel</b> and according to version <b>1.8</b>.
	 * To do this, a mapping is performed upstream that defines {@code “material”: ID}.
	 * For example: {@code “SKULL_ITEM”: 1156}.
	 * “SKULL_ITEM” comes from Hypixel, and “1156” is the ID of {@link net.minecraft.core.registries.BuiltInRegistries#ITEM}.
	 *
	 * @param skyBlockItemId the ID of the SkyBlock item to check for
	 * @return an {@link ItemStack} corresponding to the ID of the specified SkyBlock item.
	 */
	public @NotNull ItemStack getItemStack(@NotNull String skyBlockItemId) {
		ItemStack fallback = new ItemStack(Items.BARRIER, 1);
		fallback.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty(skyBlockItemId));

		if (modDataSource.isItemsMappingError()) {
			return fallback;
		}

		try {
			SkyBlockItemData skyBlockItem = itemsFetcher.getSkyBlockItemsSnapshot().get(skyBlockItemId);
			if (skyBlockItem == null) {
				return fallback;
			}

			String hypixelMaterial = skyBlockItem.material();
			String minecraftId = modDataSource.getMinecraftId(hypixelMaterial);
			if (minecraftId == null || minecraftId.equals("NO_MATCH")) {
				return fallback;
			}

			ItemStack itemStack = new ItemStack(Items.BARRIER, 1);

			Optional<Item> item = ItemUtils.getItemById(minecraftId);
			if (item.isPresent()) {
				itemStack = new ItemStack(item.get(), 1);
			}

			if (skyBlockItem.skullTexture().isPresent()) {
				itemStack = ItemUtils.createSkull(skyBlockItem.skullTexture().get());
			}

			itemStack.set(DataComponents.CUSTOM_NAME, Component.nullToEmpty(skyBlockItemId));

			return itemStack;
		} catch (Throwable ex) {
			CaribouStonks.LOGGER.warn("[HypixelDataSource] Unable to create ItemStack for {}", skyBlockItemId, ex);
		}

		return fallback;
	}

	/**
	 * Returns an {@link SkyBlockItemData} that corresponds to the ID of the specified SkyBlock item.
	 *
	 * @param skyBlockItemId the ID of the SkyBlock item to check for
	 * @return the {@link SkyBlockItemData} or null
	 * @see #getSkyBlockItemOptional(String)
	 */
	public @Nullable SkyBlockItemData getSkyBlockItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isEmpty()) {
			return null;
		}
		return itemsFetcher.getSkyBlockItemsSnapshot().get(skyBlockItemId);
	}

	/**
	 * Returns an Optional of {@link SkyBlockItemData} that corresponds to the ID of the specified SkyBlock item.
	 *
	 * @param skyBlockItemId the ID of the SkyBlock item to check for
	 * @return an Optional of {@link SkyBlockItemData}
	 * @see #getSkyBlockItem(String)
	 */
	public Optional<SkyBlockItemData> getSkyBlockItemOptional(@Nullable String skyBlockItemId) {
		SkyBlockItemData item = getSkyBlockItem(skyBlockItemId);
		return item == null ? Optional.empty() : Optional.of(item);
	}

	/**
	 * Retrieves the complete list of SkyBlock items registered from the Hypixel API.
	 * Performs checks for internal errors related to item mapping or data retrieval from the endpoint.
	 * If such problems are detected, or if the internal list of SkyBlock items is empty,
	 * a {@link HypixelDataException} exception is thrown with an appropriate error message.
	 *
	 * @return a list of SkyBlock items.
	 * @throws HypixelDataException If there is an error in the item mapping,
	 *                              data retrieval from the API,
	 *                              or if the SkyBlock item list is empty
	 */
	@NotNull
	@Unmodifiable
	public List<SkyBlockItemData> getSkyBlockItems() throws HypixelDataException {
		if (modDataSource.isItemsMappingError()) {
			throw new HypixelDataException(Component.nullToEmpty("Unable to map SkyBlock Items into Minecraft."));
		}

		if (!itemsFetcher.isLastFetchSuccessful()) {
			throw new HypixelDataException(Component.nullToEmpty("Unable to fetch SkyBlock Items from Hypixel API."));
		}

		if (itemsFetcher.getSkyBlockItemsSnapshot().isEmpty()) {
			throw new HypixelDataException(Component.nullToEmpty("No SkyBlock Items is registered."));
		}

		return new ArrayList<>(itemsFetcher.getSkyBlockItemsSnapshot().values());
	}

	@Contract(" -> new")
	public @NotNull Set<String> getSkyBlockItemsIds() {
		return new HashSet<>(itemsFetcher.getSkyBlockItemsSnapshot().keySet());
	}

	/**
	 * Returns the total number of SkyBlock items currently registered.
	 *
	 * @return the total number of SkyBlock items
	 */
	public int getSkyBlockItemCounts() {
		return itemsFetcher.getSkyBlockItemsSnapshot().size();
	}

	@ApiStatus.Internal
	public void fixSkyBlockItems() {
		if (itemsFetcher.isLastFetchSuccessful() && bazaarFetcher.isFirstBazaarUpdated() && !hasCalledFixMissing) {
			hasCalledFixMissing = true;

			int fixedEnchants = 0;
			int fixedEssences = 0;
			int fixedShards = 0;

			for (String bazaarProductId : bazaarFetcher.getBazaarSnapshot().keySet()) {
				if (itemsFetcher.getSkyBlockItemsSnapshot().containsKey(bazaarProductId)) {
					continue;
				}
				try {
					if (apiFixer.isEnchantment(bazaarProductId)) {
						itemsFetcher.putItem(bazaarProductId, apiFixer.createEnchant(bazaarProductId));
						fixedEnchants++;

					} else if (apiFixer.isEssence(bazaarProductId)) {
						itemsFetcher.putItem(bazaarProductId, apiFixer.createEssence(bazaarProductId));
						fixedEssences++;

					} else if (apiFixer.isShard(bazaarProductId)) {
						SkyBlockItemData shard = apiFixer.createShard(bazaarProductId);
						if (shard != null) {
							itemsFetcher.putItem(bazaarProductId, shard);
							fixedShards++;
						} else {
							CaribouStonks.LOGGER.warn("[HypixelDataSource] Unable to create {} Shard! Not registered in ModDataSource.",
									bazaarProductId);
						}
					} else {
						CaribouStonks.LOGGER.warn("[HypixelDataSource] Unable to fix {}. Not identified!",
								bazaarProductId);
					}
				} catch (Throwable ex) {
					CaribouStonks.LOGGER.error("[HypixelDataSource] Fix for {} failed", bazaarProductId, ex);
				}
			}

			CaribouStonks.LOGGER.info("[HypixelDataSource] Fixed {} enchants, {} essences and {} Shards from Bazaar to SkyBlock Items",
					fixedEnchants, fixedEssences, fixedShards);
		}
	}
}
