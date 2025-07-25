package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.feature.stonks.tooltips.auction.AuctionTooltipPriceType;
import fr.siroz.cariboustonks.feature.stonks.tooltips.bazaar.BazaarTooltipPriceType;
import fr.siroz.cariboustonks.feature.stonks.tooltips.TooltipPriceDisplayType;

public class GeneralConfig {

	@SerialEntry
	public boolean firstTimeWithTheMod = true;

	@SerialEntry
	public boolean checkForUpdates = true;

	@SerialEntry
	public Stonks stonks = new Stonks();

	@SerialEntry
	public Reminders reminders = new Reminders();

	public static class Stonks {

		@SerialEntry
		public boolean bazaarTooltipPrice = false;

		@SerialEntry
		public BazaarTooltipPriceType bazaarTooltipPriceType = BazaarTooltipPriceType.NORMAL;

		@SerialEntry
		public TooltipPriceDisplayType bazaarTooltipPriceDisplayType = TooltipPriceDisplayType.FULL;

		@SerialEntry
		public boolean auctionTooltipPrice = false;

		@SerialEntry
		public AuctionTooltipPriceType auctionTooltipPriceType = AuctionTooltipPriceType.LOWEST_BIN;

		@SerialEntry
		public TooltipPriceDisplayType auctionTooltipPriceDisplayType = TooltipPriceDisplayType.FULL;

		@SerialEntry
		public boolean bazaarOrderTracker = false;

		@SerialEntry
		public boolean bazaarSignEditEnterValidation = false;

		@SerialEntry
		public boolean showGradientInGraphScreen = false;

		@SerialEntry
		public boolean showAllDataInInfoScreen = false;

		@SerialEntry
		public boolean showAllDataInStonksCommand = false;
	}

	public static class Reminders {

		@SerialEntry
		public boolean boosterCookie = true;

		@SerialEntry
		public boolean chocolateFactoryMaxChocolates = true;

		@SerialEntry
		public boolean ubikCube = true;

		@SerialEntry
		public boolean enchantedCloak = true;

		@SerialEntry
		public boolean forge = true;
	}
}
