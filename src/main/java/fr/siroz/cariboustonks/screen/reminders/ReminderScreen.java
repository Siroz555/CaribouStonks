package fr.siroz.cariboustonks.screen.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReminderScreen extends CaribousStonksScreen {

    @Nullable
    private final Screen parent;
    protected List<Pair<Reminder, TimedObject>> reminders;
    private Button optionButton;

    private ReminderScreen(@Nullable Screen parent) {
        super(Component.literal("Reminders").withStyle(ChatFormatting.BOLD));
        this.parent = parent;
        this.reminders = CaribouStonks.managers().getManager(ReminderManager.class).getReminders();
    }

    @Contract("_ -> new")
    public static @NotNull ReminderScreen create(@Nullable Screen parent) {
        return new ReminderScreen(parent);
    }

    @Override
    protected void onInit() {
        addRenderableWidget(new ReminderListWidget(this.minecraft, this, this.width, this.height - 112, 40, 36));

        this.optionButton = addRenderableWidget(Button.builder(Component.literal("Options"), (b) -> {

        }).bounds(this.width / 2 - 154, this.height - 38, 150, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (b) -> setScreen(parent))
                .bounds(this.width / 2 + 4, this.height - 38, 150, 20).build());

        this.itemSelected(null);
    }

    @Override
    public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.onRender(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(this.font, title, this.width / 2, 8, Colors.WHITE.asInt());
    }

    @Override
    public void close() {
        setScreen(parent);
    }

    private void setScreen(Screen screen) {
		minecraft.setScreen(screen);
    }

    public void itemSelected(Object o) {
        if (o == null) {
            this.optionButton.setMessage(Component.literal("Options"));
            this.optionButton.active = false;
        } else {
            this.optionButton.setMessage(Component.literal("Options"));
            this.optionButton.active = true;
        }
    }
}
