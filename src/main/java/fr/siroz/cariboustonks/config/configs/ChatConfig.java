package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.util.ColorUtils;
import java.awt.Color;
import net.minecraft.network.chat.TextColor;

public class ChatConfig {

    @SerialEntry
    public boolean copyChat = false;

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
		public Color chatPartyColor = ColorUtils.getAwtColor(TextColor.BLUE);
	}

	public static class ChatGuild {

		@SerialEntry
		public boolean chatGuildColored = false;

		@SerialEntry
		public Color chatGuildColor = ColorUtils.getAwtColor(TextColor.DARK_GREEN);
	}
}
