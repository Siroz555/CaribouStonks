package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRenderer;
import fr.siroz.cariboustonks.util.shape.Cuboid;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

final class PlotInfestedRenderer implements WorldRenderer {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final int GREEN = Colors.GREEN.asInt();
    private static final int YELLOW = Colors.YELLOW.asInt();
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

	public @NotNull Optional<Plot> getPlot(int id) {
		return plots.stream().filter(plot -> plot.id == id).findFirst();
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

                Cuboid cuboid = new Cuboid(
                        new BlockPos((int) minX, 0, (int) minZ),
                        new BlockPos((int) maxX, 256, (int) maxZ));

                plots.add(new Plot(id, cuboid, cuboid.getCenter().toCenterPos()));
            }
        }
    }

    @Override
    public void render(WorldRenderContext context) {
        if (CLIENT.player == null || CLIENT.world == null) return;
        if (!pestFinderFeature.isEnabled()) return;
        if (!pestFinderFeature.highlightInfestedPlots()) return;
        if (pestFinderFeature.getInfestedPlots().isEmpty()) return;

        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        assert consumers != null;

        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.getDebugLineStrip(1.0F));

        MatrixStack matrices = context.matrixStack();
        assert matrices != null;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        for (int id : pestFinderFeature.getInfestedPlots()) {
            Optional<Plot> plotOptional = getPlot(id);
            if (plotOptional.isEmpty()) {
				continue;
			}

            Plot plot = plotOptional.get();

            WorldRenderUtils.renderText(
                    context,
                    Text.literal("Plot").formatted(Formatting.GREEN, Formatting.BOLD)
                            .append(Text.literal(" " + plot.id).formatted(Formatting.YELLOW, Formatting.BOLD)),
                    plot.center.subtract(0, 40, 0),
                    5F,
                    true
            );

            double chunkX = Math.floor((plot.center().x + 48) / PLOT_SIZE);
            double chunkZ = Math.floor((plot.center().z + 48) / PLOT_SIZE);

            float chunkMinX = (float) ((float) (chunkX * PLOT_SIZE - 48) - cameraX);
            float chunkMinZ = (float) ((float) (chunkZ * PLOT_SIZE - 48) - cameraZ);

            float y1 = (float) (MIN_PLOT_Y - cameraY);
            float y2 = (float) (MAX_PLOT_Y - cameraY);

            for (int i = 0; i <= PLOT_SIZE; i += PLOT_SIZE) {
                for (int j = 0; j <= PLOT_SIZE; j += PLOT_SIZE) {
                    vertexConsumer.vertex(matrix4f, chunkMinX + i, y1, chunkMinZ + j).color(1.0F, 0.0F, 0.0F, 0.0F);
                    vertexConsumer.vertex(matrix4f, chunkMinX + i, y1, chunkMinZ + j).color(1.0F, 0.0F, 0.0F, 0.5F);
                    vertexConsumer.vertex(matrix4f, chunkMinX + i, y2, chunkMinZ + j).color(1.0F, 0.0F, 0.0F, 0.5F);
                    vertexConsumer.vertex(matrix4f, chunkMinX + i, y2, chunkMinZ + j).color(1.0F, 0.0F, 0.0F, 0.0F);
                }
            }

            for (int i = MIN_PLOT_Y; i <= MAX_PLOT_Y + 1; i += 2) {
                float y = (float) ((double) i - cameraY);
                int color = i % 8 == 0 ? GREEN : YELLOW;
                vertexConsumer.vertex(matrix4f, chunkMinX, y, chunkMinZ).color(1.0F, 1.0F, 0.0F, 0.0F);
                vertexConsumer.vertex(matrix4f, chunkMinX, y, chunkMinZ).color(color);
                vertexConsumer.vertex(matrix4f, chunkMinX, y, chunkMinZ + PLOT_SIZE).color(color);
                vertexConsumer.vertex(matrix4f, chunkMinX + PLOT_SIZE, y, chunkMinZ + PLOT_SIZE).color(color);
                vertexConsumer.vertex(matrix4f, chunkMinX + PLOT_SIZE, y, chunkMinZ).color(color);
                vertexConsumer.vertex(matrix4f, chunkMinX, y, chunkMinZ).color(color);
                vertexConsumer.vertex(matrix4f, chunkMinX, y, chunkMinZ).color(1.0F, 1.0F, 0.0F, 0.0F);
            }

            //consumers.draw();
        }

        consumers.draw();
    }

    public record Plot(int id, Cuboid cuboid, Vec3d center) {
    }
}
