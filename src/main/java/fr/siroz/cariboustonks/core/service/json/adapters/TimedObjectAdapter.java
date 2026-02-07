package fr.siroz.cariboustonks.core.service.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import java.io.IOException;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

public class TimedObjectAdapter extends TypeAdapter<TimedObjectModel> {

	private final TypeAdapter<Instant> instant;

	public TimedObjectAdapter(TypeAdapter<Instant> instantAdapter) {
		this.instant = instantAdapter;
	}

	@Override
	public void write(@NonNull JsonWriter writer, @NonNull TimedObjectModel timedObject) throws IOException {
		writer.beginObject();
		writer.name("id").value(timedObject.id());
		writer.name("message").value(timedObject.message());
		writer.name("expirationTime");
		instant.write(writer, timedObject.expirationTime());
		writer.name("type").value(timedObject.type());
		writer.endObject();
	}

	@Override
	public TimedObjectModel read(@NonNull JsonReader reader) throws IOException {
		reader.beginObject();
		String id = "";
		String message = "";
		Instant expirationTime = null;
		String type = "";
		while (reader.hasNext()) {
			switch (reader.nextName()) {
				case "id" -> id = reader.nextString();
				case "message" -> message = reader.nextString();
				case "expirationTime" -> expirationTime = instant.read(reader);
				case "type" -> type = reader.nextString();
				case null, default -> {
				}
			}
		}
		reader.endObject();

		return new TimedObjectModel(id, message, expirationTime, type);
	}
}
