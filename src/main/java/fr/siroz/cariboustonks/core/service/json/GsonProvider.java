package fr.siroz.cariboustonks.core.service.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.core.service.json.adapters.CodecTypeAdapter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GsonProvider {

	private static final Gson STANDARD_GSON = new GsonBuilder()
			.registerTypeAdapterFactory(CustomTypeAdapterFactory.INSTANCE)
			.registerTypeHierarchyAdapter(Component.class, new CodecTypeAdapter<>(ComponentSerialization.CODEC))
			.serializeNulls()
			.disableHtmlEscaping()
			.create();

	private static final Gson PRETTY_PRINTING_GSON = new GsonBuilder()
			.registerTypeAdapterFactory(CustomTypeAdapterFactory.INSTANCE)
			.registerTypeHierarchyAdapter(Component.class, new CodecTypeAdapter<>(ComponentSerialization.CODEC))
			.serializeNulls()
			.disableHtmlEscaping()
			.setPrettyPrinting()
			.create();

	private GsonProvider() {
	}

	@NotNull
	public static Gson standard() {
		return STANDARD_GSON;
	}

	@NotNull
	public static Gson prettyPrinting() {
		return PRETTY_PRINTING_GSON;
	}

	@Nullable
	public static JsonObject safeGetAsObject(@Nullable JsonObject parent, @NotNull String member) {
		if (parent == null || !parent.has(member) || parent.get(member).isJsonNull()) return null;

		JsonElement element = parent.get(member);
		return element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	@Nullable
	public static JsonArray safeGetAsArray(@Nullable JsonObject parent, @NotNull String member) {
		if (parent == null || !parent.has(member) || parent.get(member).isJsonNull()) return null;

		JsonElement element = parent.get(member);
		return element.isJsonArray() ? element.getAsJsonArray() : null;
	}
}
