package fr.siroz.cariboustonks.core.module.gui;

import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code Trait} that provides a container matching utility based on title {@code patterns}.
 */
public interface MatcherTrait {

	/**
	 * Checks if this matcher applies to the given Screen.
	 *
	 * @param screen screen
	 * @param slotCount slot count
	 * @return matches result
	 */
	boolean matches(@Nullable Screen screen, int slotCount);

	/**
	 * Creates a new {@code MatcherTrait} without matching criteria
	 *
	 * @return {@link PatternMatcher}
	 */
	@NonNull
	static MatcherTrait empty() {
		return new PatternMatcher(null, null);
	}

	/**
	 * Creates a new {@code MatcherTrait} with the Title Pattern criteria
	 *
	 * @return {@link PatternMatcher}
	 */
	@NonNull
	static MatcherTrait pattern(@Nullable Pattern titlePattern) {
		return new PatternMatcher(titlePattern, null);
	}

	class PatternMatcher implements MatcherTrait {
		private final Pattern titlePattern;
		private final Integer expectedSlots;

		public PatternMatcher(Pattern titlePattern,  Integer expectedSlots) {
			this.titlePattern = titlePattern;
			this.expectedSlots = expectedSlots;
		}

		@Override
		public boolean matches(Screen screen, int slotCount) {
			if (screen == null) return false;

			String containerTitle = screen.getTitle().getString();

			if (expectedSlots != null && expectedSlots != slotCount) {
				return false;
			}

			return titlePattern == null || titlePattern.matcher(containerTitle).matches();
		}
	}
}
