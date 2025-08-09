package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.manager.slayer.SlayerTier;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import java.time.Duration;
import java.time.Instant;

class SlayerBossRun {

	private SlayerType slayerType;
	private SlayerTier slayerTier;
	private Instant questStart;
	private Instant bossSpawn;
	private Instant bossKill;
	private Double expReward;

	SlayerBossRun(SlayerType slayerType, SlayerTier slayerTier) {
		this.slayerType = slayerType;
		this.slayerTier = slayerTier;
	}

	public SlayerType getSlayerType() {
		return slayerType;
	}

	public SlayerTier getSlayerTier() {
		return slayerTier;
	}

	public Duration timeToSpawn() {
		if (questStart != null && bossSpawn != null) {
			return Duration.between(questStart, bossSpawn);
		}
		return null;
	}

	public Duration timeToKill() {
		if (bossSpawn != null && bossKill != null) {
			return Duration.between(bossSpawn, bossKill);
		}
		return null;
	}

	/**
	 * cycle = from quest start to boss kill (temps total pour faire 1 boss)
	 *
	 * @return cycle duration
	 */
	public Duration cycleDuration() {
		if (questStart != null && bossKill != null) {
			return Duration.between(questStart, bossKill);
		}
		return null;
	}

	public void setSlayerType(SlayerType slayerType) {
		this.slayerType = slayerType;
	}

	public void setSlayerTier(SlayerTier slayerTier) {
		this.slayerTier = slayerTier;
	}

	public void setQuestStart(Instant questStart) {
		this.questStart = questStart;
	}

	public void setBossSpawn(Instant bossSpawn) {
		this.bossSpawn = bossSpawn;
	}

	public void setBossKill(Instant bossKill) {
		this.bossKill = bossKill;
	}

	public Double getExpReward() {
		return expReward;
	}

	public void setExpReward(Double expReward) {
		this.expReward = expReward;
	}
}
