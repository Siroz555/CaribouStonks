package fr.siroz.cariboustonks.core.skyblock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;

public final class SkyBlockRegistry {

	public record AbiphoneContact(@NonNull String name, @NonNull List<String> callNames) {
		public AbiphoneContact {
			callNames = List.copyOf(callNames);
		}
	}

	private record WarpSnapshot(Collection<String> warps) {
		static final WarpSnapshot EMPTY = new WarpSnapshot(Collections.emptyList());

		static @NonNull WarpSnapshot of(Collection<String> warps) {
			return new WarpSnapshot(warps);
		}
	}

	private record AbiphoneSnapshot(Map<String, AbiphoneContact> byName) {
		static final AbiphoneSnapshot EMPTY = new AbiphoneSnapshot(Collections.emptyMap());

		static @NonNull AbiphoneSnapshot of(@NonNull Collection<AbiphoneContact> contacts) {
			return new AbiphoneSnapshot(contacts.stream()
					.collect(Collectors.toUnmodifiableMap(AbiphoneContact::name, Function.identity())));
		}
	}

	private static volatile WarpSnapshot warpSnapshot = WarpSnapshot.EMPTY;
	private static volatile AbiphoneSnapshot abiphoneSnapshot = AbiphoneSnapshot.EMPTY;

	private SkyBlockRegistry() {
	}

	public static void loadWarps(@NonNull Collection<String> warps) {
		warpSnapshot = WarpSnapshot.of(warps);
	}

	public static void loadAbiphoneContacts(@NonNull Collection<AbiphoneContact> contacts) {
		abiphoneSnapshot = AbiphoneSnapshot.of(contacts);
	}

	public static Collection<String> getWarps() {
		return warpSnapshot.warps();
	}

	public static @NonNull Collection<AbiphoneContact> getAbiphoneContacts() {
		return abiphoneSnapshot.byName().values();
	}

	public static int sizeOfWarps() {
		return warpSnapshot.warps().size();
	}

	public static int sizeOfAbiphoneContacts() {
		return abiphoneSnapshot.byName().size();
	}
}
