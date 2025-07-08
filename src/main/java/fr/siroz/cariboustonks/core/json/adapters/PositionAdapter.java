package fr.siroz.cariboustonks.core.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.util.position.Position;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PositionAdapter extends TypeAdapter<Position> {

    @Override
    public void write(@NotNull JsonWriter writer, @NotNull Position position) throws IOException {
        writer.value(position.asString());
    }

    @Override
    public Position read(@NotNull JsonReader reader) throws IOException {
        return Position.fromString(reader.nextString());
    }
}
