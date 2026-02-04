package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.VanillaConfig;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.gui.DoubleSliderWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
	private HeaderAndFooterLayout layout;
	private final InteractionHand hand;
	private final ItemStack previewItem;
	private final VanillaConfig.ItemModelCustomization.CustomHand currentHand;
	private final VanillaConfig.ItemModelCustomization.CustomHand backupHand;
	private boolean changed;

	private HeldItemViewConfigScreen(@Nullable Screen parent, InteractionHand hand) {
		super(Component.literal("Customize Held Item for " + (hand == InteractionHand.MAIN_HAND ? "Main Hand" : "Off Hand")));
		this.parent = parent;
		this.hand = hand;
		this.previewItem = new ItemStack(Items.NETHERITE_SWORD);
		this.currentHand = switch (this.hand) {
			case InteractionHand.MAIN_HAND -> ConfigManager.getConfig().vanilla.itemModelCustomization.mainHand;
			case InteractionHand.OFF_HAND -> ConfigManager.getConfig().vanilla.itemModelCustomization.offHand;
		};
		this.backupHand = new VanillaConfig.ItemModelCustomization.CustomHand().copyFrom(this.currentHand);
		this.changed = false;
	}

	@Contract("_, _ -> new")
	public static @NotNull HeldItemViewConfigScreen create(@Nullable Screen parent, @NotNull InteractionHand hand) {
		return new HeldItemViewConfigScreen(parent, hand);
	}

	public ItemStack getPreviewItem() {
		return previewItem;
	}

	public InteractionHand getHand() {
		return hand;
	}

	@Override
	protected void onInit() {
		boolean isWorldLoaded = this.minecraft.level != null;
		if (!isWorldLoaded || !ConfigManager.getConfig().vanilla.itemModelCustomization.enabled) {
			layout = new HeaderAndFooterLayout(this);
			layout.addToHeader(new StringWidget(this.getTitle(), this.font));

			if (!isWorldLoaded) {
				layout.addToContents(new StringWidget(Component.literal("You must be in a world to use this.").withColor(Colors.ORANGE.asInt()), this.font));
			} else {
				layout.addToContents(new StringWidget(Component.literal("You must enable Held Item customization to use this.").withColor(Colors.ORANGE.asInt()), this.font));
			}

			layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> close()).width(210).build());
			layout.arrangeElements();
			layout.visitWidgets(this::addRenderableWidget);
			return;
		}

		GridLayout grid = new GridLayout();
		grid.spacing(8);

		GridLayout.RowHelper adder = grid.createRowHelper(3);
		adder.addChild(new StringWidget(this.title, this.font), 3);
		adder.addChild(new SpacerElement(0, 10), 3);

		adder.addChild(new StringWidget(Component.literal("Positions"), this.font), 3);
		adder.addChild(new StringWidget(Component.literal("X"), this.font));
		adder.addChild(new StringWidget(Component.literal("Y"), this.font));
		adder.addChild(new StringWidget(Component.literal("Z"), this.font));

		adder.addChild(new DoubleSliderWidget(0, 0, 100, 20, Component.literal("X Position"),
				-1.0f, 1.0f,
				currentHand.x,
				newValue -> {
					currentHand.x = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		));
		adder.addChild(new DoubleSliderWidget(0, 0, 100, 20, Component.literal("Y Position"),
				-1.0f, 1.0f,
				currentHand.y,
				newValue -> {
					currentHand.y = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		));
		adder.addChild(new DoubleSliderWidget(0, 0, 100, 20, Component.literal("Z Position"),
				-1.0f, 1.0f,
				currentHand.z,
				newValue -> {
					currentHand.z = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		));

		adder.addChild(new StringWidget(Component.literal("Size"), this.font), 3);
		adder.addChild(new DoubleSliderWidget(0, 0, 208, 20, Component.literal("Size"),
				0.1f, 1.0f,
				currentHand.scale,
				newValue -> {
					currentHand.scale = (float) newValue;
					changed = !currentHand.equals(backupHand);
				}
		), 3);

		adder.addChild(new SpacerElement(0, 20), 3);
		adder.addChild(Button.builder(Component.literal("Revert"), b -> revert())
				.tooltip(Tooltip.create(Component.literal("Reverts to the last saved values.")))
				.width(100)
				.build());
		adder.addChild(Button.builder(Component.literal("Reset"), b -> reset())
				.tooltip(Tooltip.create(Component.literal("Resets to the default values.")))
				.width(100)
				.build());
		adder.addChild(Button.builder(Component.literal("Save"), b -> saveAndClose())
				.tooltip(Tooltip.create(Component.literal("Saves and close.")))
				.width(208)
				.build(), 3);

		grid.arrangeElements();
		ScreenRectangle dimensions = getEffectiveDimensions(this.width, this.height);
		FrameLayout.alignInRectangle(grid, dimensions, 0.5f, 0.35f);
		grid.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
		ScreenRectangle dimensions = getEffectiveDimensions(this.width, this.height);

		guiGraphics.enableScissor(dimensions.left(), dimensions.top(), dimensions.right(), dimensions.bottom());
		GuiRenderer.enableBlurScissor(dimensions.left(), dimensions.top(), dimensions.width(), dimensions.height());
		this.renderBlurredBackground(guiGraphics);
		this.renderMenuBackground(guiGraphics);
		guiGraphics.disableScissor();

		guiGraphics.vLine(
				isMainHand() ? dimensions.right() : dimensions.left(),
				dimensions.top() - 1,
				dimensions.bottom(),
				new Color(0, 0, 0).withAlpha(190).asInt());

		guiGraphics.vLine(
				isMainHand() ? dimensions.right() - 1 : dimensions.left() + 1,
				dimensions.top() - 1,
				dimensions.bottom(),
				new Color(255, 255, 255).withAlpha(50).asInt());
	}

	@Override
	protected void repositionElements() {
		if (layout != null) {
			layout.arrangeElements();
		}
	}

	@Override
	public void close() {
		if (changed) {
			this.minecraft.setScreen(new ConfirmScreen(confirmation -> {
				if (confirmation) {
					revert();
					this.minecraft.setScreen(parent);
				} else {
					this.minecraft.setScreen(this);
				}
			}, CONFIRM_SCREEN_UNSAVED_CHANGE, CONFIRM_SCREEN_PROMPT, CONFIRM_SCREEN_QUIT_MESSAGE, CommonComponents.GUI_CANCEL));
		} else {
			this.minecraft.setScreen(parent);
		}
	}

	@Contract("_, _ -> new")
	private @NotNull ScreenRectangle getEffectiveDimensions(int scaledWindowWidth, int scaledWindowHeight) {
		int x = isMainHand() ? 0 : scaledWindowWidth / 2;
		int width = scaledWindowWidth / 2;

		return new ScreenRectangle(new ScreenPosition(x, 0), width, scaledWindowHeight);
	}

	private boolean isMainHand() {
		return hand == InteractionHand.MAIN_HAND;
	}

	private void saveAndClose() {
		ConfigManager.saveConfig();
		changed = false;
		close();
	}

	private void revert() {
		currentHand.copyFrom(backupHand);
		changed = false;
		this.rebuildWidgets();
	}

	private void reset() {
		currentHand.copyFrom(new VanillaConfig.ItemModelCustomization.CustomHand());
		changed = true;
		this.rebuildWidgets();
	}
}
