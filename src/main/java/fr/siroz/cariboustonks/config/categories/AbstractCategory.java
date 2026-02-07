package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

abstract class AbstractCategory {

	protected static final String SPACE = "\n";

	protected static final Component BETA = Component.literal(" (Beta)").withStyle(ChatFormatting.RED);

	protected Config defaults;
    protected Config current;

    protected AbstractCategory(Config defaults, Config current) {
        this.defaults = defaults;
        this.current = current;
    }

    public abstract ConfigCategory create();

	protected void openScreen(@Nullable Screen screen) {
		Minecraft.getInstance().setScreen(screen);
	}

	public ButtonOption shortcutToKeybindsOptions() {
		return ButtonOption.createBuilder()
				.name(Component.literal("Edit keybind"))
				.action((screen, opt) -> Minecraft.getInstance()
						.setScreen(new KeyBindsScreen(screen, Minecraft.getInstance().options)))
				.text(Component.literal("Open the Keybinds Options"))
				.build();
	}

    public BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
        return BooleanControllerBuilder.create(opt).formatValue(b -> b
				? Component.literal("Enabled").withStyle(ChatFormatting.GREEN)
				: Component.literal("Disabled").withStyle(ChatFormatting.RED));
    }

	public BooleanControllerBuilder createYesNoController(Option<Boolean> opt) {
		return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true);
	}

	public IntegerSliderControllerBuilder createIntegerPercentController(Option<Integer> opt, int max) {
		return IntegerSliderControllerBuilder.create(opt)
				.range(1, max)
				.step(1)
				.formatValue(i -> Component.nullToEmpty(i + " %"));
	}

	public IntegerSliderControllerBuilder createIntegerSecondesController(Option<Integer> opt, int max) {
		return IntegerSliderControllerBuilder.create(opt)
				.range(1, max)
				.step(1)
				.formatValue(i -> i > 1 ? Component.nullToEmpty(i + " seconds") : Component.nullToEmpty(i + " second"));
	}

	public IntegerSliderControllerBuilder createIntegerMsController(Option<Integer> opt, int max) {
		return IntegerSliderControllerBuilder.create(opt)
				.range(1, max)
				.step(1)
				.formatValue(i -> Component.nullToEmpty(i + " ms"));
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingController(Option<E> opt) {
		return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.stateManager().get().getClass());
	}
}
