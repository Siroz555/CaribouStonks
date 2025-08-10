package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.entity.LivingEntity;

public class VanillaConfig {

	@SerialEntry
	public boolean hideWorldLoadingScreen = true;

	@SerialEntry
	public boolean hideTutorialsToast = true;

	@SerialEntry
	public boolean stopCursorResetPosition = false;

	@SerialEntry
	public boolean displayOwnNametagUsername = false;

	@SerialEntry
	public Overlay overlay = new Overlay();

	@SerialEntry
	public Mob mob = new Mob();

	@SerialEntry
	public ItemModelCustomization itemModelCustomization = new ItemModelCustomization();

	@SerialEntry
	public Sound sound = new Sound();

	public static class Overlay {

		@SerialEntry
		public boolean hideFireOverlay = false;

		@SerialEntry
		public boolean hideArmorOverlay = false;

		@SerialEntry
		public boolean hideFoodOverlay = false;
	}

	public static class ItemModelCustomization {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public int swingDuration = LivingEntity.GLOWING_FLAG; // 6

		@SerialEntry
		public boolean ignoreMiningEffects = false;

		@SerialEntry
		public CustomHand mainHand = new CustomHand();

		@SerialEntry
		public CustomHand offHand = new CustomHand();

		public static class CustomHand {

			@SerialEntry
			public float scale = 1f;

			@SerialEntry
			public float x = 0f;

			@SerialEntry
			public float y = 0f;

			@SerialEntry
			public float z = 0f;

			@SerialEntry
			public float xRotation = 0f;

			@SerialEntry
			public float yRotation = 0f;

			@SerialEntry
			public float zRotation = 0f;
		}
	}

	public static class Sound {

		@SerialEntry
		public boolean muteEnderman = false;

		@SerialEntry
		public boolean mutePhantom = false;
	}

	public static class Mob {

		@SerialEntry
		public boolean hideFireOnEntities = false;
	}
}
