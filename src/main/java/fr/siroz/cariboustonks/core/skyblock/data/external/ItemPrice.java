package fr.siroz.cariboustonks.core.skyblock.data.external;

import java.time.Instant;

public record ItemPrice(
		Instant time,
		double buyPrice,
		Double sellPrice
) {
}
