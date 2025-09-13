package fr.siroz.cariboustonks.screen.changelog;

import fr.siroz.cariboustonks.core.changelog.ChangelogEntry;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChangelogScreen extends CaribousStonksScreen {

	private final List<ChangelogEntry> changelogs;
	private final Runnable onCloseAction;

	private ChangelogScreen(@NotNull List<ChangelogEntry> changelogs, @NotNull Runnable onCloseAction) {
		super(Text.of("Changelog"));
		this.changelogs = changelogs;
		this.onCloseAction = onCloseAction;
	}

	@Contract("_, _ -> new")
	public static @NotNull ChangelogScreen create(@NotNull List<ChangelogEntry> changelogs, @NotNull Runnable onCloseAction) {
		return new ChangelogScreen(changelogs, onCloseAction);
	}

	@Override
	protected void onInit() {
		this.addDrawableChild(new ChangelogListWidget(this.client, changelogs, this.width, this.height - 100, 40));
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.onClose())
				.dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
				.build());
	}

	@Override
	public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
		super.onRender(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer,
				Text.literal("✨ What's new since your last visit ✨").formatted(Formatting.BOLD, Formatting.GOLD),
				this.width / 2, 10, Colors.WHITE.asInt());

		if (changelogs.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer,
					Text.literal("You are on the latest version!").formatted(Formatting.GREEN),
					this.width / 2, 50, Colors.WHITE.asInt());
			context.drawCenteredTextWithShadow(textRenderer, Text.empty()
							.append(Text.literal("To see the latest changes, visit ").formatted(Formatting.GRAY))
							.append(Text.literal("https://modrinth.com/mod/cariboustonks").formatted(Formatting.AQUA, Formatting.UNDERLINE)),
					this.width / 2, 69, Colors.WHITE.asInt());
		}
	}

	@Override
	public void onClose() {
		if (this.client == null) return;

		if (!changelogs.isEmpty()) {
			onCloseAction.run();
		}

		this.client.setScreen(null);
	}
}
