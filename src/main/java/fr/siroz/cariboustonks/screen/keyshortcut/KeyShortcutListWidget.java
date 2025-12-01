package fr.siroz.cariboustonks.screen.keyshortcut;

import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcut;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class KeyShortcutListWidget extends ElementListWidget<KeyShortcutListWidget.KeyShortcutEntry> {

	private final KeyShortcutScreen parent;

	KeyShortcutListWidget(
			MinecraftClient client,
			@NotNull KeyShortcutScreen parent,
			int width,
			int height,
			int y,
			int itemHeight
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;

		for (KeyShortcut shortcut : parent.shortcuts.values()) {
			addEntry(new KeyShortcutEntry(shortcut));
		}
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	@Override
	protected void removeEntry(KeyShortcutEntry entry) {
		if (entry != null && entry.keyShortcut != null) {
			super.removeEntry(entry);
			parent.shortcuts.remove(entry.keyShortcut.command());
		}
	}

	@Override
	public void setSelected(@Nullable KeyShortcutEntry entry) {
		super.setSelected(entry);
		parent.updateButtons();
	}

	void createKeyShortcut() {
		// Pour éviter d'avoir plusieurs KeyShortcut vide
		KeyShortcut keyShortcut = new KeyShortcut("", -1);
		boolean hasShortcut = parent.shortcuts.containsKey(keyShortcut.command());
		boolean hasEntry = children().stream()
				.anyMatch(entry -> entry.keyShortcut.command().equals(keyShortcut.command()));
		if (!hasShortcut && !hasEntry) {
			addEntry(new KeyShortcutEntry(keyShortcut));
		}
	}

	protected class KeyShortcutEntry extends ElementListWidget.Entry<KeyShortcutEntry> {

		protected KeyShortcut keyShortcut;

		private final List<ClickableWidget> children;
		private final TextFieldWidget commandWidget;
		private final ButtonWidget keyBindWidget;

		private boolean waitingForKey = false;

		public KeyShortcutEntry(@NotNull KeyShortcut keyShortcut) {
			this.keyShortcut = keyShortcut;

			this.commandWidget = new TextFieldWidget(client.textRenderer, width / 2 - 160, 5, 150, 20, Text.literal("Command"));
			this.commandWidget.setTooltip(Tooltip.of(Text.literal("Example: 'equipment' or '/equipment'")));
			this.commandWidget.setText(keyShortcut.command());
			this.commandWidget.setMaxLength(48);

			this.keyBindWidget = ButtonWidget.builder(Text.literal("" + keyShortcut.keyCode()), b -> {
				parent.setCurrentEntry(this);
				waitingForKey = true;
			}).dimensions(0, 0, 56, 20).build();

			this.children = List.of(commandWidget, keyBindWidget);
		}

		public void setKeyCode(int code) {
			KeyShortcut shortcut = new KeyShortcut(commandWidget.getText(), code);
			parent.shortcuts.put(shortcut.command(), shortcut);
			waitingForKey = false;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			// Vu que le Widget n'est pas un AlwaysSelectedEntryListWidget, le rendu ne se fait pas.
			// Et si c'était le cas, la facon de gérer les ElementListWidget-Entry change.
			// La facon de gérer les screen me rend fou -_-
			if (getSelectedOrNull() != null && getSelectedOrNull() == this) {
				int x1 = this.getX();
				int x2 = this.getX() + this.getContentWidth();
				context.fill(x1, this.getY() - 6, x2, this.getY() + this.getHeight() - 4, Colors.GRAY.asInt());
				context.fill(x1 + 1, this.getY() - 5, x2 - 1, this.getY() + this.getHeight() - 5, Colors.BLACK.asInt());
			}

			commandWidget.setPosition(this.getX() + 10, this.getY() + 1);
			keyBindWidget.setPosition(width / 2 + 44, this.getY());
			keyBindWidget.active = !waitingForKey;

			int keyCode = parent.shortcuts.values().stream()
					.filter(shortcut -> shortcut.command().equals(commandWidget.getText()))
					.map(KeyShortcut::keyCode)
					.findFirst()
					.orElse(-1);

			String label;
			if (waitingForKey) {
				label = "..";
			} else if (keyCode == -1) {
				label = "?";
			} else if (keyCode <= -2000) {
				int mouseButton = -2000 - keyCode;
				label = InputUtil.Type.MOUSE.createFromCode(mouseButton).getLocalizedText().getString();
			} else {
				label = InputUtil.fromKeyCode(new KeyInput(keyCode, 0, 0)).getLocalizedText().getString();
			}

			boolean duplicate = keyCode != -1 && parent.shortcuts.values().stream()
					.map(KeyShortcut::keyCode)
					.filter(c -> c == keyCode).count() > 1;

			Text message = duplicate ? Text.literal(label).formatted(Formatting.RED) : Text.literal(label);
			keyBindWidget.setMessage(message);

			for (ClickableWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			if (keyBindWidget.mouseClicked(click, doubled)) {
				return true;
			}

			return super.mouseClicked(click, doubled);
		}
	}
}
