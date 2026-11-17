package fr.siroz.cariboustonks.core.skyblock;

import java.util.Collection;
import java.util.Collections;
import org.jspecify.annotations.NonNull;

public final class SkyBlockRegistry {

	private record WarpSnapshot(Collection<String> warps) {
		static final WarpSnapshot EMPTY = new WarpSnapshot(Collections.emptyList());

		static @NonNull WarpSnapshot of(Collection<String> warps) {
			return new WarpSnapshot(warps);
		}
	}

	private static volatile WarpSnapshot warpSnapshot = WarpSnapshot.EMPTY;

	private SkyBlockRegistry() {
	}

	public static void loadWarps(Collection<String> warps) {
		warpSnapshot = WarpSnapshot.of(warps);
	}

	public static Collection<String> getWarps() {
		return warpSnapshot.warps();
	}

	public static int sizeOfWarps() {
		return warpSnapshot.warps().size();
	}
}
