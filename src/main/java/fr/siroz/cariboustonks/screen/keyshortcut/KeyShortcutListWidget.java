package fr.siroz.cariboustonks.screen.keyshortcut;

import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcut;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class KeyShortcutListWidget extends ContainerObjectSelectionList<KeyShortcutListWidget.KeyShortcutEntry> {

	private final KeyShortcutScreen parent;

	KeyShortcutListWidget(
            Minecraft client,
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

	protected class KeyShortcutEntry extends ContainerObjectSelectionList.Entry<KeyShortcutEntry> {

		protected KeyShortcut keyShortcut;

		private final List<AbstractWidget> children;
		private final EditBox commandWidget;
		private final Button keyBindWidget;

		private boolean waitingForKey = false;

		public KeyShortcutEntry(@NotNull KeyShortcut keyShortcut) {
			this.keyShortcut = keyShortcut;

			this.commandWidget = new EditBox(minecraft.font, width / 2 - 160, 5, 150, 20, Component.literal("Command"));
			this.commandWidget.setTooltip(Tooltip.create(Component.literal("Example: 'equipment' or '/equipment'")));
			this.commandWidget.setValue(keyShortcut.command());
			this.commandWidget.setMaxLength(48);

			this.keyBindWidget = Button.builder(Component.literal("" + keyShortcut.keyCode()), b -> {
				parent.setCurrentEntry(this);
				waitingForKey = true;
			}).bounds(0, 0, 56, 20).build();

			this.children = List.of(commandWidget, keyBindWidget);
		}

		public void setKeyCode(int code) {
			KeyShortcut shortcut = new KeyShortcut(commandWidget.getValue(), code);
			parent.shortcuts.put(shortcut.command(), shortcut);
			waitingForKey = false;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			// Vu que le Widget n'est pas un AlwaysSelectedEntryListWidget, le rendu ne se fait pas.
			// Et si c'était le cas, la facon de gérer les ElementListWidget-Entry change.
			// La facon de gérer les screen me rend fou -_-
			if (getSelected() != null && getSelected() == this) {
				int x1 = this.getX();
				int x2 = this.getX() + this.getContentWidth();
				context.fill(x1, this.getY() - 6, x2, this.getY() + this.getHeight() - 4, Colors.GRAY.asInt());
				context.fill(x1 + 1, this.getY() - 5, x2 - 1, this.getY() + this.getHeight() - 5, Colors.BLACK.asInt());
			}

			commandWidget.setPosition(this.getX() + 10, this.getY() + 1);
			keyBindWidget.setPosition(width / 2 + 44, this.getY());
			keyBindWidget.active = !waitingForKey;

			int keyCode = parent.shortcuts.values().stream()
					.filter(shortcut -> shortcut.command().equals(commandWidget.getValue()))
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
				label = InputConstants.Type.MOUSE.getOrCreate(mouseButton).getDisplayName().getString();
			} else {
				label = InputConstants.getKey(new KeyEvent(keyCode, 0, 0)).getDisplayName().getString();
			}

			boolean duplicate = keyCode != -1 && parent.shortcuts.values().stream()
					.map(KeyShortcut::keyCode)
					.filter(c -> c == keyCode).count() > 1;

			Component message = duplicate ? Component.literal(label).withStyle(ChatFormatting.RED) : Component.literal(label);
			keyBindWidget.setMessage(message);

			for (AbstractWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			if (keyBindWidget.mouseClicked(click, doubled)) {
				return true;
			}

			return super.mouseClicked(click, doubled);
		}
	}
}
