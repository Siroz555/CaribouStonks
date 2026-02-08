package fr.siroz.cariboustonks.screens.reminders;

import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.util.CodecUtil;
import fr.siroz.cariboustonks.util.TimeUtils;
import it.unimi.dsi.fastutil.Pair;
import java.time.Instant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

class ReminderListWidget extends ObjectSelectionList<ReminderListWidget.Entry> {

	private final ReminderScreen parent;

	ReminderListWidget(
            Minecraft client,
            @NonNull ReminderScreen parent,
            int width,
            int height,
            int y,
            int itemHeight
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;

		if (parent.reminders.isEmpty()) {
			addEntry(new NothingToShow());
		} else {
			for (Pair<ReminderComponent, TimedObjectModel> reminder : parent.reminders) {
				addEntry(new ReminderEntry(reminder));
			}
		}
	}

	@Override
	public int getRowWidth() {
		return 270;
	}

	@Override
	public void setSelected(@Nullable Entry entry) {
		super.setSelected(entry);

		Pair<ReminderComponent, TimedObjectModel> item = null;
		if (entry instanceof ReminderEntry reminderEntry) {
			item = reminderEntry.reminder;
		}

		this.parent.itemSelected(item);
	}

	protected abstract static class Entry extends ObjectSelectionList.Entry<ReminderListWidget.Entry> {
		public Entry() {
		}
	}

	protected class NothingToShow extends Entry {

		private static final Component TEXT = Component.literal("No reminders for the moment ;'(");

		@Override
		public @NonNull Component getNarration() {
			return Component.literal("Nothing to show");
		}

		@Override
		public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (minecraft.screen == null) {
				return;
			}

			int x1 = (minecraft.screen.width - minecraft.font.width(TEXT)) / 2;
			int y1 = this.getY() + (this.getHeight() - 9) / 2; // getContentHeight()
			guiGraphics.drawString(minecraft.font, TEXT, x1, y1, Colors.WHITE.asInt());
		}
	}

	protected class ReminderEntry extends Entry {

		protected Pair<ReminderComponent, TimedObjectModel> reminder;
		private final Component name;
		private final Component expireTime;
		private final Component description;
		private final ItemStack icon;

		public ReminderEntry(@NonNull Pair<ReminderComponent, TimedObjectModel> reminder) {
			this.reminder = reminder;
			this.name = reminder.left().getDisplay().title();

			Instant expiration = reminder.right().expirationTime();
			String time = TimeUtils.formatInstant(expiration, TimeUtils.DATE_FULL);
			String relative = TimeUtils.getDurationFormatted(expiration);

			this.expireTime = Component.literal("> ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(time).withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" (In " + relative + ")").withStyle(ChatFormatting.GRAY));

			this.description = reminder.left().getDisplay().description() != null
					? reminder.left().getDisplay().description()
					: CodecUtil.jsonToText(reminder.right().message()).orElse(Component.literal(reminder.right().message()));

			this.icon = reminder.left().getDisplay().icon();
		}

		@Override
		public @NonNull Component getNarration() {
			return Component.literal("Reminder " + name.getString());
		}

		@Override
		public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (minecraft.screen == null) {
				return;
			}

			guiGraphics.drawString(minecraft.font, name, this.getX() + 32 + 3, this.getY() + 1, Colors.WHITE.asInt());
			int x1 = this.getX() + 32 + 3;
			guiGraphics.drawString(minecraft.font, expireTime, x1, this.getY() + 12, Colors.WHITE.asInt());
			guiGraphics.drawString(minecraft.font, description, x1, this.getY() + 23, Colors.WHITE.asInt());

			guiGraphics.renderItem(icon, this.getX() + 7, this.getY() + 7);
		}
	}
}
