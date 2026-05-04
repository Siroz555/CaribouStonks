package fr.siroz.cariboustonks.features.hunting;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.mod.ModDataSource;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarPriceType;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class HuntingBoxOverlayFeature extends Feature {
	private static final Pattern TITLE_PATTERN = Pattern.compile("^Hunting Box.*");
	private static final Pattern OWNED_PATTERN = Pattern.compile("Owned: ([\\d,]+) Shards?");
	private static final int PADDING_LEFT = 20;
	private static final int LEFT_TO_RIGHT = 18;
	private static final int START_Y = 20;
	private static final int LINE_HEIGHT = 16; // 12

	private final ModDataSource modDataSource;
	private final HypixelDataSource hypixelDataSource;

	private final List<Line> lines = new ArrayList<>();

	public HuntingBoxOverlayFeature() {
		this.hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();
		this.modDataSource = CaribouStonks.mod().getModDataSource();

		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			ScreenEvents.afterExtract(screen).register(this::render);
			ScreenEvents.remove(screen).register(_ -> this.lines.clear());
		});

		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(TITLE_PATTERN))
				.content(this::contentAnalyzer)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().hunting.huntingBoxOverlay.enabled;
	}

	@EventHandler(event = "ScreenEvents.afterRender")
	private void render(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
		if (lines.isEmpty()) return;

		guiGraphics.pose().pushMatrix();
		final float scale = this.config().hunting.huntingBoxOverlay.scale;
		guiGraphics.pose().scale(scale, scale);

		int y = START_Y;
		for (Line line : lines) {
			boolean leftToRightPadding = false;
			if (line.itemStack() != null) {
				guiGraphics.item(line.itemStack(), PADDING_LEFT, y);
				leftToRightPadding = true;
			}
			int xPadding = leftToRightPadding ? LEFT_TO_RIGHT : 0;
			guiGraphics.text(CLIENT.font, line.text(), PADDING_LEFT + xPadding, y + 2, Colors.WHITE.asInt());
			y += LINE_HEIGHT;
		}
		guiGraphics.pose().popMatrix();
	}

	private @NonNull List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
		List<Attribute> sortedAttributes = new ArrayList<>();
		boolean dirty = false;
		int failed = 0;
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack itemStack = entry.getValue();
			// Évite-les borders vu que c'est au centre du menu
			if (StonksUtils.isEdgeSlot(entry.getIntKey(), 6)) continue;

			Component name = itemStack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty());
			SkyBlockAttribute attribute = modDataSource.getAttributeByShardName(name.getString());
			// Non connu par le mod
			if (attribute != null) {
				// Connu par le mod, handle les infos
				int owned = extractOwned(itemStack);
				double price = getBazaarPrice(attribute.skyBlockApiId()) * owned;
				sortedAttributes.add(new Attribute(itemStack, name, price, owned, false));
			} else {
				// Évite d'avoir les slots vides d'une page
				if (!itemStack.isEmpty()) {
					failed++;
					sortedAttributes.add(new Attribute(itemStack, name, 0, 0, true));
				}
			}

			dirty = true;
		}

		if (dirty) {
			sortedAttributes.sort(Comparator.comparingDouble(Attribute::price).reversed());
			buildDisplayLines(sortedAttributes, failed);
			sortedAttributes.clear();
		}

		return Collections.emptyList();
	}

	private void buildDisplayLines(@NonNull List<Attribute> attributes, int failed) {
		lines.clear();
		// Pour limiter, même avec le scale de base, bah ça depend de l'écran forcément...
		int maxLines = this.config().hunting.huntingBoxOverlay.listSize;

		attributes.stream().limit(maxLines).forEach(attribute -> {
			Component text;
			if (attribute.unknown()) {
				text = Component.empty()
						.append(Component.literal(attribute.name().getString()).withStyle(ChatFormatting.RED))
						.append(Component.literal(" [!] Unknown data").withStyle(ChatFormatting.RED));
			} else {
				text = Component.empty()
						.append(attribute.name())
						.append(Component.literal(" x" + attribute.owned() + " ").withStyle(ChatFormatting.DARK_GRAY))
						.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(attribute.price())).withStyle(ChatFormatting.GOLD));
			}
			lines.add(new Line(attribute.itemStack(), text));
		});

		if (attributes.size() > maxLines) {
			lines.add(new Line(null, Component.literal(" … and " + (attributes.size() - maxLines) + " more").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		}

		double totalPrice = attributes.stream()
				.mapToDouble(Attribute::price)
				.sum();

		lines.add(new Line(null, Component.empty()));
		lines.add(new Line(null, Component.empty()
				.append(Component.literal("Total Page Value: ").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(totalPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_INTEGER_NUMBERS.format(totalPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
		));
		if (failed > 0) {
			lines.add(new Line(null, Component.literal(failed + " attribute unrecognized!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)));
		}
	}

	private double getBazaarPrice(@NonNull String skyBlockId) {
		boolean useBuyPrice = this.config().hunting.huntingBoxOverlay.priceType == BazaarPriceType.BUY;
		return hypixelDataSource.getBazaarItem(skyBlockId)
				.map(useBuyPrice ? BazaarItemAnalytics.BUY : BazaarItemAnalytics.SELL)
				.orElse(0.0);
	}

	private int extractOwned(@NonNull ItemStack itemStack) {
		Matcher ownedMatcher = ItemUtils.getLoreLineIfMatch(itemStack, OWNED_PATTERN);
		return ownedMatcher != null ? StonksUtils.toInt(ownedMatcher.group(1).replace(",", ""), 1) : 1;
	}

	private record Attribute(ItemStack itemStack, Component name, double price, int owned, boolean unknown) {
	}

	private record Line(ItemStack itemStack, Component text) {
	}
}
