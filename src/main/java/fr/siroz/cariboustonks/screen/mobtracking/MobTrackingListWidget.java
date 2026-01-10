package fr.siroz.cariboustonks.screen.mobtracking;

import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingRegistry;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental // SIROZ-NOTE: C'est qu'une preview pour le moment
class MobTrackingListWidget extends ContainerObjectSelectionList<MobTrackingListWidget.MobTrackingListEntry> {

	MobTrackingListWidget(Minecraft client, MobTrackingScreen screen, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);

		Map<String, MobTrackingRegistry.MobTrackingEntry> mobTrackingEntries = screen.trackedMobs;
		Map<MobTrackingRegistry.MobCategory, List<MobTrackingRegistry.MobTrackingEntry>> groupedByCategory = new LinkedHashMap<>();

		for (MobTrackingRegistry.MobTrackingEntry entry : mobTrackingEntries.values()) {
			groupedByCategory.computeIfAbsent(entry.category(), k -> new ArrayList<>()).add(entry);
		}

		for (Map.Entry<MobTrackingRegistry.MobCategory, List<MobTrackingRegistry.MobTrackingEntry>> categoryGroup : groupedByCategory.entrySet()) {
			addEntry(new CategorySeparatorEntry(categoryGroup.getKey()));
			for (MobTrackingRegistry.MobTrackingEntry mobEntry : categoryGroup.getValue()) {
				addEntry(new MobTrackingListEntry(mobEntry));
			}
		}
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 10;
	}

	@Override
	protected int scrollBarX() {
		return super.scrollBarX();
	}

	protected class CategorySeparatorEntry extends MobTrackingListWidget.MobTrackingListEntry {

		private final MobTrackingRegistry.MobCategory category;

		public CategorySeparatorEntry(MobTrackingRegistry.MobCategory category) {
			this.category = category;
		}

		@Override
		public @NotNull List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public @NotNull List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			guiGraphics.drawCenteredString(minecraft.font, category.getName(), this.getContentXMiddle(), this.getY() + 5, Colors.WHITE.asInt());
		}
	}

	protected class MobTrackingListEntry extends ContainerObjectSelectionList.Entry<MobTrackingListWidget.MobTrackingListEntry> {

		private final MobTrackingRegistry.MobTrackingEntry entry;
		private final List<AbstractWidget> children;
		private final Checkbox enabledWidget;
		private final Checkbox notifyOnSpawnWidget;

		protected MobTrackingListEntry() { // Pour le CategorySeparatorEntry
			this.entry = null;
			this.enabledWidget = null;
			this.notifyOnSpawnWidget = null;
			this.children = List.of();
		}

		public MobTrackingListEntry(MobTrackingRegistry.@NotNull MobTrackingEntry entry) {
			this.entry = entry;

			this.enabledWidget = Checkbox.builder(Component.literal(""), minecraft.font)
					.selected(entry.config().enabled)
					.tooltip(Tooltip.create(Component.literal("Click to toggle this Mob Tracking")))
					.onValueChange((checkbox, checked) -> entry.config().enabled = checked)
					.build();

			this.notifyOnSpawnWidget = Checkbox.builder(Component.literal(""), minecraft.font)
					.selected(entry.config().notifyOnSpawn)
					.tooltip(Tooltip.create(Component.literal("Click to enable notification when the mob is detected")))
					.onValueChange((checkbox, checked) -> entry.config().notifyOnSpawn = checked)
					.build();

			this.children = List.of(this.enabledWidget, this.notifyOnSpawnWidget);
		}

		@Override
		public @NotNull List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public @NotNull List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getContentXMiddle() - 100;
			int y = this.getY();

			guiGraphics.drawString(minecraft.font, entry.displayName(), x - 10, y + 5, Colors.WHITE.asInt());

			enabledWidget.setPosition(x + 150, y);
			notifyOnSpawnWidget.setPosition(x + 175, y);

			for (AbstractWidget child : children) {
				child.render(guiGraphics, mouseX, mouseY, deltaTicks);
			}
		}
	}
}
