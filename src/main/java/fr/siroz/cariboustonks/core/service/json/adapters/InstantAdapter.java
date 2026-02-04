package fr.siroz.cariboustonks.core.service.json.adapters;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class InstantAdapter extends TypeAdapter<Instant> {

    @Override
    public void write(@NotNull JsonWriter writer, @NotNull Instant instant) throws IOException {
        writer.value(instant.toString());
    }

    @Override
    public Instant read(@NotNull JsonReader reader) throws IOException {
        try {
			return Instant.parse(reader.nextString());
		} catch (DateTimeException ex) {
			throw new JsonParseException(ex);
		}
    }
}
