package fr.siroz.cariboustonks.core.mod;

import fr.siroz.cariboustonks.util.Client;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.parrot.Parrot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ModFlavor {

	@Nullable
	private SplashRenderer splashText = null;

	private static final List<SplashRenderer> PREDEFINED_SPLASH = List.of(
			new SplashRenderer(Component.literal("/visit Siroz555 ;)").withStyle(ChatFormatting.DARK_AQUA)),
			new SplashRenderer(Component.literal("Crafting like a French baker").withStyle(ChatFormatting.YELLOW)),
			new SplashRenderer(Component.literal("Foraging Update?").withStyle(ChatFormatting.YELLOW)),
			new SplashRenderer(Component.literal("3-5 Business days").withStyle(ChatFormatting.LIGHT_PURPLE)),
			new SplashRenderer(Component.literal("Galatea!").withStyle(ChatFormatting.DARK_GREEN)),
			new SplashRenderer(Component.literal("SkyBlock!").withStyle(ChatFormatting.YELLOW))
	);

	public ModFlavor() {
		Calendar today = Calendar.getInstance();
		int day = today.get(Calendar.DAY_OF_MONTH);
		int month = today.get(Calendar.MONTH);

		if (day == 11 && month == Calendar.JUNE) {
			this.splashText = new SplashRenderer(Component.literal("Happy birthday SkyBlock!"));
			return;
		}

		Random random = new Random();
		if (random.nextInt(50) == 0) {
			this.splashText = PREDEFINED_SPLASH.get(random.nextInt(PREDEFINED_SPLASH.size()));
		}

		ClientCommandRegistrationCallback.EVENT.register((d, _c) -> d.register(
				ClientCommandManager.literal("cariboustonks").then(ClientCommandManager.literal("parrot").executes(context -> {
					var player = context.getSource().getPlayer();
					boolean hasLeft = player.getShoulderParrotLeft().isPresent();
					boolean hasRight = player.getShoulderParrotRight().isPresent();

					if (hasLeft || hasRight) {
						player.setShoulderParrotLeft(Optional.empty());
						player.setShoulderParrotRight(Optional.empty());
						Client.sendMessageWithPrefix(Component.literal("The Parrot is gone! ;(").withStyle(ChatFormatting.RED));
					} else {
						Parrot.Variant[] variants = Parrot.Variant.values();
						Parrot.Variant variant = variants[player.getRandom().nextInt(variants.length)];
						if (player.getRandom().nextBoolean()) {
							player.setShoulderParrotLeft(Optional.of(variant));
						} else {
							player.setShoulderParrotRight(Optional.of(variant));
						}
						Client.sendMessageWithPrefix(Component.literal("A Parrot has appeared!").withStyle(ChatFormatting.GREEN));
					}
					return 1;
				}))
		));
	}

	public @NonNull Optional<SplashRenderer> getSplashText() {
		return Optional.ofNullable(splashText);
	}
}
