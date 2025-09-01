package fr.siroz.cariboustonks.manager.hud;

import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be implemented by features that provide a HUD.
 * <p>
 * Implementations are responsible for supplying a specific {@link Hud} instance.
 * <p>
 * <h2>Implementation</h2>
 * For {@link TextHud} usage:
 * <pre>{@code
 * private final Hud hud;
 *
 * public ClassConstructorFeature() {
 * 	this.hud = new TextHud(
 * 			Text.literal("Default Text"),
 * 			this::getLines,
 * 			ConfigManager.getConfig().x.hud,
 * 			20,
 * 			64);
 * }
 * private Text getText() {
 * 	return Text.literal("Hud Text: " + getValue());
 * }
 * }</pre>
 * For {@link MultiElementHud} usage:
 * <pre>{@code
 * private final HudElementBuilder builder;
 * private final Hud hud;
 *
 * public ClassConstructorFeature() {
 * 	this.builder = new HudElementBuilder();
 * 	this.hud = new MultiElementHud(
 * 			this::isEnabled,
 * 			this.getDefault(),
 * 			this::getLines,
 * 			ConfigManager.getConfig().x.hud,
 * 			50,
 * 			64);
 * }
 * private List<? extends HudElement> getLines() {
 * 	builder.clear();
 * 	builder.appendTitle(TITLE)
 * 		.appendSpace()
 * 		.appendLine(Text.literal("Test 1"))
 * 		.appendTableRow(Text.of("Col 1"), Text.of("Col 2"), Text.of("Col 3"))
 * 		.appendIconLine(Items.DIAMOND.getDefaultItemStack(), Text.literal("Item))
 * 		.appendLine(Text.literal("Test 2"));
 * 	return builder.build();
 * }
 * }</pre>
 */
public interface HudProvider {

	/**
	 * Returns the attached layer after the layer with the specified identifier.
	 * The render condition of the layer being attached to.
	 * <p>
	 * {@code Identifier <L>}: the identifier of the layer to add the new layer after.
	 * See {@link VanillaHudElements} for a list of built-in identifiers.
	 * <p>
	 * {@code Identifier <R>}: the layer to add. {@code Unique} identifier for the HUD from {@link #getHud()}.
	 * <p>
	 * <pre>{@code
	 * private static final Identifier HUD_ID = CaribouStonks.identifier("hub_id");
	 *
	 * @Override
	 * public Pair<Identifier, Identifier> getAttachLayerAfter() {
	 *     return Pair.of(IdentifiedLayer.STATUS_EFFECTS, HUD_ID);
	 * }
	 * }</pre>
	 * See {@link HudElementRegistry#attachElementAfter(Identifier, Identifier, HudElement)}
	 *
	 * @return a pair of identifiers
	 */
	@NotNull
	Pair<Identifier, Identifier> getAttachLayerAfter();

	/**
	 * Returns the HUD associated with this provider.
	 *
	 * @return an instance of {@link Hud}
	 */
	@NotNull
	Hud getHud();
}
