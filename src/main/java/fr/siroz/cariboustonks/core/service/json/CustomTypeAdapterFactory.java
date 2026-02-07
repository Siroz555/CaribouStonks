package fr.siroz.cariboustonks.core.service.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.service.json.adapters.ColorAdapter;
import fr.siroz.cariboustonks.core.service.json.adapters.InstantAdapter;
import fr.siroz.cariboustonks.core.service.json.adapters.MobTrackingEntryAdapter;
import fr.siroz.cariboustonks.core.service.json.adapters.PositionAdapter;
import fr.siroz.cariboustonks.core.service.json.adapters.TimedObjectAdapter;
import fr.siroz.cariboustonks.core.service.json.adapters.WaypointAdapter;
import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingRegistry;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.position.Position;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
		this.adapters.put(TimedObjectModel.class, new TimedObjectAdapter(instantAdapter));
		this.adapters.put(Waypoint.class, new WaypointAdapter(positionAdapter, colorAdapter));
		this.adapters.put(MobTrackingRegistry.MobTrackingEntry.class, new MobTrackingEntryAdapter());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable TypeAdapter<T> create(Gson gson, @NonNull TypeToken<T> type) {
		TypeAdapter<?> adapter = adapters.get(type.getRawType());
		return adapter != null ? (TypeAdapter<T>) adapter : null;
	}
}
