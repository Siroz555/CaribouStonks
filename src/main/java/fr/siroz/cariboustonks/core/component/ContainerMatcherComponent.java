package fr.siroz.cariboustonks.core.component;

import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@code Component} that provides a container matching utility based on title {@code patterns}.
 *
 * @see ContainerOverlayComponent
 * @see TooltipAppenderComponent
 */
public final class ContainerMatcherComponent implements Component {
	private final Pattern titlePattern;
	private final Integer expectedSlots;
	//private final Predicate<String> customMatcher;

	private ContainerMatcherComponent(Pattern titlePattern, Integer expectedSlots) {
		this.titlePattern = titlePattern;
		this.expectedSlots = expectedSlots;
	}

	/**
	 * Checks if this matcher applies to the given Screen.
	 */
	public boolean matches(@Nullable Screen screen, int slotCount) {
		if (screen == null) return false;

		String containerTitle = screen.getTitle().getString();

		if (expectedSlots != null && expectedSlots != slotCount) {
			return false;
		}

		return titlePattern == null || titlePattern.matcher(containerTitle).matches();
	}

	@Nullable
	public Pattern getTitlePattern() {
		return titlePattern;
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a new {@code ContainerMatcherComponent} without matching criteria.
	 *
	 * @return a new Component instance
	 */
	@NonNull
	public static ContainerMatcherComponent empty() {
		return new ContainerMatcherComponent(null, null);
	}

	/**
	 * Creates a new {@code ContainerMatcherComponent} with the Title Pattern criteria.
	 *
	 * @return a new Component instance
	 */
	@NonNull
	public static ContainerMatcherComponent of(@Nullable Pattern titlePattern) {
		return new ContainerMatcherComponent(titlePattern, null);
	}

	public static class Builder {
		private Pattern titlePattern;
		private Integer expectedSlots;

		public Builder titlePattern(@Nullable Pattern pattern) {
			this.titlePattern = pattern;
			return this;
		}

		@SuppressWarnings("unused")
		public Builder titleContains(@NonNull String text) {
			this.titlePattern = Pattern.compile(".*" + Pattern.quote(text) + ".*");
			return this;
		}

		@SuppressWarnings("unused")
		public Builder titleEquals(@NonNull String title) {
			this.titlePattern = Pattern.compile(Pattern.quote(title));
			return this;
		}

		@SuppressWarnings("unused")
		public Builder slotCount(int slots) {
			this.expectedSlots = slots;
			return this;
		}

		public ContainerMatcherComponent build() {
			return new ContainerMatcherComponent(titlePattern, expectedSlots);
		}
	}
}
