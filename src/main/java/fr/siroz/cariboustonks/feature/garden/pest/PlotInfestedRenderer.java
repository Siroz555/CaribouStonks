package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

final class PlotInfestedRenderer {

	private static final Color GREEN = Colors.GREEN;
	private static final Color YELLOW = Colors.YELLOW;
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

	private final PestFinderFeature pestFinderFeature;
	private final List<Plot> plots = new ArrayList<>();

	PlotInfestedRenderer(PestFinderFeature pestFinderFeature) {
		this.pestFinderFeature = pestFinderFeature;
		createPlots();
	}

	private void createPlots() {
		for (int i = 0; i < PLOTS_IDS.size(); i++) {

			List<Integer> row = PLOTS_IDS.get(i);
			for (int x = 0; x < row.size(); x++) {
				int id = row.get(x);

				double minX = (x - 2) * 96 - 48;
				double minZ = (i - 2) * 96 - 48;
				double maxX = (x - 2) * 96 + 48;
				double maxZ = (i - 2) * 96 + 48;

				Vec3 center = AABB.encapsulatingFullBlocks(
						new BlockPos((int) minX, 0, (int) minZ),
						new BlockPos((int) maxX, 256, (int) maxZ)
				).getCenter();

				plots.add(new Plot(id, center));
			}
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!pestFinderFeature.isEnabled()) return;
		if (!pestFinderFeature.highlightInfestedPlots()) return;
		if (pestFinderFeature.getInfestedPlots().isEmpty()) return;

		for (int id : pestFinderFeature.getInfestedPlots()) {
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

			renderer.submitCuboidOutline(plot.center(), 48, PLOT_SIZE, MIN_PLOT_Y, MAX_PLOT_Y, 1.5f, GREEN, YELLOW);
		}
	}

	private @NotNull Optional<Plot> getPlot(int id) {
		return plots.stream().filter(plot -> plot.id == id).findFirst();
	}

	private record Plot(int id, Vec3 center) {
	}
}
