package fr.siroz.cariboustonks.screen.mobtracking;

import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingRegistry;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental // SIROZ-NOTE: C'est qu'une preview pour le moment
class MobTrackingListWidget extends ElementListWidget<MobTrackingListWidget.MobTrackingListEntry> {

	MobTrackingListWidget(MinecraftClient client, MobTrackingScreen screen, int width, int height, int y, int itemHeight) {
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
	protected int getScrollbarX() {
		return super.getScrollbarX();
	}

	protected class CategorySeparatorEntry extends MobTrackingListWidget.MobTrackingListEntry {

		private final MobTrackingRegistry.MobCategory category;

		public CategorySeparatorEntry(MobTrackingRegistry.MobCategory category) {
			this.category = category;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.drawCenteredTextWithShadow(client.textRenderer, category.getName(), this.getContentMiddleX(), this.getY() + 5, Colors.WHITE.asInt());
		}
	}

	protected class MobTrackingListEntry extends ElementListWidget.Entry<MobTrackingListWidget.MobTrackingListEntry> {

		private final MobTrackingRegistry.MobTrackingEntry entry;
		private final List<ClickableWidget> children;
		private final CheckboxWidget enabledWidget;
		private final CheckboxWidget notifyOnSpawnWidget;

		protected MobTrackingListEntry() { // Pour le CategorySeparatorEntry
			this.entry = null;
			this.enabledWidget = null;
			this.notifyOnSpawnWidget = null;
			this.children = List.of();
		}

		public MobTrackingListEntry(MobTrackingRegistry.@NotNull MobTrackingEntry entry) {
			this.entry = entry;

			this.enabledWidget = CheckboxWidget.builder(Text.literal(""), client.textRenderer)
					.checked(entry.config().enabled)
					.tooltip(Tooltip.of(Text.literal("Click to toggle this Mob Tracking")))
					.callback((checkbox, checked) -> entry.config().enabled = checked)
					.build();

			this.notifyOnSpawnWidget = CheckboxWidget.builder(Text.literal(""), client.textRenderer)
					.checked(entry.config().notifyOnSpawn)
					.tooltip(Tooltip.of(Text.literal("Click to enable notification when the mob is detected")))
					.callback((checkbox, checked) -> entry.config().notifyOnSpawn = checked)
					.build();

			this.children = List.of(this.enabledWidget, this.notifyOnSpawnWidget);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getContentMiddleX() - 100;
			int y = this.getY();

			context.drawTextWithShadow(client.textRenderer, entry.displayName(), x - 10, y + 5, Colors.WHITE.asInt());

			enabledWidget.setPosition(x + 150, y);
			notifyOnSpawnWidget.setPosition(x + 175, y);

			for (ClickableWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}
	}
}
