package fr.siroz.cariboustonks.screen.mobtracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingFeature;
import fr.siroz.cariboustonks.feature.ui.tracking.MobTrackingRegistry;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.util.Map;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public class MobTrackingScreen extends CaribousStonksScreen {

	@Nullable
	private final Screen parent;
	private final MobTrackingFeature mobTrackingFeature;
	protected final Map<String, MobTrackingRegistry.MobTrackingEntry> trackedMobs;

	private MobTrackingScreen(@Nullable Screen parent) {
		super(Text.empty()
				.append(Text.literal("Mob Tracking ").formatted(Formatting.RED, Formatting.BOLD))
				.append(Text.literal(" (Beta)").formatted(Formatting.GRAY))
		);
		this.parent = parent;
		this.mobTrackingFeature = CaribouStonks.features().getFeature(MobTrackingFeature.class);
		this.trackedMobs = this.mobTrackingFeature.getRegistry().getTrackedMobs();
	}

	@Contract("_ -> new")
	public static @NotNull MobTrackingScreen create(@Nullable Screen parent) {
		return new MobTrackingScreen(parent);
	}

	@Override
	protected void onInit() {
		this.addDrawableChild(new MobTrackingListWidget(client, this, width, height - 120, 32, 24));

		GridWidget grid = new GridWidget();
		grid.getMainPositioner().marginX(5).marginY(2);

		GridWidget.Adder adder = grid.createAdder(2);
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			saveMobTrackingConfig();
			close();
		}).width(310).build(), 2);

		grid.refreshPositions();
		SimplePositioningWidget.setPos(grid, 0, this.height - 64, this.width, 64);
		grid.forEachChild(this::addDrawableChild);
	}

	@Override
	public void onRender(DrawContext guiGraphics, int mouseX, int mouseY, float delta) {
		super.onRender(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, Colors.WHITE.asInt());
	}

	@Override
	public boolean onMouseClicked(Click click, boolean doubled) {
		return super.onMouseClicked(click, doubled);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void onClose() {
		assert client != null;
		client.setScreen(parent);
	}

	private void saveMobTrackingConfig() {
		mobTrackingFeature.getRegistry().updateMobTrackingConfig(trackedMobs);
		mobTrackingFeature.getRegistry().saveMobTrackingConfig();
	}
}
