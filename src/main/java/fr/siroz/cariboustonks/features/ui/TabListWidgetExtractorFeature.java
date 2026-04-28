package fr.siroz.cariboustonks.features.ui;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabLine;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabWidget;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;

// SIROZ-NOTE: Temp pour le moment, un Screen custom pour choisir et trier.
//  Avoir des options par default mais aussi custom, avec option d'Islands.
//  Mettre un système de priorité si un trop gros nombre de Widget sont présents.
@Experimental("Screen Configuration Missing")
public class TabListWidgetExtractorFeature extends Feature {

	private final Map<String, TabWidget> currentWidgets = new HashMap<>();
	private final List<WidgetEntry> widgetEntries;
	private final Set<String> predefinedNames;

	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	public TabListWidgetExtractorFeature() {
		this.widgetEntries = new LinkedList<>(List.of(
				new WidgetEntry("Bestiary", () -> this.config().uiAndVisuals.tabListWidget.bestiary),
				new WidgetEntry("Slayer", () -> this.config().uiAndVisuals.tabListWidget.slayer),
				new WidgetEntry("Pet", () -> this.config().uiAndVisuals.tabListWidget.pet),
				new WidgetEntry("Pickaxe Ability", () -> this.config().uiAndVisuals.tabListWidget.pickaxeAbility),
				new WidgetEntry("Pity", () -> this.config().uiAndVisuals.tabListWidget.pity),
				new WidgetEntry("Commissions", () -> this.config().uiAndVisuals.tabListWidget.commissions)
		));
		this.predefinedNames = widgetEntries.stream().map(WidgetEntry::name).collect(Collectors.toSet());

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("tab_widgets"))
				.hud(new MultiElementHud(
						() -> SkyBlockAPI.isOnSkyBlock() && !this.currentWidgets.isEmpty(),
						new HudElementTextBuilder()
								.append(Component.literal("--- Widgets Extractor ---"))
								.append(Component.literal("§9§lPickaxe Ability:"))
								.append(Component.literal(" §fPickobulus: §c14s"))
								.appendSpace()
								.append(Component.literal("§e§lBestiary:"))
								.append(Component.literal(" §fGhost 15: §b§lMAX"))
								.append(Component.literal(" §fLittlefoot 14: §b84/100"))
								.build(),
						this::getHudLines,
						this.config().uiAndVisuals.tabListWidget.hud,
						10,
						50
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		// Pas dans le Supplier du HudConfig car dupe, juste utilisé pour les updates
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.tabListWidget.hud.enabled;
	}

	@Override
	protected void onClientJoinServer() {
		currentWidgets.clear();
	}

	@Override
	protected void onSecondPassed() {
		if (CLIENT.player == null || CLIENT.level == null) return;
		if (!isEnabled()) return;

		// Widgets prédéfinis
		for (WidgetEntry entry : widgetEntries) {
			String widgetName = entry.name();
			if (entry.enabled().getAsBoolean()) {
				CaribouStonks.skyBlock()
						.getTabListManager()
						.get(widgetName)
						.ifPresent(widget -> currentWidgets.put(widgetName, widget));
			} else {
				currentWidgets.remove(widgetName);
			}
		}

		// Widgets custom
		Set<String> customNames = parseCustomWidgets(this.config().uiAndVisuals.tabListWidget.customWidgets);
		// Retirer ceux qui ne sont plus dans la config
		currentWidgets.keySet().removeIf(key -> !predefinedNames.contains(key) && !customNames.contains(key));

		if (!customNames.isEmpty()) {
			for (String name : customNames) {
				CaribouStonks.skyBlock()
						.getTabListManager()
						.get(name)
						.ifPresent(widget -> currentWidgets.put(name, widget));
				// SIROZ-NOTE: ifPresentOrElse, a voir pour le delete ?
			}
		}
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
			hudBuilder.appendLine(Component.empty());
		}

		return hudBuilder.build();
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

	private record WidgetEntry(String name, BooleanSupplier enabled) {
	}
}
