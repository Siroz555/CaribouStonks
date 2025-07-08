package fr.siroz.cariboustonks.core.data.generic;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface TestData {

	double BASE_PRICE = 100.0;
	double MAX_FLUCTUATION = 2.0;

	static @NotNull Set<ItemPrice> generateTestDataForLastThreeMonths() {
		Set<ItemPrice> prices = new HashSet<>();
		Instant now = Instant.now();
		LocalDateTime startDateTime = LocalDateTime.now().minusMonths(3);
		Instant start = startDateTime.toInstant(ZoneOffset.UTC);
		Random random = new Random();

		Instant currentTime = start;
		double buyPrice = 100000;
		double sellPrice = 90000;

		while (currentTime.isBefore(now)) {
			prices.add(new ItemPrice(currentTime, buyPrice, sellPrice));
			if (random.nextInt(0, 100 + 1) > 30) {
				buyPrice += 100;
				sellPrice += 10;
			} else {
				buyPrice -= 100;
				sellPrice -= 10;
			}
			currentTime = currentTime.plus(1, ChronoUnit.MINUTES);
		}

		return prices;
	}
}
