package fr.siroz.cariboustonks.core.service.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.siroz.cariboustonks.util.colors.Color;
import java.io.IOException;
import org.jspecify.annotations.NonNull;

public class ColorAdapter extends TypeAdapter<Color> {

    @Override
    public void write(@NonNull JsonWriter writer, @NonNull Color color) throws IOException {
        writer.value(color.toString());
    }

    @Override
    public Color read(@NonNull JsonReader reader) throws IOException {
        return Color.fromHexString(reader.nextString());
    }
}
