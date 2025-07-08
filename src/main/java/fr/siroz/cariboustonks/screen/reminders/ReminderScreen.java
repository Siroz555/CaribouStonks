package fr.siroz.cariboustonks.screen.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.screen.CaribousStonksScreen;
import fr.siroz.cariboustonks.util.colors.Colors;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReminderScreen extends CaribousStonksScreen {

    @Nullable
    private final Screen parent;
    protected List<Pair<Reminder, TimedObject>> reminders;
    private ButtonWidget optionButton;

    private ReminderScreen(@Nullable Screen parent) {
        super(Text.literal("Reminders").formatted(Formatting.BOLD));
        this.parent = parent;
        this.reminders = CaribouStonks.managers().getManager(ReminderManager.class).getReminders();
    }

    @Contract("_ -> new")
    public static @NotNull ReminderScreen create(@Nullable Screen parent) {
        return new ReminderScreen(parent);
    }

    @Override
    protected void onInit() {
        addDrawableChild(new ReminderListWidget(this.client, this, this.width, this.height - 112, 40, 36));

        this.optionButton = addDrawableChild(ButtonWidget.builder(Text.literal("Options"), (b) -> {

        }).dimensions(this.width / 2 - 154, this.height - 38, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (b) -> setScreen(parent))
                .dimensions(this.width / 2 + 4, this.height - 38, 150, 20).build());

        this.itemSelected(null);
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
        super.onRender(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, 8, Colors.WHITE.asInt());
    }

    @Override
    public void onClose() {
        setScreen(parent);
    }

    private void setScreen(Screen screen) {
        assert client != null;
        client.setScreen(screen);
    }

    public void itemSelected(Object o) {
        if (o == null) {
            this.optionButton.setMessage(Text.literal("Options"));
            this.optionButton.active = false;
        } else {
            this.optionButton.setMessage(Text.literal("Options"));
            this.optionButton.active = true;
        }
    }
}
