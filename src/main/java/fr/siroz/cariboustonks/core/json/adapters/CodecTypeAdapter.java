package fr.siroz.cariboustonks.core.json.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

public record CodecTypeAdapter<T>(Codec<T> codec) implements JsonSerializer<T>, JsonDeserializer<T> {

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
    }
}
