package fr.siroz.cariboustonks.util;

import com.mojang.serialization.Codec;
import fr.siroz.cariboustonks.core.service.json.GsonProvider;
import java.awt.Color;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public final class CodecUtils {

	public static final Codec<Color> COLOR_CODEC = Codec.INT.xmap(argb -> new Color(argb, true), Color::getRGB);

	private CodecUtils() {
	}

	public static Optional<String> textToJson(@NonNull Component text) {
		try {
			String json = GsonProvider.standard().toJson(text);
			return Optional.of(json);
		} catch (Exception _) {
			return Optional.empty();
		}
	}

	public static Optional<Component> jsonToText(@NonNull String json) {
		try {
			Component text = GsonProvider.standard().fromJson(json, Component.class);
			return Optional.ofNullable(text);
		} catch (Exception _) {
			return Optional.empty();
		}
	}
}
