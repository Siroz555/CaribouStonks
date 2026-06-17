package fr.siroz.cariboustonks.features.events;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.ColorHighlight;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RaffleTaskFeature extends Feature {
	private static final Pattern RAFFLE_GUIS_PATTERN = Pattern.compile("^(Raffle Tasks|Year 500.*Raffle Box)$");
	private static final Pattern RAFFLE_BOX_PATTERN = Pattern.compile("^Year 500.*Raffle Box$");
	private static final Pattern CHAT_TASK_COMPLETION_PATTERN = Pattern.compile("^RAFFLE TASK! You completed the (.*) raffle task.*");
	private static final String CHAT_TASKS_REFRESHED = "Your Raffle Tasks have refreshed! Click HERE to view your new ones!";
	private static final int TASKS_INFO_POS = 51;

	private final Map<String, RaffleTask> trackedTasks = new HashMap<>();
	private Instant resetTime = null;
	private boolean configNotified = false;

	public RaffleTaskFeature() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> this.onScreen(screen));

		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(RAFFLE_GUIS_PATTERN))
				.content(slots -> {
					List<ColorHighlight> highlights = new ArrayList<>();

					Screen screen = ClientContext.getScreen();
					String screenTitle = screen != null ? screen.getTitle().getString() : null;
					if (screenTitle != null) {
						Matcher raffleBoxMatcher = RAFFLE_BOX_PATTERN.matcher(screenTitle);
						if (raffleBoxMatcher.find()) {
							extractTimeLeft(slots.getOrDefault(TASKS_INFO_POS, null));
							return highlights;
						}
					}

					slots.forEach((i, itemStack) -> {
						if (isTaskItem(itemStack)) {
							boolean completed = extractTask(itemStack);

							if (completed) highlights.add(ColorHighlight.green(i, 0.5f));
							else highlights.add(ColorHighlight.red(i, 0.5f));
						}
					});
					return highlights;
				})
				.build());

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("special_raffle_hud"))
				.hud(new MultiElementHud(
						this::isEnabled,
						new HudElementTextBuilder()
								.append(Component.literal("§6§lRaffle Tasks §r§7- §e1h 37m 10s"))
								.append(Component.literal("§c§l✖ §r§7- ?"))
								.append(Component.literal("§c§l✖ §r§7- ?"))
								.append(Component.literal("§c§l✖ §r§7- ?"))
								.build(),
						this::getHudLines,
						this.config().events.raffle.hud,
						20,
						20
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& this.config().events.raffle.hud.enabled
				&& (SkyBlockAPI.getTime().year() == 500 || DeveloperTools.isInDevelopment());
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component component) {
		if (!isEnabled()) return;

		String message = StonksUtils.stripColor(component.getString());
		Matcher matcher = CHAT_TASK_COMPLETION_PATTERN.matcher(message);
		if (matcher.find()) {
			try {
				String completedTaskName = matcher.group(1).trim();
				if (trackedTasks.containsKey(completedTaskName)) {
					trackedTasks.get(completedTaskName).completed = true;
				}
			} catch (Exception _) {
			}
		}

		if (message.equalsIgnoreCase(CHAT_TASKS_REFRESHED)) {
			trackedTasks.clear();
		}
	}

	@EventHandler(event = "ScreenEvents.AFTER_INIT")
	private void onScreen(Screen screen) {
		if (SkyBlockAPI.isOnSkyBlock() && SkyBlockAPI.getTime().year() == 500) {
			Matcher raffleBoxMatcher = RAFFLE_BOX_PATTERN.matcher(screen.getTitle().getString());
			if (!configNotified && !this.config().events.raffle.hud.enabled && raffleBoxMatcher.find()) {
				configNotified = true;
				PlayerContext.sendMessageWithPrefix(Component.empty()
						.append(Component.literal("RAFFLE!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
						.append(Component.literal(" You can enable Raffle Tasks HUD in the Misc Category > Events - Raffle").withStyle(ChatFormatting.GREEN))
				);
			}
		}
	}

	private void extractTimeLeft(@Nullable ItemStack tasksInfo) {
		if (tasksInfo == null) return;

		for (Component lineLore : ItemUtils.getLore(tasksInfo)) {
			String line = lineLore.getString().trim();
			if (line.startsWith("Time until reset:")) {
				String timeStr = line.substring("Time until reset:".length()).trim();
				Duration duration = TimeUtils.extractDuration(timeStr);
				if (!duration.isZero()) resetTime = Instant.now().plus(duration);
				break;
			}
		}
	}

	private boolean extractTask(ItemStack itemStack) {
		Component taskName = itemStack.getHoverName();
		String rawTaskName = taskName.getString();
		boolean completed = false;
		Component objective = null;
		boolean capturingObjective = false;
		for (Component loreLine : ItemUtils.getLore(itemStack)) {
			String line = loreLine.getString().trim();
			if (line.equals("COMPLETE")) {
				completed = true;
				break;
			} else if (line.equals("INCOMPLETE")) {
				break;
			} else if (line.endsWith("Task") && (line.contains("Easy") || line.contains("Medium") || line.contains("Hard"))) {
				capturingObjective = true;
			} else if (capturingObjective && !line.isEmpty()) {
				if (objective == null) {
					objective = loreLine.copy();
				} else {
					objective = objective.copy().append(Component.literal(" ")).append(loreLine.copy());
				}
			}
		}

		TaskDifficulty difficulty = TaskDifficulty.EASY;
		if (itemStack.is(Items.MAP)) difficulty = TaskDifficulty.MEDIUM;
		else if (itemStack.is(Items.FILLED_MAP)) difficulty = TaskDifficulty.HARD;

		if (objective == null) objective = taskName;

		trackedTasks.put(rawTaskName, new RaffleTask(rawTaskName, taskName, difficulty, objective, completed));

		return completed;
	}

	private void getHudLines(HudElementBuilder builder) {
		Component timeLeft = Component.literal("?").withStyle(ChatFormatting.YELLOW);
		if (resetTime != null && Instant.now().isBefore(resetTime)) {
			timeLeft = Component.literal(TimeUtils.getDurationFormatted(resetTime, false)).withStyle(ChatFormatting.YELLOW);
		}

		builder.appendTitle(Component.empty()
				.append(Component.literal("Raffle Tasks").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
				.append(timeLeft));

		if (trackedTasks.isEmpty()) {
			builder.appendLine(Component.literal("Open Raffle Menu to load tasks").withStyle(ChatFormatting.GRAY));
			return;
		}

		builder.appendSpace();

		// Easy -> Medium -> Hard
		List<RaffleTask> sortedTasks = new ArrayList<>(trackedTasks.values());
		sortedTasks.sort(Comparator.comparing(t -> t.difficulty));

		boolean onlyShowIncomplete = this.config().events.raffle.onlyShowIncomplete;
		for (RaffleTask task : sortedTasks) {
			if (task.completed && onlyShowIncomplete) continue;

			Component status = task.completed
					? Component.literal("✔").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
					: Component.literal("✖").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

			Component objective = task.objective;
			if (task.completed) {
				objective = task.objective.copy().withStyle(ChatFormatting.STRIKETHROUGH);
			}

			builder.appendTableRow(status, objective, Component.empty());
		}
	}

	private boolean isTaskItem(ItemStack stack) {
		return stack.is(Items.PAPER) || stack.is(Items.MAP) || stack.is(Items.FILLED_MAP);
	}

	private enum TaskDifficulty {
		EASY, MEDIUM, HARD
	}

	private static class RaffleTask {
		String rawName;
		Component displayName;
		TaskDifficulty difficulty;
		Component objective;
		boolean completed;

		RaffleTask(String rawName, Component displayName, TaskDifficulty difficulty, Component objective, boolean completed) {
			this.rawName = rawName;
			this.displayName = displayName;
			this.difficulty = difficulty;
			this.objective = objective;
			this.completed = completed;
		}
	}
}
