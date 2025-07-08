package fr.siroz.cariboustonks.core.data.hypixel;

import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.util.Rarity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class SkyBlockItem {

	private final String skyBlockId;
	private final String material;
	private final String name;
	private final Rarity tier;

	private String skullTexture;
	private boolean skullItem = false;

	public SkyBlockItem(String skyBlockId, String material, String name, Rarity tier) {
		this.skyBlockId = skyBlockId;
		this.material = material;
		this.name = name;
		this.tier = tier;
	}

	public SkyBlockItem(@NotNull JsonObject jsonItem)
			throws NullPointerException, UnsupportedOperationException, IllegalStateException {
		// Toujours présents
		this.skyBlockId = jsonItem.get("id").getAsString();
		this.material = jsonItem.get("material").getAsString();
		this.name = jsonItem.get("name").getAsString();

		// Quelques items n'ont pas de tier attribué
		String tierString = null;
		if (jsonItem.has("tier")) {
			tierString = jsonItem.get("tier").getAsString();
		}
		this.tier = Rarity.getRarity(tierString);

		if (jsonItem.has("skin") && this.material.equals("SKULL_ITEM")) {
			JsonObject skin = jsonItem.get("skin").getAsJsonObject();
			this.skullTexture = skin.get("value").getAsString();
			this.skullItem = true;
		}
	}

	public String getSkyBlockId() {
		return skyBlockId;
	}

	public String getMaterial() {
		return material;
	}

	public String getName() {
		return name;
	}

	public Rarity getTier() {
		return tier;
	}

	public boolean isSkullItem() {
		return skullItem;
	}

	@Contract(pure = true)
	public @NotNull Optional<String> getSkullTexture() {
		return Optional.ofNullable(skullTexture);
	}
}
