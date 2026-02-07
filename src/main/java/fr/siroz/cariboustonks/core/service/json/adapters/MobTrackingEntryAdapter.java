package fr.siroz.cariboustonks.core.service.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.core.model.MobTrackingModel;
import java.io.IOException;
import org.jspecify.annotations.NonNull;

public class MobTrackingEntryAdapter extends TypeAdapter<MobTrackingModel> {

	@Override
	public void write(@NonNull JsonWriter writer, @NonNull MobTrackingModel model) throws IOException {
		writer.beginObject();
		writer.name("name").value(model.getName());
		writer.name("enabled").value(model.isEnabled());
		writer.name("notifyOnSpawn").value(model.isNotifyOnSpawn());
		writer.endObject();
	}

	@Override
	public MobTrackingModel read(@NonNull JsonReader reader) throws IOException {
		reader.beginObject();
		String name = "";
		boolean enabled = true;
		boolean notifyOnSpawn = false;
		while (reader.hasNext()) {
			switch (reader.nextName()) {
				case "name" -> name = reader.nextString();
				case "enabled" -> enabled = reader.nextBoolean();
				case "notifyOnSpawn" -> notifyOnSpawn = reader.nextBoolean();
				case null, default -> {
				}
			}
		}
		reader.endObject();

		return new MobTrackingModel(name, enabled, notifyOnSpawn);
	}
}
