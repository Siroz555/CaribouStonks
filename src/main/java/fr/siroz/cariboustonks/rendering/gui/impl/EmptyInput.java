package fr.siroz.cariboustonks.rendering.gui.impl;

import net.minecraft.client.input.InputWithModifiers;

public record EmptyInput() implements InputWithModifiers {

	public static final EmptyInput INSTANCE = new EmptyInput();

	@Override
	public int input() {
		return 0;
	}

	@Override
	public int modifiers() {
		return 0;
	}
}
