package fr.siroz.cariboustonks.core.model;

public class MobTrackingModel {
	private String name;
	private boolean enabled;
	private  boolean notifyOnSpawn;

	public MobTrackingModel(String name, boolean enabled, boolean notifyOnSpawn) {
		this.name = name;
		this.enabled = enabled;
		this.notifyOnSpawn = notifyOnSpawn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isNotifyOnSpawn() {
		return notifyOnSpawn;
	}

	public void setNotifyOnSpawn(boolean notifyOnSpawn) {
		this.notifyOnSpawn = notifyOnSpawn;
	}
}
