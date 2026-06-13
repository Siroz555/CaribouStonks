package fr.siroz.cariboustonks.core.mod;

import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.animal.parrot.Parrot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SecretModFeatures {

	@Nullable
	private SplashRenderer splashText = null;

	private static final List<SplashRenderer> PREDEFINED_SPLASH = List.of(
			new SplashRenderer(Component.literal("/visit Siroz555 ;)").withStyle(Style.EMPTY.withColor(Colors.DARK_AQUA_RGB))),
			new SplashRenderer(Component.literal("Crafting like a French baker").withStyle(Style.EMPTY.withColor(-256))),
			new SplashRenderer(Component.literal("Foraging Update?").withStyle(Style.EMPTY.withColor(Colors.DARK_GREEN_RGB))),
			new SplashRenderer(Component.literal("3-5 Business days").withStyle(Style.EMPTY.withColor(Colors.LIGHT_PURPLE_RGB))),
			new SplashRenderer(Component.literal("Galatea!").withStyle(Style.EMPTY.withColor(Colors.DARK_GREEN_RGB))),
			new SplashRenderer(Component.literal("SkyBlock!").withStyle(Style.EMPTY.withColor(-256)))
	);

	public SecretModFeatures() {
		Calendar today = Calendar.getInstance();
		int day = today.get(Calendar.DAY_OF_MONTH);
		int month = today.get(Calendar.MONTH);

		if (day == 11 && month == Calendar.JUNE) {
			this.splashText = new SplashRenderer(Component.literal("Happy birthday SkyBlock!").withStyle(Style.EMPTY.withColor(-256)));
			return;
		}

		Random random = new Random();
		if (random.nextInt(50) == 0) {
			this.splashText = PREDEFINED_SPLASH.get(random.nextInt(PREDEFINED_SPLASH.size()));
		}

		ClientCommandRegistrationCallback.EVENT.register((d, _) -> d.register(
				ClientCommands.literal("cariboustonks").then(ClientCommands.literal("parrot").executes(context -> {
					var player = context.getSource().getPlayer();
					boolean hasLeft = player.getShoulderParrotLeft().isPresent();
					boolean hasRight = player.getShoulderParrotRight().isPresent();

					if (hasLeft || hasRight) {
						player.setShoulderParrotLeft(Optional.empty());
						player.setShoulderParrotRight(Optional.empty());
						PlayerContext.sendMessageWithPrefix(Component.literal("The Parrot is gone! ;(").withColor(Colors.RED_RGB));
					} else {
						Parrot.Variant[] variants = Parrot.Variant.values();
						Parrot.Variant variant = variants[player.getRandom().nextInt(variants.length)];
						if (player.getRandom().nextBoolean()) {
							player.setShoulderParrotLeft(Optional.of(variant));
						} else {
							player.setShoulderParrotRight(Optional.of(variant));
						}
						PlayerContext.sendMessageWithPrefix(Component.literal("A Parrot has appeared!").withColor(Colors.GREEN_RGB));
					}
					return 1;
				}))
		));
	}

	public @NonNull Optional<SplashRenderer> getSplashText() {
		return Optional.ofNullable(splashText);
	}
}
