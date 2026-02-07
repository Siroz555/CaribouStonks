package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ClientEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class PlotInfestedFeature extends Feature {

	private static final Pattern INFESTED_PLOTS_TABLIST_PATTERN = Pattern.compile("\\sPlots: (?<plots>.*)");

	private static final int PLOT_SIZE = 96;
	private static final int MIN_PLOT_Y = 66;
	private static final int MAX_PLOT_Y = 100;

	private static final List<List<Integer>> PLOTS_IDS = Arrays.asList(
			Arrays.asList(21, 13, 9, 14, 22),
			Arrays.asList(15, 5, 1, 6, 16),
			Arrays.asList(10, 2, 0, 3, 11),
			Arrays.asList(17, 7, 4, 8, 18),
			Arrays.asList(23, 19, 12, 20, 24)
	);

	private final List<Plot> plots = new ArrayList<>();
	private final List<Integer> infestedPlots = new ArrayList<>();

	public PlotInfestedFeature() {
		this.createPlots();
		ClientEvents.TAB_LIST_UPDATE_EVENT.register(this::onTabListUpdate);
		RenderEvents.WORLD_RENDER_EVENT.register(this::onWorldRender);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& this.config().farming.garden.highlightInfestedPlots;
	}

	@Override
	protected void onClientJoinServer() {
		infestedPlots.clear();
	}

	@EventHandler(event = "ClientEvents.TAB_LIST_UPDATE_EVENT")
	private void onTabListUpdate(@NonNull List<String> lines) {
		if (!isEnabled()) return;

		infestedPlots.clear();

		for (String line : lines) {
			Matcher matcher = INFESTED_PLOTS_TABLIST_PATTERN.matcher(line);
			if (!matcher.find()) continue;

			for (String plot : matcher.group("plots").split(", ")) {
				if (plot.isEmpty()) continue;

				try {
					int plotId = Integer.parseInt(plot);
					infestedPlots.add(plotId);
				} catch (NumberFormatException nfe) {
					if (DeveloperTools.isInDevelopment()) {
						CaribouStonks.LOGGER.error("{} Unable to parse plot from Tab List", getShortName(), nfe);
					}
				}
			}
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void onWorldRender(WorldRenderer renderer) {
		if (!isEnabled()) return;
		if (infestedPlots.isEmpty()) return;

		for (int id : infestedPlots) {
			Optional<Plot> plotOptional = getPlot(id);
			if (plotOptional.isEmpty()) {
				continue;
			}

			Plot plot = plotOptional.get();
			renderer.submitText(
					Component.literal("Plot").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
							.append(Component.literal(" " + plot.id()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)),
					plot.center().subtract(0, 40, 0),
					5F,
					true
			);

			renderer.submitCuboidOutline(plot.center(), 48, PLOT_SIZE, MIN_PLOT_Y, MAX_PLOT_Y, 1.5f, Colors.GREEN, Colors.YELLOW);
		}
	}

	private void createPlots() {
		for (int i = 0; i < PLOTS_IDS.size(); i++) {
			List<Integer> row = PLOTS_IDS.get(i);
			for (int x = 0; x < row.size(); x++) {
				int id = row.get(x);

				double minX = (x - 2) * PLOT_SIZE - 48;
				double minZ = (i - 2) * PLOT_SIZE - 48;
				double maxX = (x - 2) * PLOT_SIZE + 48;
				double maxZ = (i - 2) * PLOT_SIZE + 48;

				Vec3 center = AABB.encapsulatingFullBlocks(
						new BlockPos((int) minX, 0, (int) minZ),
						new BlockPos((int) maxX, 256, (int) maxZ)
				).getCenter();

				plots.add(new Plot(id, center));
			}
		}
	}

	@NonNull
	private Optional<Plot> getPlot(int id) {
		return plots.stream().filter(plot -> plot.id == id).findFirst();
	}

	private record Plot(int id, Vec3 center) {
	}
}
