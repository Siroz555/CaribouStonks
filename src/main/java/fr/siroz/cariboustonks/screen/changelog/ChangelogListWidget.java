package fr.siroz.cariboustonks.screen.changelog;

import fr.siroz.cariboustonks.core.mod.changelog.ChangelogEntry;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

class ChangelogListWidget extends ContainerObjectSelectionList<ChangelogListWidget.@NotNull LineEntry> {

	private static final int LINE_HEIGHT = 10;
	private static final int PADDING = 10;
	private static final Component SPACE = Component.literal(" ");
	private static final Component SEPARATOR = Component.literal("────────────────────────────────────").withStyle(ChatFormatting.DARK_GRAY);

	ChangelogListWidget(Minecraft client, @NotNull List<ChangelogEntry> changelogs, int width, int height, int y) {
		super(client, width, height, y, LINE_HEIGHT);

		// Au lieu de créer une Entry qui fait pour toute la version (features + improvements + fixes + backend),
		// une Entry pour chaque ligne de chaque version est add.
		// Cela évite qu'une Entry soit démesuré, compliqué à calculer sur les dimensions
		// et que le scroll fasse n'importe quoi, car c'est un enfer pour moi.
		for (ChangelogEntry entry : changelogs) {
			List<Component> lines = formatChangelogToTexts(entry, width - 2 * PADDING);
			for (Component text : lines) {
				addEntry(new LineEntry(text));
			}
			addEntry(new LineEntry(SPACE));
			addEntry(new LineEntry(SEPARATOR));
			addEntry(new LineEntry(SPACE));
		}
		addEntry(new LineEntry(SPACE));
	}

	@Override
	public int getRowLeft() {
		return 10;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() - 20;
	}

	@Override
	protected int scrollBarX() {
		return this.width - 10;
	}

	protected class LineEntry extends ContainerObjectSelectionList.Entry<@NotNull LineEntry> {
		private final Component text;

		LineEntry(Component text) {
			this.text = text;
		}

		@Override
		public @NotNull List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public @NotNull List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int drawX = this.getX() + CONTENT_PADDING;
			// Centre verticalement la ligne dans entryHeight si besoin
			int textHeight = minecraft.font.lineHeight;
			//int drawY = getContentY() + Math.max(0, (getContentHeight() - textHeight) / 2);
			int drawY = this.getY() + Math.max(0, (this.getHeight() - textHeight) / 2);
			guiGraphics.drawString(minecraft.font, text, drawX, drawY, Colors.WHITE.asInt());
		}
	}

	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	private @NotNull List<Component> formatChangelogToTexts(@NotNull ChangelogEntry entry, int maxWidth) {
		List<Component> out = new ArrayList<>();

		// Titre Version (+ date si présente)
		out.add(Component.literal("✨ Version " + entry.version).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		if (entry.date != null && !entry.date.isBlank()) {
			out.add(Component.literal("(" + entry.date + ")").withStyle(ChatFormatting.GRAY));
		}
		out.add(SPACE);

		// Important Notes
		if (!entry.notes.isEmpty()) {
			out.add(Component.literal("Important Notes:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
			for (String item : entry.notes) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Component.literal(l).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
				}
			}
			out.add(SPACE);
		}

		// Features
		if (!entry.feature.isEmpty()) {
			out.add(Component.literal("Features:").withColor(Colors.GREEN.asInt()).withStyle(ChatFormatting.BOLD));
			for (String item : entry.feature) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Component.literal(l).withColor(Colors.GREEN.asInt()));
				}
			}
			out.add(SPACE);
		}

		// Improvements
		if (!entry.improvement.isEmpty()) {
			out.add(Component.literal("Improvements:").withColor(Colors.AQUA.asInt()).withStyle(ChatFormatting.BOLD));
			for (String item : entry.improvement) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Component.literal(l).withColor(Colors.AQUA.asInt()));
				}
			}
			out.add(SPACE);
		}

		// Fixes
		if (!entry.fixed.isEmpty()) {
			out.add(Component.literal("Fixes:").withColor(Colors.YELLOW.asInt()).withStyle(ChatFormatting.BOLD));
			for (String item : entry.fixed) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Component.literal(l).withColor(Colors.YELLOW.asInt()));
				}
			}
			out.add(SPACE);
		}

		// Backend
		if (!entry.backend.isEmpty()) {
			out.add(Component.literal("Backend:").withColor(Colors.ORANGE.asInt()).withStyle(ChatFormatting.BOLD));
			for (String item : entry.backend) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Component.literal(l).withColor(Colors.ORANGE.asInt()));
				}
			}
			out.add(SPACE);
		}

		while (!out.isEmpty() && out.getFirst().getString().isBlank()) {
			out.removeFirst();
		}
		while (!out.isEmpty() && out.getLast().getString().isBlank()) {
			out.removeLast();
		}

		return out;
	}

	private @NotNull List<String> wrapText(@NotNull String text, int maxWidth) {
		List<String> lines = new ArrayList<>();
		String[] words = text.split(" ");
		StringBuilder current = new StringBuilder();

		for (String w : words) {
			String test = current.isEmpty() ? w : current + " " + w;
			if (minecraft.font.width(test) > maxWidth && !current.isEmpty()) {
				lines.add(current.toString());
				current = new StringBuilder("    ");
				current.append(w);
			} else {
				if (!current.isEmpty() && !current.toString().trim().isEmpty()) {
					current.append(" ");
				}
				current.append(w);
			}
		}

		if (!current.isEmpty()) {
			lines.add(current.toString());
		}

		return lines;
	}
}
