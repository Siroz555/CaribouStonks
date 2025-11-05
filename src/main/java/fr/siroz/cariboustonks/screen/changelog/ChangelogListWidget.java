package fr.siroz.cariboustonks.screen.changelog;

import fr.siroz.cariboustonks.core.changelog.ChangelogEntry;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

class ChangelogListWidget extends ElementListWidget<ChangelogListWidget.LineEntry> {

	private static final int LINE_HEIGHT = 10;
	private static final int PADDING = 10;
	private static final Text SPACE = Text.literal(" ");
	private static final Text SEPARATOR = Text.literal("────────────────────────────────────").formatted(Formatting.DARK_GRAY);

	ChangelogListWidget(MinecraftClient client, @NotNull List<ChangelogEntry> changelogs, int width, int height, int y) {
		super(client, width, height, y, LINE_HEIGHT);

		// Au lieu de créer une Entry qui fait pour toute la version (features + improvements + fixes + backend),
		// une Entry pour chaque ligne de chaque version est add.
		// Cela évite qu'une Entry soit démesuré, compliqué à calculer sur les dimensions
		// et que le scroll fasse n'importe quoi, car c'est un enfer pour moi.
		for (ChangelogEntry entry : changelogs) {
			List<Text> lines = formatChangelogToTexts(entry, width - 2 * PADDING);
			for (Text text : lines) {
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
	protected int getScrollbarX() {
		return this.width - 10;
	}

	protected class LineEntry extends ElementListWidget.Entry<LineEntry> {
		private final Text text;

		LineEntry(Text text) {
			this.text = text;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			int drawX = x + PADDING;
			// Centre verticalement la ligne dans entryHeight si besoin
			int textHeight = client.textRenderer.fontHeight;
			int drawY = y + Math.max(0, (entryHeight - textHeight) / 2);
			context.drawTextWithShadow(client.textRenderer, text, drawX, drawY, Colors.WHITE.asInt());
		}
	}

	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	private @NotNull List<Text> formatChangelogToTexts(@NotNull ChangelogEntry entry, int maxWidth) {
		List<Text> out = new ArrayList<>();

		// Titre Version (+ date si présente)
		out.add(Text.literal("✨ Version " + entry.version).formatted(Formatting.GOLD, Formatting.BOLD));
		if (entry.date != null && !entry.date.isBlank()) {
			out.add(Text.literal("(" + entry.date + ")").formatted(Formatting.GRAY));
		}
		out.add(SPACE);

		// Important Notes
		if (!entry.notes.isEmpty()) {
			out.add(Text.literal("Important Notes:").formatted(Formatting.RED, Formatting.BOLD));
			for (String item : entry.notes) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Text.literal(l).formatted(Formatting.RED, Formatting.BOLD));
				}
			}
			out.add(SPACE);
		}

		// Features
		if (!entry.feature.isEmpty()) {
			out.add(Text.literal("Features:").withColor(Colors.GREEN.asInt()).formatted(Formatting.BOLD));
			for (String item : entry.feature) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Text.literal(l).withColor(Colors.GREEN.asInt()));
				}
			}
			out.add(SPACE);
		}

		// Improvements
		if (!entry.improvement.isEmpty()) {
			out.add(Text.literal("Improvements:").withColor(Colors.AQUA.asInt()).formatted(Formatting.BOLD));
			for (String item : entry.improvement) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Text.literal(l).withColor(Colors.AQUA.asInt()));
				}
			}
			out.add(SPACE);
		}

		// Fixes
		if (!entry.fixed.isEmpty()) {
			out.add(Text.literal("Fixes:").withColor(Colors.YELLOW.asInt()).formatted(Formatting.BOLD));
			for (String item : entry.fixed) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Text.literal(l).withColor(Colors.YELLOW.asInt()));
				}
			}
			out.add(SPACE);
		}

		// Backend
		if (!entry.backend.isEmpty()) {
			out.add(Text.literal("Backend:").withColor(Colors.ORANGE.asInt()).formatted(Formatting.BOLD));
			for (String item : entry.backend) {
				for (String l : wrapText(" • " + item, maxWidth)) {
					out.add(Text.literal(l).withColor(Colors.ORANGE.asInt()));
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
			if (client.textRenderer.getWidth(test) > maxWidth && !current.isEmpty()) {
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
