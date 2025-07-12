package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.stonks.search.StonksSearchScreen;
import fr.siroz.cariboustonks.screen.keyshortcut.KeyShortcutScreen;
import fr.siroz.cariboustonks.screen.reminders.ReminderScreen;
import fr.siroz.cariboustonks.screen.waypoints.WaypointScreen;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

public class CaribouStonksMenuScreen extends CaribousStonksScreen {

	private static final Identifier ICON = CaribouStonks.identifier("icon.png");
	//private static final Identifier BACKGROUND_TEXTURE = CaribouStonks.identifier("textures/screen/background_splash.png");

	private static final int SPACING = 8;
	private static final int BUTTON_WIDTH = 210;
	private static final int HALF_BUTTON_WIDTH = 100;

	private ThreePartsLayoutWidget layout;

	public CaribouStonksMenuScreen() {
		super(Text.literal("CaribouStonks").formatted(Formatting.BOLD));
	}

	@Override
	protected void onInit() {
		layout = new ThreePartsLayoutWidget(this, 50, 100);
		layout.addHeader(new IconTextWidget(this.getTitle(), this.textRenderer, ICON));

		GridWidget gridWidget = layout.addBody(new GridWidget()).setSpacing(SPACING);
		gridWidget.getMainPositioner().alignHorizontalCenter();
		GridWidget.Adder adder = gridWidget.createAdder(2);

		// LINE #1

		adder.add(ButtonWidget.builder(Text.literal("Configuration"),
						button -> openScreen(ConfigManager.createConfigGUI(this)))
				.width(BUTTON_WIDTH)
				.build(), 2);

		// LINE #2

		adder.add(ButtonWidget.builder(Text.literal("Reminders"),
						button -> openScreen(ReminderScreen.create(this)))
				.width(HALF_BUTTON_WIDTH)
				.build());

		adder.add(ButtonWidget.builder(Text.literal("Waypoints"),
						button -> openScreen(WaypointScreen.create(this)))
				.width(HALF_BUTTON_WIDTH)
				.build());

		// LINE #3

		adder.add(ButtonWidget.builder(Text.literal("Stonks"),
						button -> openScreen(new StonksSearchScreen(this)))
				.tooltip(Tooltip.of(Text.literal("Search a SkyBlock item to show more informations about.")))
				.width(HALF_BUTTON_WIDTH)
				.build());

		ButtonWidget keybinds = ButtonWidget.builder(Text.literal("Key Shortcuts"),
						button -> openScreen(KeyShortcutScreen.create(this)))
				.tooltip(Tooltip.of(Text.literal("Link Keybinds to commands to be executed.")))
				.width(HALF_BUTTON_WIDTH)
				.build();
		adder.add(keybinds);

		// LINE #4

		adder.add(ButtonWidget.builder(ScreenTexts.DONE,
						button -> this.close())
				.width(BUTTON_WIDTH)
				.build(), 2);

		// LINE FOOTER

		GridWidget footerGridWidget = layout.addFooter(new GridWidget()).setSpacing(SPACING).setRowSpacing(0);
		footerGridWidget.getMainPositioner().alignHorizontalCenter();
		GridWidget.Adder footerAdder = footerGridWidget.createAdder(2);

		footerAdder.add(ButtonWidget.builder(Text.literal("Modrinth"),
						ConfirmLinkScreen.opening(this, "https://modrinth.com/"))
				.build());

		footerAdder.add(ButtonWidget.builder(Text.literal("GitHub"),
						ConfirmLinkScreen.opening(this, "https://github.com/Siroz555/CaribouStonks"))
				.build());

		Text version = Text.literal(" Version: " + CaribouStonks.VERSION);
		addDrawableChild(new TextWidget(2, this.height - 15,
				this.textRenderer.getWidth(version), 10, version, this.textRenderer));

		Text cMwa = Text.literal("@Siroz555 ");
		addDrawableChild(new TextWidget(this.width - this.textRenderer.getWidth(cMwa) - 2, height - 15,
				this.textRenderer.getWidth(cMwa), 10, cMwa, this.textRenderer));

		layout.refreshPositions();
		layout.forEachChild(this::addDrawableChild);
	}

	@Override
	protected void refreshWidgetPositions() {
		super.refreshWidgetPositions();
		layout.refreshPositions();
	}

	private void openScreen(Screen screen) {
		if (this.client != null) {
			this.client.setScreen(screen);
		}
	}

	@Override
	public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
        /*RenderSystem.enableBlend();
        context.drawTexture(BACKGROUND_TEXTURE,
                0, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();*/
		super.onRender(context, mouseX, mouseY, delta);
	}

	private static class IconTextWidget extends TextWidget {
		private final Identifier icon;

		IconTextWidget(Text message, TextRenderer textRenderer, Identifier icon) {
			super(message, textRenderer);
			this.icon = icon;
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			Text text = this.getMessage();
			TextRenderer textRenderer = this.getTextRenderer();

			int width = this.getWidth();
			int textWidth = textRenderer.getWidth(text);
			float horizontalAlignment = 0.5f;
			int x = this.getX() + 17 + Math.round(horizontalAlignment * (float) (width - textWidth));
			int y = this.getY() + (this.getHeight() - textRenderer.fontHeight) / 2;
			OrderedText orderedText = textWidth > width ? trim(text, width) : text.asOrderedText();

			int iconX = x - 34;
			int iconY = y - 13;

			context.drawTextWithShadow(textRenderer, orderedText, x, y, this.getTextColor());
			context.drawTexture(RenderLayer::getGuiTextured, icon, iconX, iconY, 0, 0, 32, 32, 32, 32);
		}

		private OrderedText trim(Text text, int width) {
			TextRenderer textRenderer = this.getTextRenderer();
			StringVisitable stringVisitable = textRenderer.trimToWidth(
					text, width - textRenderer.getWidth(ScreenTexts.ELLIPSIS));
			return Language.getInstance().reorder(StringVisitable.concat(stringVisitable, ScreenTexts.ELLIPSIS));
		}
	}
}
