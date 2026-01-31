package fr.siroz.cariboustonks.core.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.system.waypoint.Waypoint;
import fr.siroz.cariboustonks.system.waypoint.options.TextOption;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class WaypointAdapter extends TypeAdapter<Waypoint> {

	private final TypeAdapter<Position> positionAdapter;
	private final TypeAdapter<Color> colorAdapter;

	public WaypointAdapter(TypeAdapter<Position> positionAdapter, TypeAdapter<Color> colorAdapter) {
		this.positionAdapter = positionAdapter;
		this.colorAdapter = colorAdapter;
	}

	@Override
	public void write(@NotNull JsonWriter writer, @NotNull Waypoint waypoint) throws IOException {
		writer.beginObject();
		writer.name("uuid").value(waypoint.getUuid().toString());
		writer.name("position");
		positionAdapter.write(writer, waypoint.getPosition());
		writer.name("enabled").value(waypoint.isEnabled());
		writer.name("type").value(waypoint.getType().name());
		writer.name("alpha").value(waypoint.getAlpha());
		writer.name("color");
		colorAdapter.write(writer, waypoint.getColor());
		Optional<Component> text = waypoint.getTextOption().getText();
		writer.name("text").value(text.isPresent() ? GsonProvider.standard().toJson(text.get()) : "");
		writer.endObject();
	}

	@Override
	public Waypoint read(@NotNull JsonReader reader) throws IOException {
		reader.beginObject();
		UUID uuid = UUID.randomUUID();
		Position position = Position.ORIGIN;
		boolean enabled = false;
		Waypoint.Type type = Waypoint.Type.WAYPOINT;
		float alpha = 1f;
		Color color = Colors.RED;
		Component text = null;
		while (reader.hasNext()) {
			switch (reader.nextName()) {
				case "uuid" -> uuid = UUID.fromString(reader.nextString());
				case "position" -> position = positionAdapter.read(reader);
				case "enabled" -> enabled = reader.nextBoolean();
				case "type" -> type = Waypoint.Type.valueOf(reader.nextString());
				case "alpha" -> alpha = Float.parseFloat(reader.nextString());
				case "color" -> color = colorAdapter.read(reader);
				case "text" -> text = GsonProvider.standard().fromJson(reader.nextString(), Component.class);
				case null, default -> {
				}
			}
		}
		reader.endObject();

		return Waypoint.builder(position)
				.uuid(uuid)
				.enabled(enabled)
				.type(type)
				.alpha(alpha)
				.color(color)
				.textOption(TextOption.builder().withText(text).build())
				.build();
	}
}
