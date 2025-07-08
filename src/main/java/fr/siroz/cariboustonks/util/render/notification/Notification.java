package fr.siroz.cariboustonks.util.render.notification;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public final class Notification {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static final SystemToast.Type STONKS_SYSTEM = new SystemToast.Type(10000L); // 10000L
	public static final SystemToast.Type STONKS_SYSTEM_INFINITE = new SystemToast.Type(1000000L);

	private Notification() {
	}

	public static void show(MutableText text, ItemStack icon) {
		show(new StonksToast(text, icon));
	}

	public static void show(Toast toast) {
		CLIENT.getToastManager().add(toast);
	}

	public static void showSystem(@NotNull String description) {
		showSystem("CaribouStonks", description);
	}

	public static void showSystem(@NotNull String title, @NotNull String description) {
		SystemToast systemToast = SystemToast.create(CLIENT, STONKS_SYSTEM, Text.literal(title), Text.literal(description));
		CLIENT.getToastManager().add(systemToast);
	}
}
