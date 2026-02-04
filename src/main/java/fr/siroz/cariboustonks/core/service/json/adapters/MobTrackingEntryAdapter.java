package fr.siroz.cariboustonks.core.service.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingRegistry;
import java.io.IOException;

public class MobTrackingEntryAdapter extends TypeAdapter<MobTrackingRegistry.MobTrackingConfig> {

	@Override
	public void write(JsonWriter writer, MobTrackingRegistry.MobTrackingConfig config) throws IOException {
		writer.beginObject();
		writer.name("name").value(config.name);
		writer.name("enabled").value(config.enabled);
		writer.name("notifyOnSpawn").value(config.notifyOnSpawn);
		writer.endObject();
	}

	@Override
	public MobTrackingRegistry.MobTrackingConfig read(JsonReader reader) throws IOException {
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

		return new MobTrackingRegistry.MobTrackingConfig(name, enabled, notifyOnSpawn);
	}
}
