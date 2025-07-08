package fr.siroz.cariboustonks.core.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.siroz.cariboustonks.core.json.adapters.CodecTypeAdapter;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.NotNull;

public final class GsonProvider {

	private static final Gson STANDARD_GSON = new GsonBuilder()
			.registerTypeAdapterFactory(CustomTypeAdapterFactory.INSTANCE)
			.registerTypeHierarchyAdapter(Text.class, new CodecTypeAdapter<>(TextCodecs.CODEC))
			.serializeNulls()
			.disableHtmlEscaping()
			.create();

	private static final Gson PRETTY_PRINTING_GSON = new GsonBuilder()
			.registerTypeAdapterFactory(CustomTypeAdapterFactory.INSTANCE)
			.registerTypeHierarchyAdapter(Text.class, new CodecTypeAdapter<>(TextCodecs.CODEC))
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
}
