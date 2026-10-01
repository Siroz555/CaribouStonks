package fr.siroz.cariboustonks.features.ui;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.MinecraftUtil;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ChatPromptFeature extends Feature {
	/**
	 * {@link ClickEvent.Custom}:
	 *
	 * <li>A: "Select an option: [What's an Abiphone?]"</li>
	 * <p>- 1 option : Payload = {@code npcId:"alda",responseKey:"x_2"} (x3).
	 * <p>- "x_2" peut être aussi "x_7" et il a 3x un payload, 2 pour les "[" et 1 pour le message au centre
	 *
	 * <li>B: "Train Kaus? [YES] [NO]"</li>
	 * <p>- 2 options : Payload = {@code npcId:"kaus",responseKey:"yes"} || {@code npcId:"kaus",responseKey:"no"} (x1)
	 * <p>- Peut varier entre NPC bien sûr...
	 * <p>- Ce double choix est ignoré dans la logique ici
	 *
	 * <li>C: "[GIVE ITEM]"</li>
	 * <p>- Payload = {@code npcId:"meteorologist",responseKey:"pay"} (x1)
	 */
	private static final Identifier DIALOGUE_RESPONSE_ID = Identifier.fromNamespaceAndPath(
			"skyblock", "dialogue_response"
	);
	/**
	 * {@link ClickEvent.RunCommand}:
	 *
	 * <li>A: "✆ RING...  [PICK UP]"</li>
	 * <p>- Command = /cb 5c617cf9-bdd6-47df-bfe1-0384e4e8b090
	 *
	 * <li>B: "HUNT - 5 of your Hunting Traps caught something!"</li>
	 * <p>- Command = /cb f546bc4f-a300-4f44-a465-fe57969bb3ac
	 */
	private static final Pattern CALLBACK_COMMAND = Pattern.compile(
			"^/cb [0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE
	);
	private static final Duration CLICK_WINDOW = Duration.ofSeconds(10);
	private static final Cooldown SEND_COOLDOWN = Cooldown.of(250, TimeUnit.MILLISECONDS);

	private @Nullable PendingPrompt pending;

	public ChatPromptFeature() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::handleChatMessageEvent);
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> this.handleScreenAfterInitEvent(screen));
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock()
				&& this.config().uiAndVisuals.chatPrompt.clickAnywhereOnScreenToAcceptPrompt;
	}

	@Override
	protected void onClientJoinServer() {
		pending = null;
	}

	private void handleChatMessageEvent(Component message) {
		if (!isEnabled()) return;

		ClickEvent event = resolvePromptEvent(collectPromptEvents(message));
		if (event == null) return;

		PendingPrompt previous = pending;
		boolean alreadyAnnounced = previous != null && !previous.isExpired() && previous.event().equals(event);

		// La fenêtre repart de zéro, même pour un prompt répété (RING... RING...)
		pending = PendingPrompt.of(event);
		if (!alreadyAnnounced) {
			PlayerContext.sendMessageWithPrefix(
					Component.literal("Click anywhere on screen to accept the prompt!").withColor(Colors.AQUA_RGB).withStyle(ChatFormatting.ITALIC)
			);
		}
	}

	private void handleScreenAfterInitEvent(Screen screen) {
		if (!isEnabled() || !(screen instanceof ChatScreen)) return;

		ScreenMouseEvents.beforeMouseClick(screen)
				.register((_, click) -> onChatScreenClick(screen, click.x(), click.y()));
	}

	private void onChatScreenClick(Screen screen, double mouseX, double mouseY) {
		PendingPrompt prompt = pending;
		if (prompt == null) return;

		if (prompt.isExpired()) {
			pending = null;
			return;
		}

		Style clickedStyle = new ActiveTextCollector.ClickableStyleFinder(screen.getFont(), (int) mouseX, (int) mouseY)
				.includeInsertions(false)
				.result();
		ClickEvent clickedEvent = clickedStyle != null ? clickedStyle.getClickEvent() : null;

		// Clic sur un élément interactif du chat : vanilla s'en occupe.
		// Si c'est le prompt lui-même, il est confirmé à la main : il n'est pas renvoyé
		if (clickedEvent != null) {
			if (clickedEvent.equals(prompt.event())) {
				pending = null;
			}
			return;
		}

		// le prompt reste en attente, le prochain clic retentera
		if (SEND_COOLDOWN.test()) {
			pending = null;
			send(prompt.event());
		}
	}

	private List<ClickEvent> collectPromptEvents(@NonNull Component message) {
		// Collecte les options de prompt d'un message, sans doublon : une même option peut
		// être répartie sur plusieurs segments (ex : "[", "YES", "]") qui portent le même event.
		Set<ClickEvent> events = new LinkedHashSet<>();
		message.visit((style, text) -> {
			ClickEvent event = style.getClickEvent();
			if (event != null && isPromptEvent(event, text)) {
				events.add(event);
			}
			return Optional.empty(); // continue la traversée
		}, Style.EMPTY);
		return List.copyOf(events);
	}

	private @Nullable ClickEvent resolvePromptEvent(@NonNull List<ClickEvent> options) {
		// Seul endroit qui décide quelle option devient le prompt en attente.
		// Uniquement s'il n'y a une option. Plusieurs options (ex : "Train Kaus? [YES] [NO]") sont ignorées.
		return options.size() == 1 ? options.getFirst() : null;
	}

	private boolean isPromptEvent(@NonNull ClickEvent event, String label) {
		return switch (event) {
			case ClickEvent.Custom custom -> DIALOGUE_RESPONSE_ID.equals(custom.id());
			case ClickEvent.RunCommand(String command) -> CALLBACK_COMMAND.matcher(command).matches() || isLegacyConfirmation(command, label);
			default -> false;
		};
	}

	private void send(@NonNull ClickEvent event) {
		switch (event) {
			case ClickEvent.RunCommand(String command) -> PlayerContext.sendCommandToServer(command, true);
			case ClickEvent.Custom(Identifier id, Optional<Tag> payload) -> ClientContext.sendPacket(new ServerboundCustomClickActionPacket(id, payload));
			default -> {
			}
		}
	}

	private record PendingPrompt(ClickEvent event, long expiresAtMillis) {
		public static @NonNull PendingPrompt of(ClickEvent event) {
			return new PendingPrompt(event, System.currentTimeMillis() + CLICK_WINDOW.toMillis());
		}

		public boolean isExpired() {
			return System.currentTimeMillis() >= expiresAtMillis;
		}
	}

	//  ===================== LEGACY =====================

	/**
	 * <p>{@link ClickEvent.RunCommand}:
	 *  Anciens prompts "[Yes]" via /chatprompt et /selectnpcoption.
	 * Dans mes tests, je n'ai pas trouvé de NPC, et il n'y a pas de Carnival par exemple -_-
	 */
	private static final Set<String> LEGACY_CONFIRMATION_PHRASES = Set.of(
			"[Yes]", "[YES]", // Basic
			"[Sure thing, partner!]", "[Aye sure do!]", "[You guessed it!]" // Carnival
	);
	private static final Pattern WHITESPACES = Pattern.compile("\\s+");

	private boolean isLegacyConfirmation(@NonNull String command, String label) {
		if (!command.startsWith("/chatprompt") || !command.startsWith("/selectnpcoption")) return false;

		String normalized = MinecraftUtil.stripColor(WHITESPACES.matcher(label).replaceAll(" ").trim());
		return LEGACY_CONFIRMATION_PHRASES.contains(normalized);
	}
}
