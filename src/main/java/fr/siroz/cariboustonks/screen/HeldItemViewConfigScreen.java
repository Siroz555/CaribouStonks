package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.VanillaConfig;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.gui.DoubleSliderWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the configuration screen for customizing the appearance of held item models in the player's hands.
 * <p>
 * Credits to <a href="https://github.com/AzureAaron/aaron-mod">AzureAaron</a>
 * for the basic code on rendering an item in a screen.
 * Many things have been changed, such as the organization of the code, certain logic,
 * the addition of a {@link DoubleSliderWidget} for elements, and simplification according to my needs.
 */
public final class HeldItemViewConfigScreen extends CaribousStonksScreen {

	@Nullable
	private final Screen parent;
	@Nullable
	private ThreePartsLayoutWidget layout;
	private final Hand hand;
	private final ItemStack previewItem;
	private final VanillaConfig.ItemModelCustomization.CustomHand currentHand;
	private final VanillaConfig.ItemModelCustomization.CustomHand backupHand;
	private boolean changed;

	private HeldItemViewConfigScreen(@Nullable Screen parent, Hand hand) {
		super(Text.literal("Customize Held Item for " + (hand == Hand.MAIN_HAND ? "Main Hand" : "Off Hand")));
		this.parent = parent;
		this.hand = hand;
		this.previewItem = new ItemStack(Items.NETHERITE_SWORD);
		this.currentHand = switch (this.hand) {
			case Hand.MAIN_HAND -> ConfigManager.getConfig().vanilla.itemModelCustomization.mainHand;
			case Hand.OFF_HAND -> ConfigManager.getConfig().vanilla.itemModelCustomization.offHand;
		};
		this.backupHand = new VanillaConfig.ItemModelCustomization.CustomHand().copyFrom(this.currentHand);
		this.changed = false;
	}

	@Contract("_, _ -> new")
	public static @NotNull HeldItemViewConfigScreen create(@Nullable Screen parent, @NotNull Hand hand) {
		return new HeldItemViewConfigScreen(parent, hand);
	}

	public ItemStack getPreviewItem() {
		return previewItem;
	}

	public Hand getHand() {
		return hand;
	}

	@Override
	protected void onInit() {
		if (this.client == null || this.client.world == null) {
			layout = new ThreePartsLayoutWidget(this);
			layout.addHeader(new TextWidget(this.getTitle(), this.textRenderer));
			layout.addBody(new TextWidget(Text.literal("You must be in a world to use this.").withColor(Colors.ORANGE.asInt()), this.textRenderer));
			layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, b -> onClose()).width(210).build());
			layout.refreshPositions();
			layout.forEachChild(this::addDrawableChild);
			return;
		}

		GridWidget grid = new GridWidget();
		grid.setSpacing(8);

		GridWidget.Adder adder = grid.createAdder(3);
		adder.add(new TextWidget(this.title, this.textRenderer), 3);
		adder.add(new EmptyWidget(0, 10), 3);

		adder.add(new TextWidget(Text.literal("Positions"), this.textRenderer), 3);
		adder.add(new TextWidget(Text.literal("X"), this.textRenderer));
		adder.add(new TextWidget(Text.literal("Y"), this.textRenderer));
		adder.add(new TextWidget(Text.literal("Z"), this.textRenderer));

		adder.add(new DoubleSliderWidget(0, 0, 100, 20, Text.literal("X Position"),
				-1.0f, 1.0f,
				currentHand.x,
				newValue -> {
					currentHand.x = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		));
		adder.add(new DoubleSliderWidget(0, 0, 100, 20, Text.literal("Y Position"),
				-1.0f, 1.0f,
				currentHand.y,
				newValue -> {
					currentHand.y = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		));
		adder.add(new DoubleSliderWidget(0, 0, 100, 20, Text.literal("Z Position"),
				-1.0f, 1.0f,
				currentHand.z,
				newValue -> {
					currentHand.z = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		));

		adder.add(new TextWidget(Text.literal("Size"), this.textRenderer), 3);
		adder.add(new DoubleSliderWidget(0, 0, 208, 20, Text.literal("Size"),
				0.1f, 1.0f,
				currentHand.scale,
				newValue -> {
					currentHand.scale = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		), 3);

		adder.add(new EmptyWidget(0, 20), 3);
		adder.add(ButtonWidget.builder(Text.literal("Revert"), b -> revert())
				.tooltip(Tooltip.of(Text.literal("Reverts to the last saved values.")))
				.width(100)
				.build());
		adder.add(ButtonWidget.builder(Text.literal("Reset"), b -> reset())
				.tooltip(Tooltip.of(Text.literal("Resets to the default values.")))
				.width(100)
				.build());
		adder.add(ButtonWidget.builder(Text.literal("Save"), b -> saveAndClose())
				.tooltip(Tooltip.of(Text.literal("Saves and close.")))
				.width(208)
				.build(), 3);

		grid.refreshPositions();
		ScreenRect dimensions = getEffectiveDimensions(this.width, this.height);
		SimplePositioningWidget.setPos(grid, dimensions, 0.5f, 0.35f);
		grid.forEachChild(this::addDrawableChild);
	}

	@Override
	public void renderBackground(@NotNull DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		ScreenRect dimensions = getEffectiveDimensions(this.width, this.height);

		context.enableScissor(dimensions.getLeft(), dimensions.getTop(), dimensions.getRight(), dimensions.getBottom());
		GuiRenderer.enableBlurScissor(dimensions.getLeft(), dimensions.getTop(), dimensions.width(), dimensions.height());
		this.applyBlur(context);
		this.renderDarkening(context);
		context.disableScissor();

		context.drawVerticalLine(
				isMainHand() ? dimensions.getRight() : dimensions.getLeft(),
				dimensions.getTop() - 1,
				dimensions.getBottom(),
				new Color(0, 0, 0).withAlpha(190).asInt());

		context.drawVerticalLine(
				isMainHand() ? dimensions.getRight() - 1 : dimensions.getLeft() + 1,
				dimensions.getTop() - 1,
				dimensions.getBottom(),
				new Color(255, 255, 255).withAlpha(50).asInt());
	}

	@Override
	protected void refreshWidgetPositions() {
		if (layout != null) {
			layout.refreshPositions();
		}
	}

	@Override
	public void onClose() {
		if (this.client != null) {
			if (changed) {
				this.client.setScreen(new ConfirmScreen(confirmation -> {
					if (confirmation) {
						revert();
						this.client.setScreen(parent);
					} else {
						this.client.setScreen(this);
					}
				}, CONFIRM_SCREEN_UNSAVED_CHANGE, CONFIRM_SCREEN_PROMPT, CONFIRM_SCREEN_QUIT_MESSAGE, ScreenTexts.CANCEL));
			} else {
				this.client.setScreen(parent);
			}
		}
	}

	@Contract("_, _ -> new")
	private @NotNull ScreenRect getEffectiveDimensions(int scaledWindowWidth, int scaledWindowHeight) {
		int x = isMainHand() ? 0 : scaledWindowWidth / 2;
		int width = scaledWindowWidth / 2;

		return new ScreenRect(new ScreenPos(x, 0), width, scaledWindowHeight);
	}

	private boolean isMainHand() {
		return hand == Hand.MAIN_HAND;
	}

	private void saveAndClose() {
		ConfigManager.saveConfig();
		changed = false;
		onClose();
	}

	private void revert() {
		currentHand.copyFrom(backupHand);
		changed = false;
		this.clearAndInit();
	}

	private void reset() {
		currentHand.copyFrom(new VanillaConfig.ItemModelCustomization.CustomHand());
		changed = true;
		this.clearAndInit();
	}
}
