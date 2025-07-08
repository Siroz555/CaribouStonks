package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

abstract class AbstractCategory {

	protected static final String SPACE = "\n";

	protected static final Text BETA = Text.literal(" (Beta)").formatted(Formatting.RED);

	protected Config defaults;
    protected Config current;

    protected AbstractCategory(Config defaults, Config current) {
        this.defaults = defaults;
        this.current = current;
    }

    public abstract ConfigCategory create();

	public ButtonOption shortcutToKeybindsOptions() {
		return ButtonOption.createBuilder()
				.name(Text.literal("Edit keybind"))
				.action((screen, opt) -> MinecraftClient.getInstance()
						.setScreen(new KeybindsScreen(screen, MinecraftClient.getInstance().options)))
				.text(Text.literal("Open the Keybinds Options"))
				.build();
	}

    public BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
        //return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true);
        return BooleanControllerBuilder.create(opt).formatValue(b -> b
				? Text.literal("Enabled").formatted(Formatting.GREEN)
				: Text.literal("Disabled").formatted(Formatting.RED));
    }

	public IntegerSliderControllerBuilder createIntegerPercentController(Option<Integer> opt, int max) {
		return IntegerSliderControllerBuilder.create(opt)
				.range(1, max)
				.step(1)
				.formatValue(i -> Text.of(i + " %"));
	}

	public IntegerSliderControllerBuilder createIntegerSecondesController(Option<Integer> opt, int max) {
		return IntegerSliderControllerBuilder.create(opt)
				.range(1, max)
				.step(1)
				.formatValue(i -> i > 1 ? Text.of(i + " seconds") : Text.of(i + " second"));
	}

	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingController(Option<E> opt) {
		return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.stateManager().get().getClass());
	}
}
