package fr.siroz.cariboustonks.core.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import fr.siroz.cariboustonks.core.json.adapters.ColorAdapter;
import fr.siroz.cariboustonks.core.json.adapters.InstantAdapter;
import fr.siroz.cariboustonks.core.json.adapters.PositionAdapter;
import fr.siroz.cariboustonks.core.json.adapters.TimedObjectAdapter;
import fr.siroz.cariboustonks.core.json.adapters.WaypointAdapter;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.position.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class CustomTypeAdapterFactory implements TypeAdapterFactory {

	public static final CustomTypeAdapterFactory INSTANCE = new CustomTypeAdapterFactory();

	private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<>();

	private CustomTypeAdapterFactory() {
		// "Serializers"
		TypeAdapter<Color> colorAdapter = new ColorAdapter();
		TypeAdapter<Instant> instantAdapter = new InstantAdapter();
		TypeAdapter<Position> positionAdapter = new PositionAdapter();

		this.adapters.put(Color.class, colorAdapter);
		this.adapters.put(Instant.class, instantAdapter);
		this.adapters.put(Position.class, positionAdapter);

		// "Class Adapters"
		this.adapters.put(TimedObject.class, new TimedObjectAdapter(instantAdapter));
		this.adapters.put(Waypoint.class, new WaypointAdapter(positionAdapter, colorAdapter));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable TypeAdapter<T> create(Gson gson, @NotNull TypeToken<T> type) {
		TypeAdapter<?> adapter = adapters.get(type.getRawType());
		return adapter != null ? (TypeAdapter<T>) adapter : null;
	}
}
