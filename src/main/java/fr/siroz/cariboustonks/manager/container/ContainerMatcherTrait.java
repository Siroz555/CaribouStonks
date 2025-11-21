package fr.siroz.cariboustonks.manager.container;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@code trait} that provides a container matching utility based on title {@code patterns}.
 * Implementing classes can define a title pattern to filter screens based on their {@code title text}.
 */
public interface ContainerMatcherTrait {

	/**
	 * Retrieves the pattern used to match the title of a screen.
	 * This pattern is used to determine if a given screen's title matches the defined criteria.
	 *
	 * @return the pattern used for matching screen titles, or null if no pattern is defined
	 */
	@Nullable
	Pattern getTitlePattern();

	/**
	 * Checks if the provided screen matches the criteria defined by the implementing class.
	 * The matching is based on the screen's title text.
	 *
	 * @param screen the screen to be matched
	 * @return {@code true} if the screen matches the defined criteria
	 */
	default boolean matches(@NotNull Screen screen) {
		return matches(screen.getTitle().getString());
	}

	/**
	 * Checks if the provided title matches the defined pattern.
	 *
	 * @param title the title to be matched against the defined pattern
	 * @return {@code true} if the title matches the pattern or if no pattern is defined
	 */
	default boolean matches(@NotNull String title) {
		Pattern pattern = getTitlePattern();
		if (pattern == null) {
			return true;
		}

		Matcher matcher = pattern.matcher(title);
		return matcher.matches();
	}
}
