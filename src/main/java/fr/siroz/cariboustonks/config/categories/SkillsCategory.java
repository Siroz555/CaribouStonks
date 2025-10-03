package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
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
						.option(LabelOption.create(Text.literal("| Cocooned Mobs").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Cocooned Warning"))
								.description(OptionDescription.of(
										Text.literal("Allows you to be alerted when a mob is Cocooned."),
										Text.literal(SPACE + "The alert is triggered for:").formatted(Formatting.UNDERLINE),
										Text.literal(SPACE + " - Bloodshot on the Primordial Belt").formatted(Formatting.GOLD),
										Text.literal(" - Slayer Minibosses").formatted(Formatting.RED),
										Text.literal(SPACE + "Note: In rare cases, other players' cocoons that are too close to you may be detected.").formatted(Formatting.YELLOW)))
								.binding(defaults.combat.cocoonedMob.cocoonedWarning,
										() -> current.combat.cocoonedMob.cocoonedWarning,
										newValue -> current.combat.cocoonedMob.cocoonedWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Cocooned Warning Title"))
								.description(OptionDescription.of(
										Text.literal("When a mob is Cocooned, show a Title on the screen.")))
								.binding(defaults.combat.cocoonedMob.cocoonedWarningTitle,
										() -> current.combat.cocoonedMob.cocoonedWarningTitle,
										newValue -> current.combat.cocoonedMob.cocoonedWarningTitle = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Cocooned Warning Sound"))
								.description(OptionDescription.of(
										Text.literal("When a mob is Cocooned, play a Sound.")))
								.binding(defaults.combat.cocoonedMob.cocoonedWarningSound,
										() -> current.combat.cocoonedMob.cocoonedWarningSound,
										newValue -> current.combat.cocoonedMob.cocoonedWarningSound = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Cocooned Warning Waypoint Beam"))
								.description(OptionDescription.of(
										Text.literal("When a mob is Cocooned, show a Waypoint Beam.")))
								.binding(defaults.combat.cocoonedMob.cocoonedWarningBeam,
										() -> current.combat.cocoonedMob.cocoonedWarningBeam,
										newValue -> current.combat.cocoonedMob.cocoonedWarningBeam = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Text.literal("| Ragnarock Axe").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Ragnarock Axe - Cast Title"))
								.description(OptionDescription.of(
										Text.literal("Display a Title when the Ragnarock Axe is cast.")))
								.binding(defaults.combat.ragAxe.enabled,
										() -> current.combat.ragAxe.enabled,
										newValue -> current.combat.ragAxe.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.literal("Ragnarock Axe - Cast Message"))
								.description(OptionDescription.of(
										Text.literal("Allows you to customize the Cast message. Supports Minecraft color codes (§c, §b, etc).")))
								.binding(defaults.combat.ragAxe.message,
										() -> current.combat.ragAxe.message,
										newValue -> current.combat.ragAxe.message = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Ragnarock Axe - HUD"))
								.description(OptionDescription.of(
										Text.literal("Displays a HUD when the Ragnarock Axe is cast, showing:"),
										Text.literal(SPACE + "- Time remaining for the Cast effect").formatted(Formatting.YELLOW),
										Text.literal("- Strength gained").formatted(Formatting.RED),
										Text.literal(SPACE + "Note: The HUD will only be displayed if the Ragnarock Axe Cast is enabled.").formatted(Formatting.ITALIC)))
								.binding(defaults.combat.ragAxe.hud.enabled,
										() -> current.combat.ragAxe.hud.enabled,
										newValue -> current.combat.ragAxe.hud.enabled = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Text.literal("| Second Life").formatted(Formatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Spirit/Bonzo/Phoenix Cooldowns HUD"))
								.description(OptionDescription.of(
										Text.literal("Displays a HUD that shows the cooldowns for Second Life abilities. Multiple cooldowns can be displayed, sorted and colored according to the time remaining."),
										Text.literal(SPACE + "- §5Spirit Mask"),
										Text.literal("- §cBonzo Mask"),
										Text.literal("- §ePhoenix Pet"),
										Text.literal(SPACE + "Note: The HUD will only be displayed if at least one of the above options (used) is enabled.").formatted(Formatting.GOLD)))
								.binding(defaults.combat.secondLife.cooldownHud.enabled,
										() -> current.combat.secondLife.cooldownHud.enabled,
										newValue -> current.combat.secondLife.cooldownHud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Spirit Mask - Used"))
								.description(OptionDescription.of(
										Text.literal("Shows a Title when the Spirit Mask is used."),
										Text.literal(SPACE + "If this option is enabled, it can be displayed in the Cooldowns HUD.").formatted(Formatting.ITALIC)))
								.binding(defaults.combat.secondLife.spiritMaskUsed,
										() -> current.combat.secondLife.spiritMaskUsed,
										newValue -> current.combat.secondLife.spiritMaskUsed = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Spirit Mask - Back"))
								.description(OptionDescription.of(
										Text.literal("Shows a Title when the Spirit Mask is ready.")))
								.binding(defaults.combat.secondLife.spiritMaskBack,
										() -> current.combat.secondLife.spiritMaskBack,
										newValue -> current.combat.secondLife.spiritMaskBack = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Bonzo Mask - Used"))
								.description(OptionDescription.of(
										Text.literal("Shows a Title when the Bonzo Mask is used."),
										Text.literal(SPACE + "If this option is enabled, it can be displayed in the Cooldowns HUD.").formatted(Formatting.ITALIC)))
								.binding(defaults.combat.secondLife.bonzoMaskUsed,
										() -> current.combat.secondLife.bonzoMaskUsed,
										newValue -> current.combat.secondLife.bonzoMaskUsed = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Bonzo Mask - Back"))
								.description(OptionDescription.of(
										Text.literal("Shows a Title when the Bonzo Mask is ready.")))
								.binding(defaults.combat.secondLife.bonzoMaskBack,
										() -> current.combat.secondLife.bonzoMaskBack,
										newValue -> current.combat.secondLife.bonzoMaskBack = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Phoenix Pet - Used"))
								.description(OptionDescription.of(
										Text.literal("Shows a Title when the Phoenix Pet is used."),
										Text.literal(SPACE + "If this option is enabled, it can be displayed in the Cooldowns HUD.").formatted(Formatting.ITALIC)))
								.binding(defaults.combat.secondLife.phoenixUsed,
										() -> current.combat.secondLife.phoenixUsed,
										newValue -> current.combat.secondLife.phoenixUsed = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Phoenix Pet - Back"))
								.description(OptionDescription.of(
										Text.literal("Shows a Title when the Phoenix Pet is ready.")))
								.binding(defaults.combat.secondLife.phoenixBack,
										() -> current.combat.secondLife.phoenixBack,
										newValue -> current.combat.secondLife.phoenixBack = newValue)
								.controller(this::createYesNoController)
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
						.build())
                .group(OptionGroup.createBuilder()
                        .name(Text.literal("Fishing").formatted(Formatting.BOLD))
                        .collapsed(false)
                        .description(OptionDescription.of(
                                Text.literal("Fishing settings.")))
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Bobber Timer Display"))
								.description(OptionDescription.of(
										Text.literal("Show the bobber timer in the center of the screen.")))
								.binding(defaults.fishing.bobberTimerDisplay,
										() -> current.fishing.bobberTimerDisplay,
										newValue -> current.fishing.bobberTimerDisplay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Rare Sea Creature Warning"))
								.description(OptionDescription.of(
										Text.literal("Show a Title when you catch a Rare Sea Creature."),
										Text.literal("(Also detected Double Hooks)")))
								.binding(defaults.fishing.rareSeaCreatureWarning,
										() -> current.fishing.rareSeaCreatureWarning,
										newValue -> current.fishing.rareSeaCreatureWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Rare Sea Creature Sound"))
								.description(OptionDescription.of(
										Text.literal("If Rare Sea Creature Warning is enabled, play a Sound when you catch a Rare Sea Creature.")))
								.binding(defaults.fishing.rareSeaCreatureSound,
										() -> current.fishing.rareSeaCreatureSound,
										newValue -> current.fishing.rareSeaCreatureSound = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.literal("Fish Caught Warning"))
								.description(OptionDescription.of(
										Text.literal("Show a Title when you catch a fish.")))
								.binding(defaults.fishing.fishCaughtWarning,
										() -> current.fishing.fishCaughtWarning,
										newValue -> current.fishing.fishCaughtWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Text.literal("| Hotspots").formatted(Formatting.BOLD)))
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
						.option(LabelOption.create(Text.empty()))
                        .build())
                .build();
    }
}
