package fr.siroz.cariboustonks.feature.garden.pest;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.HudEvents;
import fr.siroz.cariboustonks.event.InteractionEvents;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PestFinderFeature extends Feature {

	private static final Pattern INFESTED_PLOTS_TABLIST_PATTERN = Pattern.compile("\\sPlots: (?<plots>.*)");

	private final ParticlePathPredictor predictor = new ParticlePathPredictor(3);

	private final List<Integer> infestedPlots = new ArrayList<>();
	private long lastUsedVacuum = 0;
	private Vec3 guessPosition = null;

	public PestFinderFeature() {
		PestFinderRenderer pestFinderRenderer = new PestFinderRenderer(this);
		PlotInfestedRenderer plotInfestedRenderer = new PlotInfestedRenderer(this);

		RenderEvents.WORLD_RENDER.register(renderer -> {
			pestFinderRenderer.render(renderer);
			plotInfestedRenderer.render(renderer);
		});

		InteractionEvents.LEFT_CLICK_AIR.register(this::onLeftClickAir);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		HudEvents.TAB_LIST_UPDATE.register(this::onTabListUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& ConfigManager.getConfig().farming.garden.pestsLocator;
	}

	@Override
	protected void onClientJoinServer() {
		infestedPlots.clear();
		predictor.reset();
		guessPosition = null;
		lastUsedVacuum = 0;
	}

	public Vec3 getGuessPosition() {
		return guessPosition;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean highlightInfestedPlots() {
		return ConfigManager.getConfig().farming.garden.highlightInfestedPlots;
	}

	public List<Integer> getInfestedPlots() {
		return infestedPlots;
	}

	@EventHandler(event = "InteractionEvents.LEFT_CLICK_AIR")
	private void onLeftClickAir(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!isEnabled() || stack.isEmpty()) {
			return;
		}

		if (SkyBlockAPI.getSkyBlockItemId(stack).contains("VACUUM")) {
			predictor.reset();
			guessPosition = null;
			lastUsedVacuum = System.currentTimeMillis();
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket packet) {
		if (!isEnabled()) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUsedVacuum > 5000) {
			return;
		}

		if (ParticleTypes.ANGRY_VILLAGER.equals(packet.getParticle().getType())) {
			Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
			handleParticle(pos);
		}
	}

	private void handleParticle(Vec3 position) {
		if (predictor.isEmpty()) {
			predictor.addPoint(position);
			return;
		}

		Vec3 lastPoint = predictor.getLastPoint();
		if (lastPoint == null) {
			return;
		}

		double dist = lastPoint.distanceTo(position);
		if (dist == 0.0D || dist > 3.0D) {
			return;
		}

		predictor.addPoint(position);

		Vec3 solved = predictor.solve();
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
					} catch (NumberFormatException nfe) {
						if (DeveloperTools.isInDevelopment()) {
							CaribouStonks.LOGGER.error("{} Unable to parse plot from Tab List", getShortName(), nfe);
						}
					}
				}
			}
		}
	}
}
