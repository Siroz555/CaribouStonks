package fr.siroz.cariboustonks.core.data.hypixel;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class HypixelDataException extends RuntimeException {

	private final Text messageText;

	public HypixelDataException(@NotNull Text messageText) {
		super(messageText.getString());
		this.messageText = messageText;
	}

	public Text getMessageText() {
		return this.messageText;
	}
}
