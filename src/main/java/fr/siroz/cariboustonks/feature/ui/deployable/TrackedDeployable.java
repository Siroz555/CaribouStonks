package fr.siroz.cariboustonks.feature.ui.deployable;


import net.minecraft.entity.decoration.ArmorStandEntity;

class TrackedDeployable implements Comparable<TrackedDeployable> {
	private final ArmorStandEntity armorStand;
	private final Deployable deployable;
	private final long addedTime;
	private boolean active;

	TrackedDeployable(ArmorStandEntity armorStand, Deployable deployable) {
		this.armorStand = armorStand;
		this.deployable = deployable;
		this.addedTime = System.currentTimeMillis();
		this.active = true;
	}

	public ArmorStandEntity getArmorStand() {
		return armorStand;
	}

	public Deployable getDeployable() {
		return deployable;
	}

	public long getAddedTime() {
		return addedTime;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int compareTo(TrackedDeployable other) {
		int priorityCompare = Integer.compare(other.getDeployable().getPriority(), this.deployable.getPriority());
		if (priorityCompare != 0) return priorityCompare;

		return Long.compare(other.getAddedTime(), this.addedTime);
	}
}
