package fr.siroz.cariboustonks.screens;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.mod.crash.CrashType;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * Base Mod Screen.
 *
 * <li>{@link #onInit()}</li>
 * <li>{@link #onRender(GuiGraphics, int, int, float)}</li>
 * <li>{@link #onMouseClicked(MouseButtonEvent, boolean)}</li>
 * <li>{@link #close()}</li>
 */
public abstract class CaribousStonksScreen extends Screen {

	protected static final Component CONFIRM_SCREEN_UNSAVED_CHANGE = Component.literal("Unsaved Changes");
	protected static final Component CONFIRM_SCREEN_PROMPT = Component.literal("Are you sure you want to exit this screen? Any changes will not be saved!");
	protected static final Component CONFIRM_SCREEN_QUIT_MESSAGE = Component.literal("Quit & Discard Changes");

	protected CaribousStonksScreen(Component title) {
		super(title);
	}

	private void failure(String method, Throwable throwable) {
		Minecraft.getInstance().setScreen(null);
		CaribouStonks.mod().getCrashManager().reportCrash(CrashType.SCREEN,
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
	public final void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
		try {
			onRender(guiGraphics, mouseX, mouseY, deltaTicks);
		} catch (Throwable throwable) {
			failure("render", throwable);
		}
	}

	public void onRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
		try {
			return onMouseClicked(click, doubled);
		} catch (Throwable throwable) {
			failure("mouseClicked", throwable);
		}

		return false;
	}

	public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
		return super.mouseClicked(click, doubled);
	}

	@Override
	public final void onClose() {
		try {
			close();
		} catch (Throwable throwable) {
			failure("close", throwable);
		}
	}

	public void close() {
		super.onClose();
	}

	@Override
	public void renderBackground(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		renderBlurredBackground(guiGraphics);
	}
}
