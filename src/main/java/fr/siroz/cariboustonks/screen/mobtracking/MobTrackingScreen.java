package fr.siroz.cariboustonks.screen.mobtracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingFeature;
import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingRegistry;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Experimental
public class MobTrackingScreen extends CaribousStonksScreen {

	@Nullable
	private final Screen parent;
	private final MobTrackingFeature mobTrackingFeature;
	protected final Map<String, MobTrackingRegistry.MobTrackingEntry> trackedMobs;

	private MobTrackingScreen(@Nullable Screen parent) {
		super(Component.empty()
				.append(Component.literal("Mob Tracking ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal(" (Beta)").withStyle(ChatFormatting.GRAY))
		);
		this.parent = parent;
		this.mobTrackingFeature = CaribouStonks.features().getFeature(MobTrackingFeature.class);
		this.trackedMobs = this.mobTrackingFeature.getRegistry().getTrackedMobsSnapshot();
	}

	public static @NonNull MobTrackingScreen create(@Nullable Screen parent) {
		return new MobTrackingScreen(parent);
	}

	@Override
	protected void onInit() {
		this.addRenderableWidget(new MobTrackingListWidget(minecraft, this, width, height - 120, 32, 24));

		GridLayout grid = new GridLayout();
		grid.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);

		GridLayout.RowHelper adder = grid.createRowHelper(2);
		adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			saveMobTrackingConfig();
			close();
		}).width(310).build(), 2);

		grid.arrangeElements();
		FrameLayout.centerInRectangle(grid, 0, this.height - 64, this.width, 64);
		grid.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.onRender(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, Colors.WHITE.asInt());
	}

	@Override
	public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
		return super.onMouseClicked(click, doubled);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void close() {
		minecraft.setScreen(parent);
	}

	private void saveMobTrackingConfig() {
		mobTrackingFeature.getRegistry().updateMobTrackingConfig(trackedMobs);
		mobTrackingFeature.getRegistry().saveMobTrackingConfig();
	}
}
