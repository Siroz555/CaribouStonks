package fr.siroz.cariboustonks.screen.keyshortcut;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcut;
import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcutFeature;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
	private ButtonWidget buttonDelete;
	private double scrollBackup;
	@Nullable
	private KeyShortcutListWidget.KeyShortcutEntry currentEntry = null;

	private KeyShortcutScreen(@Nullable Screen parent) {
		super(Text.literal("Key Shortcuts").formatted(Formatting.BOLD));
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
		listWidget = new KeyShortcutListWidget(this.client, this, this.width, this.height - 112, 40, 36);
		addDrawableChild(listWidget);

		GridWidget grid = new GridWidget();
		grid.getMainPositioner().marginX(5).marginY(2);

		GridWidget.Adder adder = grid.createAdder(2);

		adder.add(ButtonWidget.builder(Text.literal("New Shortcut"),
				b -> listWidget.createKeyShortcut()).build());

		buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), b -> {
			if (client != null && listWidget.getSelectedOrNull() instanceof KeyShortcutListWidget.KeyShortcutEntry entry) {
				scrollBackup = listWidget.getScrollY();
				client.setScreen(new ConfirmScreen(
						confirmation -> deleteEntry(confirmation, entry),
						Text.literal("Confirm?"),
						Text.empty(),
						Text.translatable("selectServer.deleteButton"),
						ScreenTexts.CANCEL
				));
			}
		}).build();
		adder.add(buttonDelete);

		adder.add(ButtonWidget.builder(ScreenTexts.DONE, b -> {
			saveShortcuts();
			onClose();
		}).width(310).build(), 2);

		grid.refreshPositions();
		SimplePositioningWidget.setPos(grid, 0, this.height - 64, this.width, 64);
		grid.forEachChild(this::addDrawableChild);
		updateButtons();
	}

	@Override
	public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
		super.onRender(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, Colors.WHITE.asInt());
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (currentEntry != null) {
			int code = (input.getKeycode() == GLFW.GLFW_KEY_ESCAPE || input.getKeycode() == GLFW.GLFW_KEY_DELETE) ? -1 : input.getKeycode();
			currentEntry.setKeyCode(code);
			currentEntry = null;
			setFocused(null);
			return true;
		}

		return super.keyPressed(input);
	}

	@Override
	public boolean onMouseClicked(Click click, boolean doubled) {
		if (currentEntry != null) {
			currentEntry.setKeyCode(-2000 - click.button());
			currentEntry = null;
			return true;
		}

		return super.onMouseClicked(click, doubled);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		if (currentEntry != null) {
			context.fill(0, 0, width, height, 0x88000000);
		}
	}

	@Override
	public void onClose() {
		assert client != null;
		client.setScreen(parent);
	}

	void updateButtons() {
		buttonDelete.active = listWidget.getSelectedOrNull() instanceof KeyShortcutListWidget.KeyShortcutEntry;
	}

	void setCurrentEntry(KeyShortcutListWidget.KeyShortcutEntry entry) {
		currentEntry = entry;
		this.setFocused(null);
	}

	private void deleteEntry(boolean confirmed, KeyShortcutListWidget.KeyShortcutEntry entry) {
		if (confirmed) {
			listWidget.removeEntry(entry);
		}

		if (client != null) {
			client.setScreen(this);
		}

		listWidget.setScrollY(scrollBackup);
	}

	private void saveShortcuts() {
		keyShortcutFeature.updateShortcuts(shortcuts);
		keyShortcutFeature.saveShortcuts(client);
	}
}
