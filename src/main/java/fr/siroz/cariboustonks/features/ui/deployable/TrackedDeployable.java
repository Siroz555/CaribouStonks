package fr.siroz.cariboustonks.features.ui.deployable;

import net.minecraft.world.entity.decoration.ArmorStand;

class TrackedDeployable implements Comparable<TrackedDeployable> {
	private final ArmorStand armorStand;
	private final Deployable deployable;
	private final long addedTime;
	private boolean active;

	TrackedDeployable(ArmorStand armorStand, Deployable deployable) {
		this.armorStand = armorStand;
		this.deployable = deployable;
		this.addedTime = System.currentTimeMillis();
		this.active = true;
	}

	public ArmorStand getArmorStand() {
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
