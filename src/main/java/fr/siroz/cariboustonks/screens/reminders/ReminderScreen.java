package fr.siroz.cariboustonks.screens.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.screens.CaribousStonksScreen;
import fr.siroz.cariboustonks.systems.ReminderSystem;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ReminderScreen extends CaribousStonksScreen {

    @Nullable
    private final Screen parent;
    protected List<Pair<ReminderComponent, TimedObjectModel>> reminders;
    private Button createButton;
    private Button optionButton;

    private ReminderScreen(@Nullable Screen parent) {
        super(Component.literal("Reminders").withStyle(ChatFormatting.BOLD));
        this.parent = parent;
        this.reminders = CaribouStonks.systems().getSystem(ReminderSystem.class).getReminders();
    }

    public static @NonNull ReminderScreen create(@Nullable Screen parent) {
        return new ReminderScreen(parent);
    }

    @Override
    protected void onInit() {
        addRenderableWidget(new ReminderListWidget(this.minecraft, this, this.width, this.height - 112, 40, 36));

		GridLayout grid = new GridLayout();
		grid.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);

		GridLayout.RowHelper adder = grid.createRowHelper(2);
		createButton = adder.addChild(Button.builder(Component.literal("Create custom Reminder"), _b -> {

		}).tooltip(Tooltip.create(Component.literal("3-5 Business days"))).build());

		optionButton = adder.addChild(Button.builder(Component.literal("Options"), _b -> {

		}).tooltip(Tooltip.create(Component.literal("3-5 Business days"))).build());

		adder.addChild(Button.builder(CommonComponents.GUI_DONE, _b -> close()).width(310).build(), 2);

		grid.arrangeElements();
		FrameLayout.centerInRectangle(grid, 0, this.height - 64, this.width, 64);
		grid.visitWidgets(this::addRenderableWidget);

		this.itemSelected(null);
    }

    @Override
    public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.onRender(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(this.font, title, this.width / 2, 8, Colors.WHITE.asInt());
    }

    @Override
    public void close() {
		minecraft.setScreen(parent);
    }

    public void itemSelected(Object o) {
		createButton.active = false;
		optionButton.active = false;
//        if (o == null) {
//            this.optionButton.setMessage(Component.literal("Options"));
//            this.optionButton.active = false;
//        } else {
//            this.optionButton.setMessage(Component.literal("Options"));
//            this.optionButton.active = true;
//        }
    }
}
