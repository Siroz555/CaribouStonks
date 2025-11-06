package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.crash.CrashType;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class CaribousStonksScreen extends Screen {

	protected static final Text CONFIRM_SCREEN_UNSAVED_CHANGE = Text.literal("Unsaved Changes");
	protected static final Text CONFIRM_SCREEN_PROMPT = Text.literal("Are you sure you want to exit this screen? Any changes will not be saved!");
	protected static final Text CONFIRM_SCREEN_QUIT_MESSAGE = Text.literal("Quit & Discard Changes");

	protected CaribousStonksScreen(Text title) {
		super(title);
	}

	private void failure(String method, Throwable throwable) {
		MinecraftClient.getInstance().setScreen(null);
		CaribouStonks.core().getCrashManager().reportCrash(CrashType.SCREEN,
				this.getClass().getSimpleName(),
				this.getClass().getName(),
				method, throwable);
		Client.sendErrorMessage("Forced closing of screen", false);
	}

	@Override
	protected final void init() {
		try {
			onInit();
		} catch (Throwable throwable) {
			failure("init", throwable);
		}
	}

	protected void onInit() {
		super.init();
	}

	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		try {
			onRender(context, mouseX, mouseY, deltaTicks);
		} catch (Throwable throwable) {
			failure("render", throwable);
		}
	}

	public void onRender(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		try {
			return onMouseClicked(click, doubled);
		} catch (Throwable throwable) {
			failure("mouseClicked", throwable);
		}

		return false;
	}

	public boolean onMouseClicked(Click click, boolean doubled) {
		return super.mouseClicked(click, doubled);
	}

	@Override
	public final void close() {
		try {
			onClose();
		} catch (Throwable throwable) {
			failure("close", throwable);
		}
	}

	public void onClose() {
		super.close();
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		applyBlur(context);
	}
}
