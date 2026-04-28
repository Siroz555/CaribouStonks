package fr.siroz.cariboustonks.feature.ui;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabLine;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabWidget;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.MultiElementHud;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import it.unimi.dsi.fastutil.Pair;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TabListWidgetExtractorFeature extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("tab_widgets");

	private final Map<String, TabWidget> currentWidgets = new HashMap<>();
	private final List<WidgetEntry> widgetEntries;
	private final Set<String> predefinedNames;

	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	public TabListWidgetExtractorFeature() {
		this.widgetEntries = new LinkedList<>(List.of(
				new WidgetEntry("Bestiary", () -> ConfigManager.getConfig().uiAndVisuals.tabListWidget.bestiary),
				new WidgetEntry("Slayer", () -> ConfigManager.getConfig().uiAndVisuals.tabListWidget.slayer),
				new WidgetEntry("Pet", () -> ConfigManager.getConfig().uiAndVisuals.tabListWidget.pet),
				new WidgetEntry("Pickaxe Ability", () -> ConfigManager.getConfig().uiAndVisuals.tabListWidget.pickaxeAbility),
				new WidgetEntry("Pity", () -> ConfigManager.getConfig().uiAndVisuals.tabListWidget.pity),
				new WidgetEntry("Commissions", () -> ConfigManager.getConfig().uiAndVisuals.tabListWidget.commissions)
		));
		this.predefinedNames = widgetEntries.stream().map(WidgetEntry::name).collect(Collectors.toSet());

		TickScheduler.getInstance().runRepeating(this::onSecondPassed, 1, TimeUnit.SECONDS);
	}

	@Override
	public boolean isEnabled() {
		// Pas dans le Supplier du HudConfig car dupe, juste utilisé pour les updates
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.tabListWidget.hud.enabled;
	}

	@Override
	protected void onClientJoinServer() {
		currentWidgets.clear();
	}

	private void onSecondPassed() {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled()) return;

		// Widgets prédéfinis
		for (WidgetEntry entry : widgetEntries) {
			String widgetName = entry.name();
			if (entry.enabled().getAsBoolean()) {
				CaribouStonks.core()
						.getTabListManager()
						.get(widgetName)
						.ifPresent(widget -> currentWidgets.put(widgetName, widget));
			} else {
				currentWidgets.remove(widgetName);
			}
		}

		// Widgets custom
		Set<String> customNames = parseCustomWidgets(ConfigManager.getConfig().uiAndVisuals.tabListWidget.customWidgets);
		// Retirer ceux qui ne sont plus dans la config
		currentWidgets.keySet().removeIf(key -> !predefinedNames.contains(key) && !customNames.contains(key));

		if (!customNames.isEmpty()) {
			for (String name : customNames) {
				CaribouStonks.core()
						.getTabListManager()
						.get(name)
						.ifPresent(w -> currentWidgets.put(name, w)); // else? a voir pour le delete ?
			}
		}
	}

	private Set<String> parseCustomWidgets(String raw) {
		if (raw == null || raw.isBlank()) return Collections.emptySet();
		try {
			return Arrays.stream(raw.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.collect(Collectors.toCollection(LinkedHashSet::new)); // pas de doublons
		} catch (Throwable ignored) { // la syntaxe peut sauter si la chaine n'est pas correctement parsé par la config.
			return Collections.emptySet();
		}
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new MultiElementHud(
				() -> SkyBlockAPI.isOnSkyBlock() && !this.currentWidgets.isEmpty(),
				new HudElementTextBuilder()
						.append(Text.literal("--- Widgets Extractor ---"))
						.append(Text.literal("§9§lPickaxe Ability:"))
						.append(Text.literal(" §fPickobulus: §c14s"))
						.appendSpace()
						.append(Text.literal("§e§lBestiary:"))
						.append(Text.literal(" §fGhost 15: §b§lMAX"))
						.append(Text.literal(" §fLittlefoot 14: §b84/100"))
						.append(Text.literal("------"))
						.build(),
				this::getHudLines,
				ConfigManager.getConfig().uiAndVisuals.tabListWidget.hud,
				10,
				50
		);
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		if (currentWidgets.isEmpty()) {
			return hudBuilder.build();
		}

		for (TabWidget widget : currentWidgets.values()) {
			hudBuilder.appendLine(widget.getHeader().component());
			for (TabLine line : widget.getLines()) {
				hudBuilder.appendLine(line.component());
			}
			hudBuilder.appendLine(Text.empty());
		}

		return hudBuilder.build();
	}

	private record WidgetEntry(String name, BooleanSupplier enabled) {
	}
}
