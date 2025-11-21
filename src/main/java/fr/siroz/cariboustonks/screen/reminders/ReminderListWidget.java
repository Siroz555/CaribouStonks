package fr.siroz.cariboustonks.screen.reminders;

import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

class ReminderListWidget extends ObjectSelectionList<ReminderListWidget.Entry> {

	private final ReminderScreen parent;

	ReminderListWidget(
            Minecraft client,
            @NotNull ReminderScreen parent,
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
			for (Pair<Reminder, TimedObject> reminder : parent.reminders) {
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

		Pair<Reminder, TimedObject> item = null;
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
		public Component getNarration() {
			return Component.literal("Nothing to show");
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (minecraft == null || minecraft.screen == null || minecraft.font == null) {
				return;
			}

			int x1 = (minecraft.screen.width - minecraft.font.width(TEXT)) / 2;
			int y1 = this.getY() + (this.getHeight() - 9) / 2; // getContentHeight()
			context.drawString(minecraft.font, TEXT, x1, y1, Colors.WHITE.asInt());
		}
	}

	protected class ReminderEntry extends Entry {

		protected Pair<Reminder, TimedObject> reminder;
		private final Component name;
		private final Component expireTime;
		private final Component description;
		private final ItemStack icon;

		public ReminderEntry(@NotNull Pair<Reminder, TimedObject> reminder) {
			this.reminder = reminder;
			this.name = reminder.left().display().title();

			Instant expiration = reminder.right().expirationTime();
			String time = TimeUtils.formatInstant(expiration, TimeUtils.DATE_FULL);
			String relative = TimeUtils.getDurationFormatted(expiration);

			this.expireTime = Component.literal("> ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(time).withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" (In " + relative + ")").withStyle(ChatFormatting.GRAY));

			this.description = reminder.left().display().description() != null
					? reminder.left().display().description()
					: StonksUtils.jsonToText(reminder.right().message()).orElse(Component.literal(reminder.right().message()));

			this.icon = reminder.left().display().icon();
		}

		@Override
		public Component getNarration() {
			return Component.literal("Reminder " + name.getString());
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (minecraft == null || minecraft.screen == null || minecraft.font == null) {
				return;
			}

			context.drawString(minecraft.font, name, this.getX() + 32 + 3, this.getY() + 1, Colors.WHITE.asInt());
			int x1 = this.getX() + 32 + 3;
			context.drawString(minecraft.font, expireTime, x1, this.getY() + 12, Colors.WHITE.asInt());
			context.drawString(minecraft.font, description, x1, this.getY() + 23, Colors.WHITE.asInt());

			context.renderItem(icon, this.getX() + 7, this.getY() + 7);
		}
	}
}
