package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.util.colors.ColorUtils;
import net.minecraft.ChatFormatting;

import java.awt.Color;

public class ChatConfig {

    @SerialEntry
    public boolean copyChat = true;

	@SerialEntry
	public int chatHistoryLength = 555;

	@SerialEntry
	public ChatParty chatParty = new ChatParty();

	@SerialEntry
	public ChatGuild chatGuild = new ChatGuild();

	public static class ChatParty {

		@SerialEntry
		public boolean chatPartyColored = false;

		@SerialEntry
		public Color chatPartyColor = ColorUtils.getAwtColor(ChatFormatting.BLUE);
	}

	public static class ChatGuild {

		@SerialEntry
		public boolean chatGuildColored = false;

		@SerialEntry
		public Color chatGuildColor = ColorUtils.getAwtColor(ChatFormatting.DARK_GREEN);
	}
}
