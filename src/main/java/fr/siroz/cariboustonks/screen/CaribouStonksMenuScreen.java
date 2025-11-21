package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.screen.search.StonksSearchScreen;
import fr.siroz.cariboustonks.screen.keyshortcut.KeyShortcutScreen;
import fr.siroz.cariboustonks.screen.reminders.ReminderScreen;
import fr.siroz.cariboustonks.screen.waypoints.WaypointScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.locale.Language;

public class CaribouStonksMenuScreen extends CaribousStonksScreen {

	private static final Identifier ICON = CaribouStonks.identifier("icon.png");
	//private static final Identifier BACKGROUND_TEXTURE = CaribouStonks.identifier("textures/screen/background_splash.png");

	private static final int SPACING = 8;
	private static final int BUTTON_WIDTH = 210;
	private static final int HALF_BUTTON_WIDTH = 100;

	private HeaderAndFooterLayout layout;

	public CaribouStonksMenuScreen() {
		super(Component.literal("CaribouStonks").withStyle(ChatFormatting.BOLD));
	}

	@Override
	protected void onInit() {
		layout = new HeaderAndFooterLayout(this, 50, 100);
		layout.addToHeader(new IconTextWidget(this.getTitle(), this.font, ICON));

		GridLayout gridWidget = layout.addToContents(new GridLayout()).spacing(SPACING);
		gridWidget.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);

		// LINE #1

		adder.addChild(Button.builder(Component.literal("Configuration"),
						button -> openScreen(ConfigManager.createConfigGUI(this)))
				.width(BUTTON_WIDTH)
				.build(), 2);

		// LINE #2

		adder.addChild(Button.builder(Component.literal("Reminders"),
						button -> openScreen(ReminderScreen.create(this)))
				.width(HALF_BUTTON_WIDTH)
				.build());

		adder.addChild(Button.builder(Component.literal("Waypoints"),
						button -> openScreen(WaypointScreen.create(this)))
				.width(HALF_BUTTON_WIDTH)
				.build());

		// LINE #3

		adder.addChild(Button.builder(Component.literal("Stonks"),
						button -> openScreen(new StonksSearchScreen(this)))
				.tooltip(Tooltip.create(Component.literal("Search a SkyBlock item to show more informations about.")))
				.width(HALF_BUTTON_WIDTH)
				.build());

		Button keybinds = Button.builder(Component.literal("Key Shortcuts"),
						button -> openScreen(KeyShortcutScreen.create(this)))
				.tooltip(Tooltip.create(Component.literal("Link Keybinds to commands to be executed.")))
				.width(HALF_BUTTON_WIDTH)
				.build();
		adder.addChild(keybinds);

		// LINE #4

		adder.addChild(Button.builder(CommonComponents.GUI_DONE,
						button -> this.close())
				.width(BUTTON_WIDTH)
				.build(), 2);

		// LINE FOOTER

		GridLayout footerGridWidget = layout.addToFooter(new GridLayout()).spacing(SPACING).rowSpacing(0);
		footerGridWidget.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper footerAdder = footerGridWidget.createRowHelper(2);

		footerAdder.addChild(Button.builder(Component.literal("Modrinth"),
						ConfirmLinkScreen.confirmLink(this, "https://modrinth.com/mod/cariboustonks"))
				.build());

		footerAdder.addChild(Button.builder(Component.literal("GitHub"),
						ConfirmLinkScreen.confirmLink(this, "https://github.com/Siroz555/CaribouStonks"))
				.build());

		Component version = Component.literal(" Version: " + CaribouStonks.VERSION.getFriendlyString());
		addRenderableWidget(new StringWidget(2, this.height - 15,
				this.font.width(version), 10, version, this.font));

		Component cMwa = Component.literal("@Siroz555 ");
		addRenderableWidget(new StringWidget(this.width - this.font.width(cMwa) - 2, height - 15,
				this.font.width(cMwa), 10, cMwa, this.font));

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		layout.arrangeElements();
	}

	private void openScreen(Screen screen) {
		if (this.minecraft != null) {
			this.minecraft.setScreen(screen);
		}
	}

	@Override
	public void onRender(GuiGraphics context, int mouseX, int mouseY, float delta) {
		//this.renderBackground(context, mouseX, mouseY, delta);
        /*RenderSystem.enableBlend();
        context.drawTexture(BACKGROUND_TEXTURE,
                0, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();*/
		super.onRender(context, mouseX, mouseY, delta);
	}

	private static class IconTextWidget extends StringWidget {
		private final Identifier icon;

		IconTextWidget(Component message, Font textRenderer, Identifier icon) {
			super(message, textRenderer);
			this.icon = icon;
		}

		@Override
		public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			Component text = this.getMessage();
			Font textRenderer = this.getFont();

			int width = this.getWidth();
			int textWidth = textRenderer.width(text);
			float horizontalAlignment = 0.5f;
			int x = this.getX() + 17 + Math.round(horizontalAlignment * (float) (width - textWidth));
			int y = this.getY() + (this.getHeight() - textRenderer.lineHeight) / 2;
			FormattedCharSequence orderedText = textWidth > width ? trim(text, width) : text.getVisualOrderText();

			int iconX = x - 34;
			int iconY = y - 13;

			context.drawString(textRenderer, orderedText, x, y, Colors.WHITE.asInt());
			context.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 0, 0, 32, 32, 32, 32);
		}

		private FormattedCharSequence trim(Component text, int width) {
			Font textRenderer = this.getFont();
			FormattedText stringVisitable = textRenderer.substrByWidth(
					text, width - textRenderer.width(CommonComponents.ELLIPSIS));
			return Language.getInstance().getVisualOrder(FormattedText.composite(stringVisitable, CommonComponents.ELLIPSIS));
		}
	}
}
