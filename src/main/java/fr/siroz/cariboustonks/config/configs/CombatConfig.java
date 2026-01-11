package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.manager.hud.HudConfig;

public class CombatConfig {

	@SerialEntry
	public CocoonedMob cocoonedMob = new CocoonedMob();

	@SerialEntry
	public LowHealthWarning lowHealthWarning = new LowHealthWarning();

	@SerialEntry
	public RagAxe ragAxe = new RagAxe();

	@SerialEntry
	public SecondLife secondLife = new SecondLife();

	@SerialEntry
	public WitherShield witherShield = new WitherShield();

	public static class CocoonedMob {

		@SerialEntry
		public boolean cocoonedWarning = false;

		@SerialEntry
		public boolean cocoonedWarningTitle = true;

		@SerialEntry
		public boolean cocoonedWarningSound = true;

		@SerialEntry
		public boolean cocoonedWarningBeam = false;
	}

	public static class LowHealthWarning {

		@SerialEntry
		public boolean lowHealthWarningEnabled = true;

		@SerialEntry
		public int lowHealthWarningThreshold = 15;

		@SerialEntry
		public double lowHealthWarningIntensity = 0.69d;
	}

	public static class RagAxe {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public String message = "§cCasted!";

		@SerialEntry
		public RagAxeHud hud = new RagAxeHud();

		public static class RagAxeHud implements HudConfig {

			@SerialEntry
			public boolean enabled = false;

			@SerialEntry
			public int x = 200;

			@SerialEntry
			public int y = 10;

			@SerialEntry
			public float scale = 1f;

			@Override
			public int x() {
				return this.x;
			}

			@Override
			public void setX(int x) {
				this.x = x;
			}

			@Override
			public int y() {
				return this.y;
			}

			@Override
			public void setY(int y) {
				this.y = y;
			}

			@Override
			public float scale() {
				return this.scale;
			}

			@Override
			public void setScale(float scale) {
				this.scale = scale;
			}

			@Override
			public boolean shouldRender() {
				return this.enabled;
			}
		}
	}

	public static class SecondLife {

		@SerialEntry
		public CooldownHud cooldownHud = new CooldownHud();

		@SerialEntry
		public boolean spiritMaskUsed = true;

		@SerialEntry
		public boolean spiritMaskBack = true;

		@SerialEntry
		public boolean bonzoMaskUsed = false;

		@SerialEntry
		public boolean bonzoMaskBack = false;

		@SerialEntry
		public boolean phoenixUsed = false;

		@SerialEntry
		public boolean phoenixBack = false;

		public static class CooldownHud implements HudConfig {

			@SerialEntry
			public boolean enabled = false;

			@SerialEntry
			public int x = 150;

			@SerialEntry
			public int y = 50;

			@SerialEntry
			public float scale = 1f;

			@Override
			public int x() {
				return this.x;
			}

			@Override
			public void setX(int x) {
				this.x = x;
			}

			@Override
			public int y() {
				return this.y;
			}

			@Override
			public void setY(int y) {
				this.y = y;
			}

			@Override
			public float scale() {
				return this.scale;
			}

			@Override
			public void setScale(float scale) {
				this.scale = scale;
			}

			@Override
			public boolean shouldRender() {
				return this.enabled;
			}
		}
	}

	public static class WitherShield {

		@SerialEntry
		public WitherShieldHud hud = new WitherShieldHud();

		public static class WitherShieldHud implements HudConfig {

			@SerialEntry
			public boolean enabled = false;

			@SerialEntry
			public int x = 50;

			@SerialEntry
			public int y = 100;

			@SerialEntry
			public float scale = 1f;

			@Override
			public int x() {
				return this.x;
			}

			@Override
			public void setX(int x) {
				this.x = x;
			}

			@Override
			public int y() {
				return this.y;
			}

			@Override
			public void setY(int y) {
				this.y = y;
			}

			@Override
			public float scale() {
				return this.scale;
			}

			@Override
			public void setScale(float scale) {
				this.scale = scale;
			}

			@Override
			public boolean shouldRender() {
				return this.enabled;
			}
		}
	}
}
