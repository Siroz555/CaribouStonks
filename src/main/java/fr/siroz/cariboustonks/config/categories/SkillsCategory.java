package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@SuppressWarnings("checkstyle:linelength")
public class SkillsCategory extends AbstractCategory {

    public SkillsCategory(Config defaults, Config current) {
        super(defaults, current);
    }

    @Override
    public ConfigCategory create() {
        return ConfigCategory.createBuilder()
                .name(Text.literal("Skills"))
                .tooltip(Text.literal("Skills-related Settings"))
                .group(OptionGroup.createBuilder()
                        .name(Text.literal("Combat").formatted(Formatting.BOLD))
                        .description(OptionDescription.of(
                                Text.literal("Combat-related Settings")))
                        .collapsed(false)
						/*.option(LabelOption.create(Text.literal("| Cocooned Mobs").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Cocooned Warning"))
								.description(OptionDescription.of(
										Text.literal("Allows you to be alerted when you kill a mob and it is cocooned."),
										Text.literal("Note: The alert is triggered only for reforging Bloodshot on the Primordial Belt. (Shriveled Cornea Reforge Stone)").formatted(Formatting.YELLOW),
										Text.literal("See the “Slayer” section for features related to the Primordial Belt for Slayers.").formatted(Formatting.ITALIC)))
								.binding(defaults.combat.cocoonedMob.cocoonedWarning,
										() -> current.combat.cocoonedMob.cocoonedWarning,
										newValue -> current.combat.cocoonedMob.cocoonedWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Text.empty()))*/
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Low Health Warning"))
                                .description(OptionDescription.of(
                                        Text.literal("Displays red around the screen when life points are low.")))
                                .binding(defaults.combat.lowHealthWarning.lowHealthWarningEnabled,
                                        () -> current.combat.lowHealthWarning.lowHealthWarningEnabled,
                                        newValue -> current.combat.lowHealthWarning.lowHealthWarningEnabled = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("Low Health Warning - Threshold"))
                                .description(OptionDescription.of(
                                        Text.literal("If the red screen display is enabled, allows you to modify in % when the warning will be triggered.")))
                                .binding(defaults.combat.lowHealthWarning.lowHealthWarningThreshold,
                                        () -> current.combat.lowHealthWarning.lowHealthWarningThreshold,
                                        newValue -> current.combat.lowHealthWarning.lowHealthWarningThreshold = newValue)
                                .controller(opt -> createIntegerPercentController(opt, 50))
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.literal("Farming - Garden").formatted(Formatting.BOLD))
                        .description(OptionDescription.of(
                                Text.literal("Garden settings")))
                        .collapsed(false)
                        .option(ButtonOption.createBuilder()
                                .name(Text.literal("Locking the camera during farming"))
								.text(Text.literal("/lockMouse"))
                                .description(OptionDescription.of(
                                        Text.literal("Allows you to block the movements of the mouse during farming."),
                                        Text.literal(SPACE + "Use /lockMouse or go to KeyBinds Options.")))
                                .action((yaclScreen, buttonOption) -> {
                                    Client.sendMessageWithPrefix(Text.literal("Use /lockMouse or go to KeyBinds Options."));
                                })
                                .build())
                        .option(this::shortcutToKeybindsOptions)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Locating Pests"))
                                .description(OptionDescription.of(
                                        Text.literal("Locate Pests with your Vacuum, creating a Guess Waypoint. A line from your cursor to the nearest Pest will also be displayed."),
										Text.literal(SPACE + "If you're not in an infested Plot, the Guess Waypoint will always point to the center of the Plot, so you'll have to use the ability again.").formatted(Formatting.YELLOW)))
                                .binding(defaults.farming.garden.pestsLocator,
                                        () -> current.farming.garden.pestsLocator,
                                        newValue -> current.farming.garden.pestsLocator = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Highlight Infested Plots"))
                                .description(OptionDescription.of(
                                        Text.literal("Highlight Plots that are infested by pests with a border delimitations.")))
                                .binding(defaults.farming.garden.highlightInfestedPlots,
                                        () -> current.farming.garden.highlightInfestedPlots,
                                        newValue -> current.farming.garden.highlightInfestedPlots = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Foraging").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Foraging-related Settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Galatea - Show Tree Overlay"))
								.description(OptionDescription.of(
										Text.literal("Show Tree progression as Overlay.")))
								.binding(defaults.foraging.showTreeOverlayInfo,
										() -> current.foraging.showTreeOverlayInfo,
										newValue -> current.foraging.showTreeOverlayInfo = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Galatea - Hide Tree Break Animation"))
								.description(OptionDescription.of(
										Text.literal("Hide the entities forming the Tree Break Animation.")))
								.binding(defaults.foraging.hideTreeBreakAnimation,
										() -> current.foraging.hideTreeBreakAnimation,
										newValue -> current.foraging.hideTreeBreakAnimation = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Text.literal("Hunting").formatted(Formatting.BOLD))
						.description(OptionDescription.of(
								Text.literal("Hunting-related Settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Attribute Informations"))
								.description(OptionDescription.of(
										Text.literal("Show some Informations in the Hunting Box or Attribute Menu, related to Shards."),
										Text.literal("Allows you to add the number of missing Shards and display the total price to max it out.")))
								.binding(defaults.hunting.attributeInfos,
										() -> current.hunting.attributeInfos,
										newValue -> current.hunting.attributeInfos = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Prevent the placement of Fishing Net"))
								.description(OptionDescription.of(
										Text.literal("Prevent Fishing Net from being placed")))
								.binding(defaults.hunting.cancelFishingNetPlacement,
										() -> current.hunting.cancelFishingNetPlacement,
										newValue -> current.hunting.cancelFishingNetPlacement = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
                .group(OptionGroup.createBuilder()
                        .name(Text.literal("Fishing").formatted(Formatting.BOLD))
                        .collapsed(false)
                        .description(OptionDescription.of(
                                Text.literal("Fishing settings.")))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Locating Hotspots"))
								.description(OptionDescription.of(
										Text.literal("Locates Hotspots when using the Hotspot Radar.")))
								.binding(defaults.fishing.hotspotRadarGuess,
										() -> current.fishing.hotspotRadarGuess,
										newValue -> current.fishing.hotspotRadarGuess = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Highlighting Hotspots"))
								.description(OptionDescription.of(
										Text.literal("A colored circle appears if your bobber is within the hotspot radius.")))
								.binding(defaults.fishing.hotspotHighlight,
										() -> current.fishing.hotspotHighlight,
										newValue -> current.fishing.hotspotHighlight = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Text.empty()))
                        .build())
                .build();
    }
}
