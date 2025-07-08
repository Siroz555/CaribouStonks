package fr.siroz.cariboustonks.core.data.generic;

import java.time.Instant;

public record ItemPrice(Instant time, double buyPrice, Double sellPrice) {
}
