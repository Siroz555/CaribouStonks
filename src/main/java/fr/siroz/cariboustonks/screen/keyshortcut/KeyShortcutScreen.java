package fr.siroz.cariboustonks.screen.keyshortcut;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcut;
import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcutFeature;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class KeyShortcutScreen extends CaribousStonksScreen {

	// NOTE-ME : Avoir une confirmation lors de la fermeture du screen, sauf pour le bouton Done, même
	// chose à faire pour les Waypoints -_-

	@Nullable
	private final Screen parent;
	private final KeyShortcutFeature keyShortcutFeature;
	protected Map<String, KeyShortcut> shortcuts;
	private KeyShortcutListWidget listWidget;
	private Button buttonDelete;
	private double scrollBackup;
	@Nullable
	private KeyShortcutListWidget.KeyShortcutEntry currentEntry = null;

	private KeyShortcutScreen(@Nullable Screen parent) {
		super(Component.literal("Key Shortcuts").withStyle(ChatFormatting.BOLD));
		this.parent = parent;
		this.keyShortcutFeature = CaribouStonks.features().getFeature(KeyShortcutFeature.class);
		this.shortcuts = new HashMap<>(this.keyShortcutFeature.getShortcutsCopy());
	}

	@Contract("_ -> new")
	public static @NotNull KeyShortcutScreen create(@Nullable Screen parent) {
		return new KeyShortcutScreen(parent);
	}

	@Override
	protected void onInit() {
		listWidget = new KeyShortcutListWidget(this.minecraft, this, this.width, this.height - 112, 40, 36);
		addRenderableWidget(listWidget);

		GridLayout grid = new GridLayout();
		grid.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);

		GridLayout.RowHelper adder = grid.createRowHelper(2);

		adder.addChild(Button.builder(Component.literal("New Shortcut"),
				b -> listWidget.createKeyShortcut()).build());

		buttonDelete = Button.builder(Component.translatable("selectServer.deleteButton"), b -> {
			if (listWidget.getSelected() instanceof KeyShortcutListWidget.KeyShortcutEntry entry) {
				scrollBackup = listWidget.scrollAmount();
				minecraft.setScreen(new ConfirmScreen(
						confirmation -> deleteEntry(confirmation, entry),
						Component.literal("Confirm?"),
						Component.empty(),
						Component.translatable("selectServer.deleteButton"),
						CommonComponents.GUI_CANCEL
				));
			}
		}).build();
		adder.addChild(buttonDelete);

		adder.addChild(Button.builder(CommonComponents.GUI_DONE, b -> {
			saveShortcuts();
			close();
		}).width(310).build(), 2);

		grid.arrangeElements();
		FrameLayout.centerInRectangle(grid, 0, this.height - 64, this.width, 64);
		grid.visitWidgets(this::addRenderableWidget);
		updateButtons();
	}

	@Override
	public void onRender(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.onRender(context, mouseX, mouseY, delta);
		context.drawCenteredString(this.font, this.title, this.width / 2, 16, Colors.WHITE.asInt());
	}

	@Override
	public boolean keyPressed(@NotNull KeyEvent input) {
		if (currentEntry != null) {
			int code = (input.input() == GLFW.GLFW_KEY_ESCAPE || input.input() == GLFW.GLFW_KEY_DELETE) ? -1 : input.input();
			currentEntry.setKeyCode(code);
			currentEntry = null;
			setFocused(null);
			return true;
		}

		return super.keyPressed(input);
	}

	@Override
	public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
		if (currentEntry != null) {
			currentEntry.setKeyCode(-2000 - click.button());
			currentEntry = null;
			return true;
		}

		return super.onMouseClicked(click, doubled);
	}

	@Override
	public void renderBackground(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		if (currentEntry != null) {
			context.fill(0, 0, width, height, 0x88000000);
		}
	}

	@Override
	public void close() {
		minecraft.setScreen(parent);
	}

	void updateButtons() {
		buttonDelete.active = listWidget.getSelected() instanceof KeyShortcutListWidget.KeyShortcutEntry;
	}

	void setCurrentEntry(KeyShortcutListWidget.KeyShortcutEntry entry) {
		currentEntry = entry;
		this.setFocused(null);
	}

	private void deleteEntry(boolean confirmed, KeyShortcutListWidget.KeyShortcutEntry entry) {
		if (confirmed) {
			listWidget.removeEntry(entry);
		}

		minecraft.setScreen(this);
		listWidget.setScrollAmount(scrollBackup);
	}

	private void saveShortcuts() {
		keyShortcutFeature.updateShortcuts(shortcuts);
		keyShortcutFeature.saveShortcuts(minecraft);
	}
}
