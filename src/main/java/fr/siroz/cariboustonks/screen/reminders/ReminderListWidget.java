package fr.siroz.cariboustonks.screen.reminders;

import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import fr.siroz.cariboustonks.util.colors.Colors;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

class ReminderListWidget extends AlwaysSelectedEntryListWidget<ReminderListWidget.Entry> {

	private final ReminderScreen parent;

	ReminderListWidget(
			MinecraftClient client,
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

	protected abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<ReminderListWidget.Entry> {
		public Entry() {
		}
	}

	protected class NothingToShow extends Entry {

		private static final Text TEXT = Text.literal("No reminders for the moment ;'(");

		@Override
		public Text getNarration() {
			return Text.literal("Nothing to show");
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (client == null || client.currentScreen == null || client.textRenderer == null) {
				return;
			}

			int x1 = (client.currentScreen.width - client.textRenderer.getWidth(TEXT)) / 2;
			int y1 = this.getY() + (this.getHeight() - 9) / 2; // getContentHeight()
			context.drawTextWithShadow(client.textRenderer, TEXT, x1, y1, Colors.WHITE.asInt());
		}
	}

	protected class ReminderEntry extends Entry {

		protected Pair<Reminder, TimedObject> reminder;
		private final Text name;
		private final Text expireTime;
		private final Text description;
		private final ItemStack icon;

		public ReminderEntry(@NotNull Pair<Reminder, TimedObject> reminder) {
			this.reminder = reminder;
			this.name = reminder.left().display().title();

			Instant expiration = reminder.right().expirationTime();
			String time = TimeUtils.formatInstant(expiration, TimeUtils.DATE_FULL);
			String relative = TimeUtils.getDurationFormatted(expiration);

			this.expireTime = Text.literal("> ").formatted(Formatting.WHITE)
					.append(Text.literal(time).formatted(Formatting.AQUA))
					.append(Text.literal(" (In " + relative + ")").formatted(Formatting.GRAY));

			this.description = reminder.left().display().description() != null
					? reminder.left().display().description()
					: StonksUtils.jsonToText(reminder.right().message()).orElse(Text.literal(reminder.right().message()));

			this.icon = reminder.left().display().icon();
		}

		@Override
		public Text getNarration() {
			return Text.literal("Reminder " + name.getString());
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (client == null || client.currentScreen == null || client.textRenderer == null) {
				return;
			}

			context.drawTextWithShadow(client.textRenderer, name, this.getX() + 32 + 3, this.getY() + 1, Colors.WHITE.asInt());
			int x1 = this.getX() + 32 + 3;
			context.drawTextWithShadow(client.textRenderer, expireTime, x1, this.getY() + 12, Colors.WHITE.asInt());
			context.drawTextWithShadow(client.textRenderer, description, x1, this.getY() + 23, Colors.WHITE.asInt());

			context.drawItem(icon, this.getX() + 7, this.getY() + 7);
		}
	}
}
