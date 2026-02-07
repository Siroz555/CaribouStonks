package fr.siroz.cariboustonks.screen.changelog;

import fr.siroz.cariboustonks.core.mod.changelog.ChangelogEntry;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ChangelogScreen extends CaribousStonksScreen {

	private final List<ChangelogEntry> changelogs;
	private final Runnable onCloseAction;

	private ChangelogScreen(@NonNull List<ChangelogEntry> changelogs, @NonNull Runnable onCloseAction) {
		super(Component.nullToEmpty("Changelog"));
		this.changelogs = changelogs;
		this.onCloseAction = onCloseAction;
	}

	public static @NonNull ChangelogScreen create(@NonNull List<ChangelogEntry> changelogs, @NonNull Runnable onCloseAction) {
		return new ChangelogScreen(changelogs, onCloseAction);
	}

	@Override
	protected void onInit() {
		this.addRenderableWidget(new ChangelogListWidget(this.minecraft, changelogs, this.width, this.height - 100, 40));
		this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.close())
				.bounds(this.width / 2 - 50, this.height - 30, 100, 20)
				.build());
	}

	@Override
	public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.onRender(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(font,
				Component.literal("✨ What's new since your last visit ✨").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
				this.width / 2, 10, Colors.WHITE.asInt());

		if (changelogs.isEmpty()) {
			guiGraphics.drawCenteredString(font,
					Component.literal("You are on the latest version!").withStyle(ChatFormatting.GREEN),
					this.width / 2, 50, Colors.WHITE.asInt());
			guiGraphics.drawCenteredString(font, Component.empty()
							.append(Component.literal("To see the latest changes, visit ").withStyle(ChatFormatting.GRAY))
							.append(Component.literal("https://modrinth.com/mod/cariboustonks").withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)),
					this.width / 2, 69, Colors.WHITE.asInt());
		}
	}

	@Override
	public void close() {
		if (!changelogs.isEmpty()) {
			onCloseAction.run();
		}

		this.minecraft.setScreen(null);
	}
}
