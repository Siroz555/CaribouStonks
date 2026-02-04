package fr.siroz.cariboustonks.core.skyblock.data.hypixel;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class HypixelDataException extends RuntimeException {

	private final Component messageText;

	public HypixelDataException(@NotNull Component messageText) {
		super(messageText.getString());
		this.messageText = messageText;
	}

	public Component getMessageText() {
		return this.messageText;
	}
}
