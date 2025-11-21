package fr.siroz.cariboustonks.manager.network;

import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.util.math.MathUtils;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

public final class NetworkManager implements Manager {

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private int lastParameterS2CPing;

	private long lastPingResult = 0;
	private long currentPingResult = 0;
	private long lastUpdateTime = 0;

	private final float[] tickRates = new float[20];
	private int nextIndex = 0;
	private long timeLastTimeUpdate = -1;
	private long timeGameJoined = 0;

	@ApiStatus.Internal
	public NetworkManager() {
		NetworkEvents.PING_RESULT.register(ping -> this.lastPingResult = ping);
		NetworkEvents.WORLD_TIME_UPDATE_PACKET.register(this::onWorldTimeUpdatePacket);
		NetworkEvents.GAME_JOIN_PACKET.register(this::onGameJoinPacket);
		TickScheduler.getInstance().runRepeating(this::sendPingPacket, 5, TimeUnit.SECONDS);
	}

	public long getPing() {
		long currentTime = System.currentTimeMillis();

		if (currentTime - lastUpdateTime >= 1500) {
			currentPingResult = lastPingResult;
			lastUpdateTime = currentTime;
		}

		return currentPingResult;
	}

	public float getTickRate() {
		if (CLIENT.level == null || CLIENT.player == null) return 0;
		if (System.currentTimeMillis() - timeGameJoined < 4000) return 20;

		int numTicks = 0;
		float sumTickRates = 0.0F;
		for (float tickRate : tickRates) {
			if (tickRate > 0) {
				sumTickRates += tickRate;
				numTicks++;
			}
		}

		return sumTickRates / numTicks;
	}

	@ApiStatus.Internal
	public void onServerTick(ClientboundPingPacket packet) {
		if (packet != null && packet.getId() != lastParameterS2CPing) {
			lastParameterS2CPing = packet.getId();
			NetworkEvents.SERVER_TICK.invoker().onServerTick();
		}
	}

	@EventHandler(event = "NetworkEvents.WORLD_TIME_UPDATE_PACKET")
	private void onWorldTimeUpdatePacket() {
		long now = System.currentTimeMillis();
		float timeElapsed = (now - timeLastTimeUpdate) / 1000.0F;
		tickRates[nextIndex] = MathUtils.clamp(20.0F / timeElapsed, 0.0F, 20.0F);
		nextIndex = (nextIndex + 1) % tickRates.length;
		timeLastTimeUpdate = now;
	}

	@EventHandler(event = "NetworkEvents.GAME_JOIN_PACKET")
	private void onGameJoinPacket() {
		Arrays.fill(tickRates, 0);
		nextIndex = 0;
		timeGameJoined = timeLastTimeUpdate = System.currentTimeMillis();
	}

	private void sendPingPacket() {
		if (CLIENT.player != null && CLIENT.level != null && CLIENT.getConnection() != null) {
			CLIENT.getConnection().send(new ServerboundPingRequestPacket(Util.getMillis()));
		}
	}
}
