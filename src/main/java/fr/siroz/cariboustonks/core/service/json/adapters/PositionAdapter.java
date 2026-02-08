package fr.siroz.cariboustonks.core.service.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.core.module.position.Position;
import java.io.IOException;
import org.jspecify.annotations.NonNull;

public class PositionAdapter extends TypeAdapter<Position> {

    @Override
    public void write(@NonNull JsonWriter writer, @NonNull Position position) throws IOException {
        writer.value(position.asString());
    }

    @Override
    public Position read(@NonNull JsonReader reader) throws IOException {
        return Position.fromString(reader.nextString());
    }
}
