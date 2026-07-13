package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ForagingConfig {

	@SerialEntry
	public boolean showTreeOverlayInfo = false;

	@SerialEntry
	public boolean hideTreeBreakAnimation = false;

	@SerialEntry
	public Safari safari = new Safari();

	public static class Safari {

		@SerialEntry
		public boolean floorDrops = true;
	}
}
