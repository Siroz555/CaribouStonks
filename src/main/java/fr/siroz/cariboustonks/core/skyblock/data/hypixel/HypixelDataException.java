package fr.siroz.cariboustonks.core.skyblock.data.hypixel;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class HypixelDataException extends RuntimeException {

	private final Component messageText;

	public HypixelDataException(@NonNull Component messageText) {
		super(messageText.getString());
		this.messageText = messageText;
	}

	public Component getMessageText() {
		return this.messageText;
	}
}
