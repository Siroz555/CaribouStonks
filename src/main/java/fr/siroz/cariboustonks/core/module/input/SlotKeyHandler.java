package fr.siroz.cariboustonks.core.module.input;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a functional interface designed to handle key press events in a specific context
 * involving GUI screens.
 */
@FunctionalInterface
public interface SlotKeyHandler {

    /**
     * Handles a key press event within a GUI screen context, specifying the key pressed
     * and the slot within the screen that may be interacted with.
     *
     * @param screen   the screen where the key press event occurred
     * @param slot     the slot instance within the screen that may be involved in the key press event
     */
    void onKeyPressed(@NotNull Screen screen, @NotNull Slot slot);
}
