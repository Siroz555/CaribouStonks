package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.HudEvents;
import fr.siroz.cariboustonks.event.InteractionEvents;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PestFinderFeature extends Feature {

	private static final Pattern INFESTED_PLOTS_TABLIST_PATTERN = Pattern.compile("\\sPlots: (?<plots>.*)");

	private final PestFinderRenderer pestFinderRenderer;
	private final PlotInfestedRenderer plotInfestedRenderer;
	private final ParticlePathPredictor predictor = new ParticlePathPredictor(3);

	private final List<Integer> infestedPlots = new ArrayList<>();
	private long lastUsedVacuum = 0;
	private Vec3d guessPosition = null;

	public PestFinderFeature() {
		this.pestFinderRenderer = new PestFinderRenderer(this);
		this.plotInfestedRenderer = new PlotInfestedRenderer(this);

		WorldEvents.JOIN.register(world -> this.reset());
		InteractionEvents.LEFT_CLICK_AIR.register(this::onLeftClickAir);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		HudEvents.TAB_LIST_UPDATE.register(this::onTabListUpdate);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& ConfigManager.getConfig().farming.garden.pestsLocator;
	}

	public Vec3d getGuessPosition() {
		return guessPosition;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean highlightInfestedPlots() {
		return ConfigManager.getConfig().farming.garden.highlightInfestedPlots;
	}

	public List<Integer> getInfestedPlots() {
		return infestedPlots;
	}

	@EventHandler(event = "WorldEvents.JOIN")
	private void reset() {
		infestedPlots.clear();
		predictor.reset();
		guessPosition = null;
		lastUsedVacuum = 0;
	}

	@EventHandler(event = "InteractionEvents.LEFT_CLICK_AIR")
	private void onLeftClickAir(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!isEnabled() || stack == null || stack.isEmpty()) {
			return;
		}

		if (SkyBlockAPI.getSkyBlockItemId(stack).contains("VACUUM")) {
			predictor.reset();
			guessPosition = null;
			lastUsedVacuum = System.currentTimeMillis();
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ParticleS2CPacket packet) {
		if (!isEnabled()) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUsedVacuum > 5000) {
			return;
		}

		if (ParticleTypes.ANGRY_VILLAGER.equals(packet.getParameters().getType())) {
			Vec3d pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
			handleParticle(pos);
		}
	}

	private void handleParticle(Vec3d position) {
		if (predictor.isEmpty()) {
			predictor.addPoint(position);
			return;
		}

		Vec3d lastPoint = predictor.getLastPoint();
		if (lastPoint == null) {
			return;
		}

		double dist = lastPoint.distanceTo(position);
		if (dist == 0.0D || dist > 3.0D) {
			return;
		}

		predictor.addPoint(position);

		Vec3d solved = predictor.solve();
		if (solved == null) {
			return;
		}

		guessPosition = solved;
	}

	@EventHandler(event = "TabListEvents.UPDATE")
	private void onTabListUpdate(@NotNull List<String> lines) {
		if (!isEnabled()) return;
		if (!highlightInfestedPlots()) return;

		infestedPlots.clear();

		for (String line : lines) {
			Matcher matcher = INFESTED_PLOTS_TABLIST_PATTERN.matcher(line);
			if (matcher.find()) {
				for (String plot : matcher.group("plots").split(", ")) {
					if (plot.isEmpty()) {
						continue;
					}

					try {
						int plotId = Integer.parseInt(plot);
						infestedPlots.add(plotId);
					} catch (NumberFormatException ignored) {
					}
				}
			}
		}
	}

	@EventHandler(event = "WorldRenderEvents.AFTER_TRANSLUCENT")
	private void render(WorldRenderContext context) {
		pestFinderRenderer.render(context);
		plotInfestedRenderer.render(context);
	}
}
