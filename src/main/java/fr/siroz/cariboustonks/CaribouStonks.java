package fr.siroz.cariboustonks;

import fr.siroz.cariboustonks.dungeons.DungeonManager;
import fr.siroz.cariboustonks.fishing.FishingManager;
import fr.siroz.cariboustonks.garden.GardenManager;
import fr.siroz.cariboustonks.nether.NetherManager;
import fr.siroz.cariboustonks.slayer.SlayerManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaribouStonks implements ModInitializer {
	private static CaribouStonks instance;
    public static final Logger LOGGER = LoggerFactory.getLogger("caribou-stonks");

	@Override
	public void onInitialize() {
		instance = this;

		new DungeonManager();
		new FishingManager();
		new GardenManager();
		new NetherManager();
		new SlayerManager();
	}

	public static CaribouStonks getInstance() {
		return instance;
	}
}