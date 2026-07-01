package fr.siroz.cariboustonks.features.misc.bestiary;

import fr.siroz.cariboustonks.core.skyblock.tablist.TabLine;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

record BestiaryEntry(
		@NonNull String mobName,
		int tier,
		long current,
		long max,
		@NonNull Component lineComponent
) {
	private static final Pattern LINE_PATTERN = Pattern.compile("^(.+) (\\d+): ([\\d,]+)/([\\d,]+)$");

	public static Optional<BestiaryEntry> parse(@NonNull TabLine line) {
		if (line.isEmpty() || !line.isIndented()) return Optional.empty();

		Matcher matcher = LINE_PATTERN.matcher(line.text().trim());
		if (!matcher.matches()) return Optional.empty();

		try {
			String mobName = matcher.group(1).trim();
			int tier = Integer.parseInt(matcher.group(2)); // pas de toInt pour quand même lever le NumberFormatException
			long current = parseNumber(matcher.group(3));
			long max = parseNumber(matcher.group(4));
			return Optional.of(new BestiaryEntry(mobName, tier, current, max, line.component()));
		} catch (Exception _) {
			return Optional.empty();
		}
	}

	private static long parseNumber(String raw) {
		return Long.parseLong(raw.replace(",", ""));
	}

	public boolean isMaxed() {
		return current >= max;
	}
}
