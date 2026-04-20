package fr.siroz.cariboustonks.core.skyblock.tablist;

import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ClientEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.NonNull;

/**
 * Manages the SkyBlock Player List "TabList"
 *
 * @see TabLine
 * @see TabWidget
 */
public final class TabListManager {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final long WORLD_CHANGE_THRESHOLD_MS = 3_500; // 3.5s

	/**
	 * Hypixel contrôle le slot de chaque entrée via le nom d'équipe (team.getName()).
	 * Après le tri par team → nom, les entrées sortent exactement dans l'ordre colonne-par-colonne
	 * (haut → bas, puis colonne suivante), comme à l'affichage coté client.
	 * Pas besoin d'un toColumnMajor qui est une double-transformation alors que le comparator fait le boulo.
	 * C'est en gros le Comparator qui est aussi présent dans la partie du renderer client.
	 */
	private static final Comparator<PlayerInfo> TAB_ORDER = Comparator
			.<PlayerInfo, Integer>comparing(p -> p.getGameMode() == GameType.SPECTATOR ? 1 : 0)
			.thenComparing(p -> p.getTeam() != null ? p.getTeam().getName() : "")
			.thenComparing(p -> p.getProfile().name(), String.CASE_INSENSITIVE_ORDER);

	private final List<String> stringTab = new ArrayList<>();
	private final List<TabWidget> widgets = new ArrayList<>();

	private long lastWorldChange = 0;

	public TabListManager() {
		ClientPlayConnectionEvents.JOIN.register((_c, _s, _m) -> this.lastWorldChange = System.currentTimeMillis());
		TickScheduler.getInstance().runRepeating(this::updateTabList, 1, TimeUnit.SECONDS);
	}

	/**
	 * Retrieves a {@link TabWidget} from the given name.
	 * <p>
	 * widgetName = "Jacob's Contest", TabWidget = "Jacob's Contest: 11m left"
	 *
	 * @param widgetName the widget name to find
	 * @return an Optional {@link TabWidget}
	 */
	public @NonNull Optional<TabWidget> get(@NonNull String widgetName) {
		return widgets.stream()
				.filter(widget -> widget.getName().equalsIgnoreCase(widgetName))
				.findFirst();
	}

	/**
	 * Returns a view of all {@link TabWidget}.
	 *
	 * @return a view of {@link TabWidget}
	 */
	public @NonNull List<TabWidget> getWidgetView() {
		return Collections.unmodifiableList(widgets);
	}

	/**
	 * Gets all lines from the {@link TabWidget} with the given name
	 *
	 * @param name the widget name
	 * @return a list of {@link String} or an empty list
	 */
	@SuppressWarnings("unused")
	public List<String> getLines(@NonNull String name) {
		return get(name)
				.map(widget -> widget.getLines().stream()
						.map(TabLine::text)
						.toList())
				.orElse(Collections.emptyList());
	}

	/**
	 * Retrieves a line from a {@link TabWidget} with the given keyword.
	 *
	 * @param widgetName the widget name
	 * @param keyword    the keyword
	 * @return an Optional {@link String}
	 */
	public Optional<String> findLine(@NonNull String widgetName, @NonNull String keyword) {
		return get(widgetName)
				.flatMap(w -> w.getLines().stream()
						.map(TabLine::text)
						.filter(text -> text.contains(keyword))
						.findFirst());
	}

	private void updateTabList() {
		try {
			// Toujours clear avant les checks
			stringTab.clear();
			widgets.clear();

			if (CLIENT.player == null || CLIENT.level == null || CLIENT.getConnection() == null) return;
			// SIROZ-NOTE: 80 lignes toujours présentes "normalement", mais je n'aime pas ce check
			//if (CLIENT.getConnection().getOnlinePlayers().size() < 80) return;
			// Cooldown de 3s entre chaque swap pour éviter les lags entre les transferts server
			if (System.currentTimeMillis() - lastWorldChange < WORLD_CHANGE_THRESHOLD_MS) return;
			if (!SkyBlockAPI.isOnSkyBlock()) return;

			List<String> stringLines = new ArrayList<>();
			List<TabLine> tabLines = new ArrayList<>();

			// Tri identique au renderer coté client
			List<PlayerInfo> playerList = new ArrayList<>(CLIENT.getConnection().getOnlinePlayers());
			playerList.sort(TAB_ORDER);
			for (PlayerInfo entry : playerList) {

				Component displayName = entry.getTabListDisplayName();
				if (displayName == null) {
					// Slot vide, mais il est gardé pour préserver la grille
					tabLines.add(TabLine.EMPTY);
					continue;
				}

				String text = displayName.getString();
				// Tout est gardé pour la grille, les "[" sont marqué comme vide pour que le parser les ignore
				if (text.startsWith("[")) {
					tabLines.add(TabLine.EMPTY);
				} else {
					tabLines.add(new TabLine(text, displayName));
					if (!text.isEmpty()) stringLines.add(text);
				}
			}

			stringTab.addAll(stringLines);
			// Hypixel SkyBlock Widgets "parser"
			widgets.addAll(extractWidgets(tabLines));
			// Les lines en String sont handle sous listener contrairement aux Widgets
			ClientEvents.TAB_LIST_UPDATE_EVENT.invoker().onUpdate(stringTab);
		} catch (Exception ignored) {
		}
	}

	private @NonNull List<TabWidget> extractWidgets(@NonNull List<TabLine> lines) {
		List<TabWidget> widgets = new ArrayList<>();
		TabWidget current = null;

		for (TabLine line : lines) {
			if (line.isEmpty()) {
				if (current != null) {
					widgets.add(current);
					current = null;
				}
				continue;
			}

			if (line.isWidgetHeader()) {
				if (current != null) widgets.add(current);
				current = new TabWidget(line);
			} else if (current != null) {
				current.addLine(line);
			}
		}

		if (current != null) widgets.add(current);
		return widgets;
	}
}
