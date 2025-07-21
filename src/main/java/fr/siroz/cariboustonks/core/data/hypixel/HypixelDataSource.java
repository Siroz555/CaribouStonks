package fr.siroz.cariboustonks.core.data.hypixel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.data.hypixel.reply.SkyBlockBazaarReply;
import fr.siroz.cariboustonks.core.data.hypixel.reply.SkyBlockItemsReply;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.http.client.HttpResponseException;
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
public final class HypixelDataSource { // TODO clean up & docs

	// Liste des items SkyBlock (Hypixel SkyBlock API)
	private static final String ITEMS_API = "https://api.hypixel.net/v2/resources/skyblock/items";
	private final Object2ObjectMap<String, SkyBlockItem> skyBlockItems = new Object2ObjectOpenHashMap<>();

	// Liste des items au Bazaar (Hypixel SkyBlock API)
	private static final String BAZAAR_API = "https://api.hypixel.net/v2/skyblock/bazaar";
	private Object2ObjectMap<String, Product> bazaarData = new Object2ObjectOpenHashMap<>();

	private final HypixelAPIFixer apiFixer = new HypixelAPIFixer();
	private final ModDataSource modDataSource;

	private boolean skyBlockItemError = false;
	private boolean bazaarUpdateError = false;

	private boolean bazaarInUpdate = false;
	// Flags | fixMissingSkyBlockItems
	private boolean itemsLoaded = false;
	private boolean firstBazaarUpdated = false;
	private boolean hasCalledFixMissing = false;

	@ApiStatus.Internal
	public HypixelDataSource(@NotNull ModDataSource modDataSource) {
		this.modDataSource = modDataSource;
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> onClientStarted());
	}

	private void onClientStarted() {
		loadItems().thenRun(() -> {
			itemsLoaded = true;
			checkItemsResults();
			fixMissingSkyBlockItems();
		});

		TickScheduler.getInstance().runRepeating(() -> {
			if (ConfigManager.getConfig().general.stonks.bazaarTooltipPrice) {
				updateBazaar().thenRun(() -> {
					bazaarInUpdate = false;
					bazaarUpdateError = false;

					if (!firstBazaarUpdated) {
						firstBazaarUpdated = true;
						checkBazaarResult();
						fixMissingSkyBlockItems();
					} else {
						checkBazaarResult();
					}
				});
			}
		}, 5, TimeUnit.MINUTES);
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
			SkyBlockItem skyBlockItem = skyBlockItems.get(skyBlockItemId);
			if (skyBlockItem == null) {
				return fallback;
			}

			String hypixelMaterial = skyBlockItem.getMaterial();
			String minecraftId = modDataSource.getMinecraftId(hypixelMaterial);
			if (minecraftId == null || minecraftId.equals("NO_MATCH")) {
				return fallback;
			}

			ItemStack itemStack = new ItemStack(Items.BARRIER, 1);

			Optional<Item> item = ItemUtils.getItemById(minecraftId);
			if (item.isPresent()) {
				itemStack = new ItemStack(item.get(), 1);
			}

			if (skyBlockItem.isSkullItem() && skyBlockItem.getSkullTexture().isPresent()) {
				itemStack = ItemUtils.createSkull(skyBlockItem.getSkullTexture().get());
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
		return skyBlockItems.get(skyBlockItemId);
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
	 * Sinon, elle retourne une nouvelle liste {@link ObjectList} contenant tous les items SkyBlock
	 * actuellement enregistrés.
	 *
	 * @return une liste des SkyBlock items.
	 * @throws HypixelDataException S'il y a une erreur dans le mapping des items,
	 *                              la récupération des données depuis l'API
	 *                              ou si la liste des items SkyBlock est vide.
	 */
	@NotNull
	@Unmodifiable
	public ObjectList<SkyBlockItem> getSkyBlockItems() throws HypixelDataException {
		if (modDataSource.isItemsMappingError()) {
			throw new HypixelDataException(Text.of("Unable to map SkyBlock Items into Minecraft."));
		}

		if (skyBlockItemError) {
			throw new HypixelDataException(Text.of("Unable to fetch SkyBlock Items from Hypixel API."));
		}

		if (skyBlockItems.isEmpty()) {
			throw new HypixelDataException(Text.of("No SkyBlock Items is registered."));
		}

		return new ObjectArrayList<>(skyBlockItems.values());
	}

	@Contract(" -> new")
	public @NotNull Set<String> getSkyBlockItemsIds() {
		return new HashSet<>(skyBlockItems.keySet());
	}

	/**
	 * Retourne le nombre total des items SkyBlock actuellement enregistrés.
	 *
	 * @return le nombre total d'items SkyBlock
	 */
	public int getSkyBlockItemCounts() {
		return skyBlockItems.size();
	}

	public boolean isBazaarInUpdate() {
		return bazaarInUpdate;
	}

	public boolean hasBazaarItem(@Nullable String skyBlockItemId) {
		if (skyBlockItemId == null || skyBlockItemId.isEmpty()) return false;
		return bazaarData.containsKey(skyBlockItemId);
	}

	public Optional<Product> getBazaarItem(@Nullable String skyBlockItemId) {
		if (bazaarData == null || skyBlockItemId == null || skyBlockItemId.isEmpty()) return Optional.empty();
		return Optional.ofNullable(bazaarData.get(skyBlockItemId));
	}

	private @NotNull CompletableFuture<Void> loadItems() {
		CaribouStonks.LOGGER.info("[HypixelDataSource] Loading SkyBlock Items..");

		return fetchSkyBlockItems().thenAccept(reply -> {
			if (reply.getResponse() == null) {
				skyBlockItemError = true;
				return;
			}

			try {
				JsonArray array = reply.getResponse().get("items").getAsJsonArray();
				for (int i = 0; i < array.size(); i++) {

					JsonObject item = array.get(i).getAsJsonObject();
					if (item.has("id")) {
						String id = item.get("id").getAsString();
						// ignore les minions et autres
						if (apiFixer.isBlacklisted(id)) {
							continue;
						}

						try {
							SkyBlockItem skyBlockItem = new SkyBlockItem(item);
							skyBlockItems.put(id, skyBlockItem);
						} catch (Exception ex) {
							CaribouStonks.LOGGER.error(
									"[CaribouStonks HypixelData] Unable to parse SkyBlock Item: {}", id, ex);
						}
					}
				}
			} catch (Throwable ex) {
				skyBlockItemError = true;
				CaribouStonks.LOGGER.error("[HypixelDataSource] There was an error while loading SkyBlock Items", ex);
			}
		});
	}

	private @NotNull CompletableFuture<Void> updateBazaar() {
		CaribouStonks.LOGGER.info("[HypixelDataSource] Updating SkyBlock Bazaar..");
		bazaarInUpdate = true;

		return fetchBazaar().thenAccept(reply -> {
			if (reply == null) {
				bazaarUpdateError = true;
				return;
			}

			if (reply.getProducts() == null || reply.getProducts().isEmpty()) {
				bazaarUpdateError = true;
				return;
			}

			//bazaarLastUpdated = reply.getLastUpdated();
			bazaarData = new Object2ObjectOpenHashMap<>(reply.getProducts());
		});
	}

	@Contract(" -> new")
	private @NotNull CompletableFuture<SkyBlockItemsReply> fetchSkyBlockItems() {
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(ITEMS_API)) {
				if (!response.success()) {
					throw new HttpResponseException(response.statusCode(), response.content());
				}

				SkyBlockItemsReply reply = new SkyBlockItemsReply(
						GsonProvider.standard().fromJson(response.content(), JsonObject.class));
				if (!reply.isSuccess()) {
					throw new RuntimeException("SkyBlock Resource Items reply failed: " + reply.getCause());
				}

				return reply;
			} catch (Throwable ex) {
				CaribouStonks.LOGGER.error("[HypixelDataSource] Failed to fetch SkyBlock Items from Hypixel API", ex);
				return null;
			}
		});
	}

	@Contract(" -> new")
	private @NotNull CompletableFuture<SkyBlockBazaarReply> fetchBazaar() {
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(BAZAAR_API)) {
				if (!response.success()) {
					throw new HttpResponseException(response.statusCode(), response.content());
				}

				// TODO - remplacer les Reply de facon "officiel", arrêter de laisser Gson faire tout seul
				SkyBlockBazaarReply reply
						= GsonProvider.prettyPrinting().fromJson(response.content(), SkyBlockBazaarReply.class);
				if (reply == null) {
					throw new IllegalStateException("Json is null or empty");
				}

				if (!reply.isSuccess()) {
					throw new RuntimeException("SkyBlock Bazaar reply failed: " + reply.getCause());
				}

				return reply;
			} catch (Throwable ex) {
				CaribouStonks.LOGGER.error("[HypixelDataSource] Failed to fetch SkyBlock Bazaar from Hypixel API", ex);
				return null;
			}
		});
	}

	private void checkItemsResults() {
		if (!skyBlockItemError) {
			CaribouStonks.LOGGER.info("[HypixelDataSource] Loaded {} SkyBlock Items", skyBlockItems.size());
		} else {
			CaribouStonks.LOGGER.error("[HypixelDataSource] Unable to load SkyBlock Items from Hypixel API");
		}

		if (!skyBlockItemError && !modDataSource.isItemsMappingError()) {
			List<String> hypixelMaterials = skyBlockItems.values().stream()
					.map(SkyBlockItem::getMaterial)
					.collect(Collectors.toSet())
					.stream()
					.toList();

			for (String material : hypixelMaterials) {
				if (!modDataSource.containsItem(material)) {
					CaribouStonks.LOGGER.warn(
							"[HypixelDataSource] (Minecraft Ids Mapping) -> {} is not registered!", material);
				}
			}
		} else {
			CaribouStonks.LOGGER.error("[HypixelDataSource] (Minecraft Ids Mapping) SkyBlock Items error or mapping error");
		}
	}

	private void checkBazaarResult() {
		if (!bazaarUpdateError) {
			CaribouStonks.LOGGER.info("[HypixelDataSource] Updated {} Bazaar Items", bazaarData.size());
		} else {
			CaribouStonks.LOGGER.error("[HypixelDataSource] Unable to update Bazaar Items from Hypixel API");
		}
	}

	@SuppressWarnings("checkstyle:CyclomaticComplexity") // -_-
	private void fixMissingSkyBlockItems() {
		if (itemsLoaded && firstBazaarUpdated && !hasCalledFixMissing) {
			hasCalledFixMissing = true;
			CaribouStonks.LOGGER.info("[HypixelDataSource] Fixing Hypixel SkyBlock Items..");

			int fixedEnchants = 0;
			int fixedEssences = 0;
			int fixedShards = 0;

			for (String bazaarProductId : bazaarData.keySet()) {
				if (!skyBlockItems.containsKey(bazaarProductId)) {
					try {
						if (apiFixer.isEnchantment(bazaarProductId)) {
							skyBlockItems.put(bazaarProductId, apiFixer.createEnchant(bazaarProductId));
							fixedEnchants++;

						} else if (apiFixer.isEssence(bazaarProductId)) {
							skyBlockItems.put(bazaarProductId, apiFixer.createEssence(bazaarProductId));
							fixedEssences++;

						} else if (apiFixer.isShard(bazaarProductId)) {
							SkyBlockItem shard = apiFixer.createShard(bazaarProductId);
							if (shard != null) {
								skyBlockItems.put(bazaarProductId, shard);
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
