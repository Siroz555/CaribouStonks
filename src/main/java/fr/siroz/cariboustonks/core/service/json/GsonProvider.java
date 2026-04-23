package fr.siroz.cariboustonks.core.service.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.siroz.cariboustonks.core.service.json.adapters.CodecTypeAdapter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jspecify.annotations.NonNull;

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

	@NonNull
	public static Gson standard() {
		return STANDARD_GSON;
	}

	@NonNull
	public static Gson prettyPrinting() {
		return PRETTY_PRINTING_GSON;
	}
}
