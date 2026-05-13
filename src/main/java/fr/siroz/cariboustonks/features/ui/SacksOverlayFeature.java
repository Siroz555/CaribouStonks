package fr.siroz.cariboustonks.features.ui;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarItemAnalytics;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SacksOverlayFeature extends Feature {

	// TODO
	//  - config
	//  - gemstone sack handler
	//  - missing skyBlockApiId
	//  - failed items
	//  - sort system
	//  - NPC price ?
	//  - failed stored pattern parsing

	private static final Pattern TITLE_PATTERN = Pattern.compile("^(?:.* Sack|Enchanted .* Sack)$");
	private static final Pattern STORED_PATTERN = Pattern.compile("Stored: (?<stored>[0-9.,kKmMbB]+)/(?<total>\\d+(?:[0-9.,]+)?[kKmMbB]?)");
	private static final Set<String> BLACKLISTED_ITEM_NAMES = Set.of(
			"go back", "next page",
			"pickup all",
			"filter", "item tier",
			"rune type filter"
	);
	private static final int PADDING_LEFT = 10;
	private static final int LEFT_TO_RIGHT = 18;
	private static final int START_Y = 20;
	private static final int LINE_HEIGHT = 16;

	private final HypixelDataSource hypixelDataSource;
	private final List<Line> lines = new ArrayList<>();

	public SacksOverlayFeature() {
		this.hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();

		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			ScreenEvents.afterExtract(screen).register(this::render);
			ScreenEvents.remove(screen).register(_ -> this.lines.clear());
		});

		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(TITLE_PATTERN))
				.content(this::contentAnalyzer)
				.onReset(this.lines::clear)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
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
			guiGraphics.text(ClientContext.getFont(), line.text(), PADDING_LEFT + xPadding, y + 2, Colors.WHITE.asInt());
			y += LINE_HEIGHT;
		}
		guiGraphics.pose().popMatrix();
	}

	private @NonNull List<ColorHighlight> contentAnalyzer(@NonNull Int2ObjectMap<ItemStack> slots) {
		List<Item> items = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack itemStack = entry.getValue();
			Component name = itemStack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty());
			if (isBlacklisted(itemStack, name)) continue;

			String skyBlockId = SkyBlockAPI.getSkyBlockApiId(itemStack);
			if (skyBlockId.isEmpty()) continue;

			StoredInfo info = extractStored(itemStack);
			if (info == null) continue;

			double unitPrice = getBazaarPrice(skyBlockId);
			double value = unitPrice * info.storedAmount();

			items.add(new Item(itemStack, name, value, info));
		}
		items.sort(Comparator.comparingDouble(Item::value).reversed());
		buildDisplayLines(items);
		items.clear();

		return Collections.emptyList();
	}

	private void buildDisplayLines(List<Item> items) {
		lines.clear();

		int maxLines = 10;

		items.stream().limit(maxLines).forEach(item -> {
			Component text = Component.empty()
					.append(item.name())
					.append(Component.literal(" " + StonksUtils.SHORT_INTEGER_NUMBERS.format(item.value())).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" " + item.info().storedValue()).withStyle(ChatFormatting.YELLOW))
					.append(Component.literal("/" + item.info().totalValue()).withStyle(ChatFormatting.GRAY));
			lines.add(new Line(item.itemStack(), text));
		});

		if (items.size() > maxLines) {
			lines.add(new Line(null, Component.literal(" … and " + (items.size() - maxLines) + " more").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
		}

		double totalPrice = items.stream()
				.mapToDouble(Item::value)
				.sum();

		lines.add(new Line(null, Component.empty()));
		lines.add(new Line(null, Component.empty()
				.append(Component.literal("Sack Value: ").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(totalPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(StonksUtils.SHORT_INTEGER_NUMBERS.format(totalPrice)).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
		));
	}

	private boolean isBlacklisted(@NonNull ItemStack itemStack, @NonNull Component itemName) {
		String name = itemName.getString();
		if (name.isBlank()) return true;

		// Les bordures ou les espaces sans items
		if (itemStack.is(Items.BLACK_STAINED_GLASS_PANE)) return true;

		// Les items de navigation et de triage
		return BLACKLISTED_ITEM_NAMES.contains(name.toLowerCase(Locale.ENGLISH));
	}

	private double getBazaarPrice(@NonNull String skyBlockId) {
		return hypixelDataSource.getBazaarItem(skyBlockId)
				.map(BazaarItemAnalytics.BUY)
				.orElse(0.0);
	}

	private @Nullable StoredInfo extractStored(ItemStack itemStack) {
		Matcher storedMatcher = ItemUtils.getLoreLineIfMatch(itemStack, STORED_PATTERN);
		if (storedMatcher == null) return null;

		try {
			String storedValue = storedMatcher.group("stored");
			long storedAmount = StonksUtils.parseAmount(storedValue);

			String totalValue = storedMatcher.group("total");
			long totalAmount = StonksUtils.parseAmount(totalValue);

			return new StoredInfo(storedValue, storedAmount, totalValue, totalAmount);
		} catch (Exception _) {
			return null;
		}
	}

	private record Line(ItemStack itemStack, Component text) {
	}

	private record Item(ItemStack itemStack, Component name, double value, StoredInfo info) {
	}

	private record StoredInfo(String storedValue, long storedAmount, String totalValue, long totalAmount) {
	}
}
