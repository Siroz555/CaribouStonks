package fr.siroz.cariboustonks.core.data.hypixel;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.core.data.hypixel.election.ElectionResult;
import fr.siroz.cariboustonks.core.data.hypixel.fetcher.BazaarFetcher;
import fr.siroz.cariboustonks.core.data.hypixel.fetcher.ElectionFetcher;
import fr.siroz.cariboustonks.core.data.hypixel.fetcher.ItemsFetcher;
import fr.siroz.cariboustonks.core.data.hypixel.item.SkyBlockItem;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Cette class est responsable de la gestion des données des items provenant de l'API SkyBlock d'Hypixel,
 * de "mapper" les SkyBlock items, en fonction des items ID Minecraft ({@link net.minecraft.registry.Registries#ITEM}).
 * Charge et enregistre les données des items SkyBlock et fait un mapping pendant
 * {@link ClientLifecycleEvents#CLIENT_STARTED} et donne accès à différentes méthodes pour retourner les données.
 */
public final class HypixelDataSource {

	private final HypixelAPIFixer apiFixer = new HypixelAPIFixer();
	private final ModDataSource modDataSource;

	private final ItemsFetcher itemsFetcher;
	private final BazaarFetcher bazaarFetcher;
	private final ElectionFetcher electionFetcher;

	private boolean hasCalledFixMissing = false;

	@ApiStatus.Internal
	public HypixelDataSource(@NotNull ModDataSource modDataSource) {
		this.modDataSource = modDataSource;
		// Fetchers
		this.itemsFetcher = new ItemsFetcher(this, modDataSource, apiFixer);
		this.bazaarFetcher = new BazaarFetcher(this, 5, () -> ConfigManager.getConfig().general.stonks.bazaarTooltipPrice);
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

	@Nullable
	public ElectionResult getElection() {
		return electionFetcher.getCachedElection();
	}

	public boolean isBazaarInUpdate() {
		return bazaarFetcher.isFetching();
	}

	public boolean hasBazaarItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isBlank()) {
			return false;
		}

		return bazaarFetcher.getBazaarSnapshot().containsKey(skyBlockItemId);
	}

	public Optional<BazaarProduct> getBazaarItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isEmpty()) return Optional.empty();
		return Optional.ofNullable(bazaarFetcher.getBazaarSnapshot().get(skyBlockItemId));
	}

	/**
	 * Retourne un {@link ItemStack} qui correspond à l'ID de l'item SkyBlock spécifié.
	 * <p>
	 * Cette correspondance dépend si l'item SkyBlock est enregistré en interne, avec un "material" retourné
	 * par l'API SkyBlock d'Hypixel et est mappé avec un ID du {@link net.minecraft.registry.Registries#ITEM}.
	 * <p>
	 * Si l'ID de l'item spécifié n'est pas reconnu dans la liste des items SkyBlock enregistrés,
	 * <b>OU</b> qu'il est impossible de récupérer la liste des items SkyBlock / récupérer le mapping selon
	 * la version, {@link Items#BARRIER} sera retourné.
	 * <p>
	 * Dans l'API SkyBlock d'Hypixel, les items retournés ont un "material" qui est une chaine de caractères,
	 * qui est basé de facon <b>arbitraire par Hypixel</b> et selon la version <b>1.8</b>. Pour ce faire,
	 * un mapping est réalisé en amont qui défini {@code "material": ID}. Par exemple : {@code "SKULL_ITEM": 1156}.
	 * "SKULL_ITEM" provenant d'Hypixel, et "1156" étant l'ID du {@link net.minecraft.registry.Registries#ITEM}.
	 * <p>
	 * Si l'item est un "SKULL_ITEM" (Hypixel Material) et qu'un skin est trouvé, un {@link Items#PLAYER_HEAD}
	 * est retourné avec la texture.
	 *
	 * @param skyBlockItemId l'ID du SkyBlock item (par exemple : "RECOMBOBULATOR_3000")
	 * @return un nouveau {@link ItemStack} de l'item SkyBlock
	 */
	public @NotNull ItemStack getItemStack(@NotNull String skyBlockItemId) {
		ItemStack fallback = new ItemStack(Items.BARRIER, 1);
		fallback.set(DataComponentTypes.CUSTOM_NAME, Text.of(skyBlockItemId));

		if (modDataSource.isItemsMappingError()) {
			return fallback;
		}

		try {
			SkyBlockItem skyBlockItem = itemsFetcher.getSkyBlockItemsSnapshot().get(skyBlockItemId);
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

			if (skyBlockItem.skullTexture() != null) {
				itemStack = ItemUtils.createSkull(skyBlockItem.skullTexture());
			}

			itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.of(skyBlockItemId));

			return itemStack;
		} catch (Throwable ex) {
			CaribouStonks.LOGGER.warn("[HypixelDataSource] Unable to create ItemStack for {}", skyBlockItemId, ex);
		}

		return fallback;
	}

	/**
	 * Retourne un {@link SkyBlockItem} selon le {@code skyBlockItemId} spécifié.
	 *
	 * @param skyBlockItemId l'ID de l'item SkyBlock
	 * @return le SkyBlock item trouvé ou {@code null} si aucune correspondance
	 * @see #getSkyBlockItemOptional(String)
	 */
	public @Nullable SkyBlockItem getSkyBlockItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isEmpty()) return null;
		return itemsFetcher.getSkyBlockItemsSnapshot().get(skyBlockItemId);
	}

	/**
	 * Retourne un {@link Optional} du {@link SkyBlockItem} selon le {@code skyBlockItemId} spécifié.
	 *
	 * @param skyBlockItemId l'ID de l'item SkyBlock
	 * @return un Optional du SkyBlock item
	 * @see #getSkyBlockItem(String)
	 */
	public Optional<SkyBlockItem> getSkyBlockItemOptional(@Nullable String skyBlockItemId) {
		SkyBlockItem item = getSkyBlockItem(skyBlockItemId);
		return item == null ? Optional.empty() : Optional.of(item);
	}

	/**
	 * Récupère la liste complète des items SkyBlock enregistrés depuis l'API Hypixel.
	 * Cette méthode effectue des vérifications des erreurs internes liées au mapping des items
	 * ou à la récupération des données depuis l'API SkyBlock d'Hypixel.
	 * Si de tels problèmes sont détectés, ou si la liste interne des items SkyBlock est vide,
	 * une exception {@link HypixelDataException} est levée avec un message d'erreur approprié.
	 * Sinon, elle retourne une nouvelle liste {@link List} contenant tous les items SkyBlock
	 * actuellement enregistrés.
	 *
	 * @return une liste des SkyBlock items.
	 * @throws HypixelDataException S'il y a une erreur dans le mapping des items,
	 *                              la récupération des données depuis l'API
	 *                              ou si la liste des items SkyBlock est vide.
	 */
	@NotNull
	@Unmodifiable
	public List<SkyBlockItem> getSkyBlockItems() throws HypixelDataException {
		if (modDataSource.isItemsMappingError()) {
			throw new HypixelDataException(Text.of("Unable to map SkyBlock Items into Minecraft."));
		}

		if (!itemsFetcher.isLastFetchSuccessful()) {
			throw new HypixelDataException(Text.of("Unable to fetch SkyBlock Items from Hypixel API."));
		}

		if (itemsFetcher.getSkyBlockItemsSnapshot().isEmpty()) {
			throw new HypixelDataException(Text.of("No SkyBlock Items is registered."));
		}

		return new ArrayList<>(itemsFetcher.getSkyBlockItemsSnapshot().values());
	}

	@Contract(" -> new")
	public @NotNull Set<String> getSkyBlockItemsIds() {
		return new HashSet<>(itemsFetcher.getSkyBlockItemsSnapshot().keySet());
	}

	/**
	 * Retourne le nombre total des items SkyBlock actuellement enregistrés.
	 *
	 * @return le nombre total d'items SkyBlock
	 */
	public int getSkyBlockItemCounts() {
		return itemsFetcher.getSkyBlockItemsSnapshot().size();
	}

	@ApiStatus.Internal
	public void fixSkyBlockItems() {
		if (itemsFetcher.isLastFetchSuccessful() && bazaarFetcher.isFirstBazaarUpdated() && !hasCalledFixMissing) {
			hasCalledFixMissing = true;
			CaribouStonks.LOGGER.info("[HypixelDataSource] Fixing Hypixel SkyBlock Items..");

			int fixedEnchants = 0;
			int fixedEssences = 0;
			int fixedShards = 0;

			for (String bazaarProductId : bazaarFetcher.getBazaarSnapshot().keySet()) {
				if (!itemsFetcher.getSkyBlockItemsSnapshot().containsKey(bazaarProductId)) {
					try {
						if (apiFixer.isEnchantment(bazaarProductId)) {
							//itemsFetcher.skyBlockItems().put(bazaarProductId, apiFixer.createEnchant(bazaarProductId));
							itemsFetcher.putItem(bazaarProductId, apiFixer.createEnchant(bazaarProductId));
							fixedEnchants++;

						} else if (apiFixer.isEssence(bazaarProductId)) {
							itemsFetcher.putItem(bazaarProductId, apiFixer.createEssence(bazaarProductId));
							fixedEssences++;

						} else if (apiFixer.isShard(bazaarProductId)) {
							SkyBlockItem shard = apiFixer.createShard(bazaarProductId);
							if (shard != null) {
								//itemsFetcher.skyBlockItems().put(bazaarProductId, shard);
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
			}

			CaribouStonks.LOGGER.info("[HypixelDataSource] Fixed {} enchants, {} essences and {} Shards from Bazaar to SkyBlock Items",
					fixedEnchants, fixedEssences, fixedShards);
		}
	}
}
