package fr.siroz.cariboustonks.feature.stonks.graph;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.generic.ItemPrice;
import fr.siroz.cariboustonks.feature.stonks.AbstractItemStonksWidget;
import fr.siroz.cariboustonks.util.colors.ColorUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.render.GuiRenderUtils;
import fr.siroz.cariboustonks.util.render.gui.Point;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * TODO - C'est un bordel monumentale lul. Ça marche, mais c'est beaucoup trop brouillon pour le moment coté code -_-
 *  Beaucoup de chose useless et surtout a modifier pour avoir un vrai truc "trading" mais sur MC..
 *  Il y a plein de chose a changer, que sa soit coté ordre et logique d'implémentation de chaque partie.
 */
public class ItemGraphWidget extends AbstractItemStonksWidget {

	private GraphDataFilter.Granularity granularity;
	private Type type = Type.AUCTION;
	private Instant lastUpdateTime;

	private final List<ItemPrice> rawData = new ArrayList<>();
	private Map<Point, ItemPrice> graphData;
	private List<ItemPrice> prices;

	private Instant minTime;
	private Instant maxTime;
	private double minPrice;
	private double maxPrice;

	public ItemGraphWidget(@NotNull List<ItemPrice> neuData, int width, int height) {
		super(width, height);

		this.granularity = GraphDataFilter.Granularity.DAY;
		this.lastUpdateTime = Instant.now();

		this.rawData.addAll(neuData);

		// filtrer par Instant, pour simplifier le GraphDataFilter#filterData
		// si la méthode est rappelé par une update
		this.rawData.sort(Comparator.comparing(ItemPrice::time));

		// Filtrer le rawData selon la granularité et retourner la liste des ItemPrice
		this.prices = GraphDataFilter.filterData(this.rawData, this.granularity);

		// Savoir si l'item est du Bazaar ou de l'Auction House
		this.type = this.prices.stream()
				.filter(itemPrice -> itemPrice.sellPrice() != null)
				.findFirst()
				.map(itemPrice -> Type.BAZAAR)
				.orElse(this.type);

		updateMinMaxValues();
	}

	public boolean updateGranularity() {
		Instant now = Instant.now();
		if (Duration.between(lastUpdateTime, now).getSeconds() < 2) {
			return false;
		}

		GraphDataFilter.Granularity[] values = GraphDataFilter.Granularity.values();
		int currentIndex = granularity.ordinal();
		int nextIndex = (currentIndex + 1) % values.length;

		updateGraphData(values[nextIndex]);
		lastUpdateTime = now;

		return true;
	}

	public String getGranularity() {
		return switch (granularity) {
			case HOUR -> "Hour";
			case DAY -> "Day";
			case WEEK -> "Week";
			case MONTH -> "Month";
		};
	}

	/**
	 * Met à jour les prix selon la nouvelle granularité
	 *
	 * @param newGranularity the new Granularity
	 */
	private void updateGraphData(GraphDataFilter.Granularity newGranularity) {
		prices = null;
		prices = GraphDataFilter.filterData(rawData, newGranularity);
		updateMinMaxValues();
		granularity = newGranularity;
	}

	private void updateMinMaxValues() {
		minTime = prices.stream().map(ItemPrice::time).min(Instant::compareTo).orElse(Instant.now());
		maxTime = prices.stream().map(ItemPrice::time).max(Instant::compareTo).orElse(Instant.now());

		if (type == Type.BAZAAR) {
			minPrice = Math.min(
					prices.stream().mapToDouble(ItemPrice::buyPrice).min().orElse(0.0),
					prices.stream().mapToDouble(ItemPrice::sellPrice).min().orElse(0.0)
			);
			maxPrice = Math.max(
					prices.stream().mapToDouble(ItemPrice::buyPrice).max().orElse(1.0),
					prices.stream().mapToDouble(ItemPrice::sellPrice).min().orElse(1.0)
			);
		} else {
			minPrice = prices.stream().mapToDouble(ItemPrice::buyPrice).min().orElse(0.0);
			maxPrice = prices.stream().mapToDouble(ItemPrice::buyPrice).max().orElse(1.0);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
		final int x1 = x + 20;
		final int y1 = y + 25; // 20
		final int x2 = x1 + this.width / 2;
		final int y2 = y1 + this.height / 2;
		final int borderMarge = 10;
		int borderColor = new Color(192, 192, 192).getRGB();

		context.fill(x1, y1 - borderMarge, x2, y1 - borderMarge + 1, borderColor);
		context.fill(x1, y2 + borderMarge - 1, x2, y2 + borderMarge, borderColor);
		context.fill(x1, y1 - borderMarge + 1, x1 + 1, y2 + borderMarge - 1, borderColor);
		context.fill(x2 - 1, y1 - borderMarge + 1, x2, y2 + borderMarge - 1, borderColor);

		if (prices == null || prices.isEmpty() || prices.size() < 3) {
			return;
		}

		// / 0
		if (minTime.equals(maxTime) || minPrice == maxPrice) {
			return;
		}

		List<Point> pointsBuy = new ArrayList<>();
		Map<Point, ItemPrice> toData = new HashMap<>();
		for (ItemPrice item : prices) {
			// Normalisation des coordonnées
			double timeRatio = (double) ChronoUnit.MILLIS.between(minTime, item.time())
					/ ChronoUnit.MILLIS.between(minTime, maxTime);
			double priceRatio = (item.buyPrice() - minPrice) / (maxPrice - minPrice);

			double pointX = x1 + timeRatio * (x2 - x1);
			double pointY = y2 - priceRatio * (y2 - y1); // Y inversé, car origine en haut à gauche
			pointsBuy.add(new Point((int) Math.round(pointX), (int) Math.round(pointY)));

			toData.put(new Point((int) Math.round(pointX), (int) Math.round(pointY)), item); // +1? -1?
		}

		graphData = toData;

		if (ConfigManager.getConfig().general.stonks.showGradientInGraphScreen) {
			//new Color(0x1D6517);
			int startColor = 0x1D6517;
			int endColor = 0x001D6517;
			drawGradient(context, pointsBuy, startColor, endColor, y2 + 10, false);
		}

		GuiRenderUtils.renderLinesFromPoints(context, pointsBuy.toArray(new Point[0]), new Color(0, 222, 5), 3f);

		// Calculer de facon indépendante les points pour les prix sell, même si les calculs sont les mêmes,
		// car s'il y a une différence en nombres (quantité), les courbes seront décalées.
		// Mais à voir, j'ai normalement "patché". J'ai trop bidouillé et c'est trop brouillon pour le moment.
		if (type == Type.BAZAAR) {
			List<Point> pointsSell = new ArrayList<>();
			for (ItemPrice item : prices) {
				// Normalisation des coordonnées
				double timeRatio = (double) ChronoUnit.MILLIS.between(minTime, item.time())
						/ ChronoUnit.MILLIS.between(minTime, maxTime);
				double priceRatio = (item.sellPrice() - minPrice) / (maxPrice - minPrice);

				double pointX = x1 + timeRatio * (x2 - x1);
				double pointY = y2 - priceRatio * (y2 - y1); // Y inversé, car origine en haut à gauche
				pointsSell.add(new Point((int) Math.round(pointX), (int) Math.round(pointY)));
			}

			if (ConfigManager.getConfig().general.stonks.showGradientInGraphScreen) {
				//new Color(0x888313);
				int startColor = 0x888313;
				int endColor = 0x00888313;
				drawGradient(context, pointsSell, startColor, endColor, y2, true);
			}

			GuiRenderUtils.renderLinesFromPoints(context, pointsSell.toArray(new Point[0]), new Color(234, 214, 7), 2.5f);
		}

		drawLabels(context, x1, y1, x2, y2);
		drawHoveredItem(context, mouseX, mouseY);
	}

	private void drawLabels(DrawContext context, int x1, int y1, int x2, int y2) {
		int timeSteps = getGranularityStep();
		for (int i = 0; i <= timeSteps; i++) {
			// Calcul de l'instant correspondant au label
			Instant currentTime = minTime.plus(i * ChronoUnit.MILLIS.between(minTime, maxTime) / timeSteps, ChronoUnit.MILLIS);

			// Calcul de la position en X sur le graphique
			int labelX = (int) (x1 + i * (x2 - x1) / (double) timeSteps);

			String timeLabel = formatTimeLabel(currentTime);

			// -12 vers la gauche | y2 + 10
			context.drawTextWithShadow(textRenderer, Text.literal(timeLabel), labelX - 12, y2 + 20, Color.WHITE.getRGB());
		}

		int priceSteps = 5;
		for (int i = 0; i <= priceSteps; i++) {
			// Calcul du prix correspondant au label
			double currentPrice = minPrice + i * (maxPrice - minPrice) / priceSteps;

			// Calcul de la position en Y sur le graphique
			int labelY = (int) (y2 - i * (y2 - y1) / (double) priceSteps);

			String priceLabel = StonksUtils.SHORT_FLOAT_NUMBERS.format(currentPrice);

			context.drawTextWithShadow(textRenderer, Text.literal(priceLabel).formatted(Formatting.GOLD),
					x2 + 10, labelY, Color.WHITE.getRGB());
		}
	}

	@SuppressWarnings("ConstantConditions") // if (closestPoint != null) -> Aucune confiance
	private void drawHoveredItem(DrawContext context, int mouseX, int mouseY) {
		if (graphData == null || graphData.isEmpty() || graphData.size() < 3) {
			return;
		}

		int x1 = 20;
		int y1 = 20 + 32;
		int x2 = x1 + this.width / 2;
		int y2 = y1 + this.height / 2 + 31; // 32

		if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
			Point closestPoint = null;
			double minDistance = Double.MAX_VALUE;

			for (Point point : graphData.keySet()) {
				double distance = Math.abs(point.x() - mouseX);
				if (distance < minDistance) {
					minDistance = distance;
					closestPoint = point;
				}
			}

			if (closestPoint != null) {
				ItemPrice item = graphData.get(closestPoint);

				Text dateText = Text.literal(TimeUtils.formatInstant(item.time(), TimeUtils.DATE_TIME_FULL))
						.formatted(Formatting.AQUA);

				Text priceText;
				Text secondPriceText = null;
				if (type == Type.BAZAAR) {
					priceText = Text.literal("Bazaar Buy: ").formatted(Formatting.YELLOW)
							.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(item.buyPrice()))
									.formatted(Formatting.GOLD));
					secondPriceText = Text.literal("Bazaar Sell: ").formatted(Formatting.YELLOW)
							.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(item.sellPrice()))
									.formatted(Formatting.GOLD));
				} else {
					priceText = Text.literal("Price: ").formatted(Formatting.YELLOW)
							.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(item.buyPrice()))
									.formatted(Formatting.GOLD));
				}

				int textWidth = this.textRenderer.getWidth(dateText) + 10;
				int height = type == Type.BAZAAR && secondPriceText != null ? 34 : 24; // 24
				// Border
				context.drawBorder(mouseX + 7, mouseY - 13, textWidth, height, Color.WHITE.getRGB());
				// Date
				context.drawTextWithShadow(textRenderer, dateText, mouseX + 10, mouseY - 10, Color.WHITE.getRGB());
				// Price - Auction / 1ère ligne du Bazaar
				context.drawTextWithShadow(textRenderer, priceText, mouseX + 10, mouseY, Color.WHITE.getRGB());
				// Price - Deuxième ligne du Bazaar
				if (type == Type.BAZAAR && secondPriceText != null) {
					context.drawTextWithShadow(textRenderer, secondPriceText, mouseX + 10, mouseY + 10, Color.WHITE.getRGB());
				}

				// - 32 || +- 5 border haut/bas
				context.drawVerticalLine(mouseX, y1 - 5, y2 - 22 + 5, new Color(204, 2, 2).getRGB());
			}
		}
	}

	enum Type {
		AUCTION, BAZAAR
	}

	private int getGranularityStep() {
		return switch (granularity) {
			case HOUR -> 7;
			case DAY -> 5;
			case WEEK -> 7;
			case MONTH -> 5;
		};
	}

	private @NotNull String formatTimeLabel(Instant time) {
		ZoneId zoneId = ZoneId.systemDefault();
		return switch (granularity) { // ZoneOffset.UTC
			case HOUR -> time.atZone(zoneId).format(DateTimeFormatter.ofPattern("HH:mm")); // Minutes et secondes
			case DAY -> time.atZone(zoneId).format(DateTimeFormatter.ofPattern("HH:mm")); // Heures et minutes
			case WEEK -> time.atZone(zoneId).format(DateTimeFormatter.ofPattern("EEE"));   // Jours (lun, mar, ...)
			case MONTH -> time.atZone(zoneId).format(DateTimeFormatter.ofPattern("EEE dd")); // Jour > "lun, 22"
		};
	}

	private void drawGradient(
			DrawContext context,
			List<Point> pointsToRender,
			int startColor,
			int endColor,
			int y2,
			boolean withSellerPrice
	) {
		List<Point> gradientPoints = new ArrayList<>(pointsToRender);
		if (this.granularity == GraphDataFilter.Granularity.DAY) {
			gradientPoints = StonksUtils.reduceListToApproxSize(gradientPoints, 300);
		}

		renderGradient(context, gradientPoints, startColor, endColor, y2 + 10, withSellerPrice);
	}

	private void renderGradient(
			DrawContext context,
			@NotNull List<Point> pointsToRender,
			int colorStart,
			int colorEnd,
			int y2,
			boolean withSellerPrice
	) {
		int minY = pointsToRender.stream().mapToInt(Point::y).min().orElse(0);
		int maxY = pointsToRender.stream().mapToInt(Point::y).max().orElse(0);

		Set<Integer> seen = new HashSet<>();
		List<Point> generatedPoints = completePoints(pointsToRender);

		for (Point point : generatedPoints) {
			if (seen.contains(point.x())) {
				continue;
			}

			seen.add(point.x());

			if (point.y() >= minY) {
				float factor = (float) (point.y() - minY) / (maxY - minY);
				/*if (granularity == GraphDataFilter.Granularity.HOUR)
					factor = 1;

				if ((granularity == GraphDataFilter.Granularity.HOUR || granularity == GraphDataFilter.Granularity.DAY)
						&& withSellerPrice) {
					factor = 1;
				}*/

				Color color = ColorUtils.interpolatedColor(new Color(colorStart), new Color(colorEnd), factor);

				int startX = point.x();
				int startY = point.y();
				int endX = point.x() + 1;
				int endY = y2;

				if (withSellerPrice) {
					//startX = point.x() - 2;
					startY = point.y() - 1;
				}

				context.fillGradient(startX, startY, endX, endY, color.getRGB(), colorEnd);
				//context.fillGradient(point.x(), withSellerPrice ? point.y() - 1 : point.y(), point.x() + 1, y2, 0, color.getRGB(), colorEnd);
			}
		}
	}

	@Contract("_ -> param1")
	private @NotNull List<Point> completePoints(@NotNull List<Point> pointsToComplete) {
		List<Integer> missingX = new ArrayList<>();

		int startX = pointsToComplete.getFirst().x();
		int endX = pointsToComplete.getLast().x();
		for (int x = startX; x <= endX; x++) {
			if (getYForX(pointsToComplete, x) == null) { // Si pas de Y défini pour ce X
				missingX.add(x);
			}
		}

		// Calculer les Y pour les X manquants
		List<Point> newPoints = new ArrayList<>();
		for (int x : missingX) {
			int interpolatedY = interpolateY(pointsToComplete, x);
			newPoints.add(new Point(x, interpolatedY));
		}

		pointsToComplete.addAll(newPoints);
		pointsToComplete.sort(Comparator.comparingInt(Point::x));
		return pointsToComplete;
	}

	private int interpolateY(@NotNull List<Point> points, int x) {
		Point before = null;
		Point after = null;

		for (Point point : points) {
			if (point.x() < x) {
				before = point;
				// Continue de chercher parce que "before" doit être le dernier point connu avant x sur l'axe des X.
			} else if (point.x() > x) {
				after = point;
				break;
			}
		}

		if (before == null || after == null) { // Illegal state : Il manque un avant ou un après pour X = x
			return 0;
		}

		// Interpolation linéaire
		float slope = (float) (after.y() - before.y()) / (after.x() - before.x());
		return (int) (before.y() + slope * (x - before.x()));
	}

	private @Nullable Integer getYForX(@NotNull List<Point> points, int x) {
		for (Point point : points) {
			if (point.x() == x) {
				return point.y(); // Retourne Y si le X correspond
			}
		}

		return null;
	}
}
