package fr.siroz.cariboustonks.core.mod.crash;

public enum CrashType {
	CORE("Core"),
	FEATURE("Feature"),
	MANAGER("Service"),
	HUD("Hud"),
	KEYBINDING("KeyBinding"),
	CONTAINER("Container"),
	SCREEN("Screen"),
	;

	private final String name;

	CrashType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
