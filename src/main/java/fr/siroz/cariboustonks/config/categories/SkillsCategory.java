package fr.siroz.cariboustonks.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import fr.siroz.cariboustonks.config.Config;
import fr.siroz.cariboustonks.util.Client;
import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

@SuppressWarnings("checkstyle:linelength")
public class SkillsCategory extends AbstractCategory {

    public SkillsCategory(Config defaults, Config current) {
        super(defaults, current);
    }

    @Override
    public ConfigCategory create() {
        return ConfigCategory.createBuilder()
                .name(Component.literal("Skills-related"))
                .tooltip(Component.literal("Skills-related Settings"))
                .group(OptionGroup.createBuilder()
                        .name(Component.literal("Combat").withStyle(ChatFormatting.BOLD))
                        .description(OptionDescription.of(
                                Component.literal("Combat-related Settings")))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Low Health Warning"))
                                .description(OptionDescription.of(
                                        Component.literal("Displays red around the screen when life points are low.")))
                                .binding(defaults.combat.lowHealthWarning.lowHealthWarningEnabled,
                                        () -> current.combat.lowHealthWarning.lowHealthWarningEnabled,
                                        newValue -> current.combat.lowHealthWarning.lowHealthWarningEnabled = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Low Health Warning - Threshold"))
                                .description(OptionDescription.of(
                                        Component.literal("If Low Health Warning is enabled, allows you to modify in % when the warning will be triggered.")))
                                .binding(defaults.combat.lowHealthWarning.lowHealthWarningThreshold,
                                        () -> current.combat.lowHealthWarning.lowHealthWarningThreshold,
                                        newValue -> current.combat.lowHealthWarning.lowHealthWarningThreshold = newValue)
                                .controller(opt -> createIntegerPercentController(opt, 50))
                                .build())
						.option(Option.<Double>createBuilder()
								.name(Component.literal("Low Health Warning - Red Intensity"))
								.description(OptionDescription.of(
										Component.literal("If Low Health Warning is enabled, allows you to modify the Red Intensity displayed.")))
								.binding(defaults.combat.lowHealthWarning.lowHealthWarningIntensity,
										() -> current.combat.lowHealthWarning.lowHealthWarningIntensity,
										newValue -> current.combat.lowHealthWarning.lowHealthWarningIntensity = newValue)
								.controller(opt -> DoubleSliderControllerBuilder.create(opt)
										.range(0.05d, 0.8d)
										.step(0.1d)
										.formatValue(d -> Component.nullToEmpty("x " + String.format("%.1f", d))))
								.build())
						.option(LabelOption.create(Component.literal("| Cocooned Mobs").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Cocooned Warning"))
								.description(OptionDescription.of(
										Component.literal("Allows you to be alerted when a mob is Cocooned."),
										Component.literal(SPACE + "The alert is triggered for:").withStyle(ChatFormatting.UNDERLINE),
										Component.literal(SPACE + " - Bloodshot on the Primordial Belt").withStyle(ChatFormatting.GOLD),
										Component.literal(" - Slayer Minibosses").withStyle(ChatFormatting.RED),
										Component.literal(SPACE + "Note: In rare cases, other players' cocoons that are too close to you may be detected.").withStyle(ChatFormatting.YELLOW)))
								.binding(defaults.combat.cocoonedMob.cocoonedWarning,
										() -> current.combat.cocoonedMob.cocoonedWarning,
										newValue -> current.combat.cocoonedMob.cocoonedWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Cocooned Warning - Title"))
								.description(OptionDescription.of(
										Component.literal("When a mob is Cocooned, show a Title on the screen.")))
								.binding(defaults.combat.cocoonedMob.cocoonedWarningTitle,
										() -> current.combat.cocoonedMob.cocoonedWarningTitle,
										newValue -> current.combat.cocoonedMob.cocoonedWarningTitle = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Cocooned Warning - Sound"))
								.description(OptionDescription.of(
										Component.literal("When a mob is Cocooned, play a Sound.")))
								.binding(defaults.combat.cocoonedMob.cocoonedWarningSound,
										() -> current.combat.cocoonedMob.cocoonedWarningSound,
										newValue -> current.combat.cocoonedMob.cocoonedWarningSound = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Cocooned Warning - Waypoint Beam"))
								.description(OptionDescription.of(
										Component.literal("When a mob is Cocooned, show a Waypoint Beam.")))
								.binding(defaults.combat.cocoonedMob.cocoonedWarningBeam,
										() -> current.combat.cocoonedMob.cocoonedWarningBeam,
										newValue -> current.combat.cocoonedMob.cocoonedWarningBeam = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Component.literal("Cocooned Warning - Message"))
								.description(OptionDescription.of(
										Component.literal("Change the message when a mob is Cocooned."),
										Component.literal(SPACE + "Support Minecraft color codes (§)").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.combat.cocoonedMob.message,
										() -> current.combat.cocoonedMob.message,
										newValue -> current.combat.cocoonedMob.message = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(LabelOption.create(Component.literal("| Wither Shield").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither Shield - Cooldown HUD"))
								.description(OptionDescription.of(
										Component.literal("Displays a HUD that shows the cooldowns of the Wither Shield Ability."),
										Component.literal(SPACE + "The HUD is displayed only when the ability is activated for 5 seconds. Once the cooldown is reached, the HUD displays READY for 2 seconds before disappearing.")))
								.binding(defaults.combat.witherShield.hud.enabled,
										() -> current.combat.witherShield.hud.enabled,
										newValue -> current.combat.witherShield.hud.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Wither Shield - Only Show Timer"))
								.description(OptionDescription.of(
										Component.literal("If enabled, only displays the timer on the screen, not “Wither Shield: 3.4s.”")))
								.binding(defaults.combat.witherShield.onlyShowTimer,
										() -> current.combat.witherShield.onlyShowTimer,
										newValue -> current.combat.witherShield.onlyShowTimer = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Component.literal("Wither Shield - Ready Message"))
								.description(OptionDescription.of(
										Component.literal("Change the “READY” message."),
										Component.literal(SPACE + "Support Minecraft color codes (§)").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.combat.witherShield.readyMessage,
										() -> current.combat.witherShield.readyMessage,
										newValue -> current.combat.witherShield.readyMessage = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.literal("Wither Shield - Timer Color"))
								.description(OptionDescription.of(
										Component.literal("Change the color of the timer.")))
								.binding(defaults.combat.witherShield.timerColor,
										() -> current.combat.witherShield.timerColor,
										newValue -> current.combat.witherShield.timerColor = newValue)
								.controller(ColorControllerBuilder::create)
								.build())
						.option(LabelOption.create(Component.literal("| Ragnarock Axe").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Ragnarock Axe - Cast Title"))
								.description(OptionDescription.of(
										Component.literal("Display a Title when the Ragnarock Axe is cast.")))
								.binding(defaults.combat.ragAxe.enabled,
										() -> current.combat.ragAxe.enabled,
										newValue -> current.combat.ragAxe.enabled = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Component.literal("Ragnarock Axe - Cast Message"))
								.description(OptionDescription.of(
										Component.literal("Allows you to customize the Cast message. Supports Minecraft color codes (§c, §b, etc).")))
								.binding(defaults.combat.ragAxe.message,
										() -> current.combat.ragAxe.message,
										newValue -> current.combat.ragAxe.message = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Ragnarock Axe - HUD"))
								.description(OptionDescription.of(
										Component.literal("Displays a HUD when the Ragnarock Axe is cast, showing:"),
										Component.literal(SPACE + "- Time remaining for the Cast effect").withStyle(ChatFormatting.YELLOW),
										Component.literal("- Strength gained").withStyle(ChatFormatting.RED),
										Component.literal(SPACE + "Note: The HUD will only be displayed if the Ragnarock Axe Cast is enabled.").withStyle(ChatFormatting.ITALIC)))
								.binding(defaults.combat.ragAxe.hud.enabled,
										() -> current.combat.ragAxe.hud.enabled,
										newValue -> current.combat.ragAxe.hud.enabled = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(LabelOption.create(Component.literal("| Second Life").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Spirit Mask - Used"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title when the Spirit Mask is used.")))
								.binding(defaults.combat.secondLife.spiritMaskUsed,
										() -> current.combat.secondLife.spiritMaskUsed,
										newValue -> current.combat.secondLife.spiritMaskUsed = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Spirit Mask - Back"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title/Message when the Spirit Mask is ready."),
										Component.literal(SPACE + "See “Show Back Title” or “Show Back Message” settings.")))
								.binding(defaults.combat.secondLife.spiritMaskBack,
										() -> current.combat.secondLife.spiritMaskBack,
										newValue -> current.combat.secondLife.spiritMaskBack = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Bonzo Mask - Used"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title when the Bonzo Mask is used.")))
								.binding(defaults.combat.secondLife.bonzoMaskUsed,
										() -> current.combat.secondLife.bonzoMaskUsed,
										newValue -> current.combat.secondLife.bonzoMaskUsed = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Bonzo Mask - Back"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title/Message when the Bonzo Mask is ready."),
										Component.literal(SPACE + "See “Show Back Title” or “Show Back Message” settings.")))
								.binding(defaults.combat.secondLife.bonzoMaskBack,
										() -> current.combat.secondLife.bonzoMaskBack,
										newValue -> current.combat.secondLife.bonzoMaskBack = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Phoenix Pet - Used"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title when the Phoenix Pet is used.")))
								.binding(defaults.combat.secondLife.phoenixUsed,
										() -> current.combat.secondLife.phoenixUsed,
										newValue -> current.combat.secondLife.phoenixUsed = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Phoenix Pet - Back"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title/Message when the Phoenix Pet is ready."),
										Component.literal(SPACE + "See “Show Back Title” or “Show Back Message” settings.")))
								.binding(defaults.combat.secondLife.phoenixBack,
										() -> current.combat.secondLife.phoenixBack,
										newValue -> current.combat.secondLife.phoenixBack = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Cooldowns HUD"))
								.description(OptionDescription.of(
										Component.literal("Displays a HUD that shows the cooldowns for Second Life abilities. Multiple cooldowns can be displayed, sorted and colored according to the time remaining."),
										Component.literal(SPACE + "- §5Spirit Mask"),
										Component.literal("- §cBonzo Mask"),
										Component.literal("- §ePhoenix Pet"),
										Component.literal(SPACE + "Note: The HUD can function without having to activate the options above.").withStyle(ChatFormatting.GREEN)))
								.binding(defaults.combat.secondLife.cooldownHud.enabled,
										() -> current.combat.secondLife.cooldownHud.enabled,
										newValue -> current.combat.secondLife.cooldownHud.enabled = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Back - Show Title"))
								.description(OptionDescription.of(
										Component.literal("Shows a Title when any second life is ready.")))
								.binding(defaults.combat.secondLife.backTitle,
										() -> current.combat.secondLife.backTitle,
										newValue -> current.combat.secondLife.backTitle = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Back - Show Message"))
								.description(OptionDescription.of(
										Component.literal("Shows a Message when any second life is ready.")))
								.binding(defaults.combat.secondLife.backMessage,
										() -> current.combat.secondLife.backMessage,
										newValue -> current.combat.secondLife.backMessage = newValue)
								.controller(this::createYesNoController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Back - Play Sound"))
								.description(OptionDescription.of(
										Component.literal("Plays a Sound when any second life is ready.")))
								.binding(defaults.combat.secondLife.backSound,
										() -> current.combat.secondLife.backSound,
										newValue -> current.combat.secondLife.backSound = newValue)
								.controller(this::createYesNoController)
								.build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.literal("Garden").withStyle(ChatFormatting.BOLD))
                        .description(OptionDescription.of(
                                Component.literal("Garden settings")))
                        .collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Greenhouse Growth Stage Reminder"))
								.description(OptionDescription.of(
										Component.literal("Allows you to activate a reminder when your Greenhouse reaches the Next Growth Stage."),
										Component.literal(SPACE + "You must use your Crop Diagnostics at least once to begin detection.").withStyle(ChatFormatting.GOLD)))
								.binding(defaults.farming.garden.greenhouseGrowthStageReminder,
										() -> current.farming.garden.greenhouseGrowthStageReminder,
										newValue -> current.farming.garden.greenhouseGrowthStageReminder = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Disable Greenhouse watering can placement"))
								.description(OptionDescription.of(
										Component.literal("If enabled, disables the placement of watering cans in the Greenhouse."),
										Component.literal(SPACE + "- HydroCan X").withStyle(ChatFormatting.AQUA),
										Component.literal("- AquaMaster X").withStyle(ChatFormatting.RED)))
								.binding(defaults.farming.garden.disableWateringCanPlacement,
										() -> current.farming.garden.disableWateringCanPlacement,
										newValue -> current.farming.garden.disableWateringCanPlacement = newValue)
								.controller(this::createBooleanController)
								.build())
                        .option(ButtonOption.createBuilder()
                                .name(Component.literal("Locking the camera during farming"))
								.text(Component.literal("/lockMouse"))
                                .description(OptionDescription.of(
                                        Component.literal("Allows you to block the movements of the mouse during farming."),
                                        Component.literal(SPACE + "Use /lockMouse or go to KeyBinds Options.")))
                                .action((yaclScreen, buttonOption) -> {
                                    Client.sendMessageWithPrefix(Component.literal("Use /lockMouse or go to KeyBinds Options."));
                                })
                                .build())
                        .option(this::shortcutToKeybindsOptions)
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Locating Pests"))
                                .description(OptionDescription.of(
                                        Component.literal("Locate Pests with your Vacuum, creating a Guess Waypoint. A line from your cursor to the nearest Pest will also be displayed."),
										Component.literal(SPACE + "If you're not in an infested Plot, the Guess Waypoint will always point to the center of the Plot, so you'll have to use the ability again.").withStyle(ChatFormatting.YELLOW)))
                                .binding(defaults.farming.garden.pestsLocator,
                                        () -> current.farming.garden.pestsLocator,
                                        newValue -> current.farming.garden.pestsLocator = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Highlight Infested Plots"))
                                .description(OptionDescription.of(
                                        Component.literal("Highlight Plots that are infested by pests with a border delimitations.")))
                                .binding(defaults.farming.garden.highlightInfestedPlots,
                                        () -> current.farming.garden.highlightInfestedPlots,
                                        newValue -> current.farming.garden.highlightInfestedPlots = newValue)
                                .controller(this::createBooleanController)
                                .build())
                        .build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Foraging").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Foraging-related Settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Galatea - Show Tree Overlay"))
								.description(OptionDescription.of(
										Component.literal("Show Tree progression as Overlay.")))
								.binding(defaults.foraging.showTreeOverlayInfo,
										() -> current.foraging.showTreeOverlayInfo,
										newValue -> current.foraging.showTreeOverlayInfo = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Galatea - Hide Tree Break Animation"))
								.description(OptionDescription.of(
										Component.literal("Hide the entities forming the Tree Break Animation.")))
								.binding(defaults.foraging.hideTreeBreakAnimation,
										() -> current.foraging.hideTreeBreakAnimation,
										newValue -> current.foraging.hideTreeBreakAnimation = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Hunting").withStyle(ChatFormatting.BOLD))
						.description(OptionDescription.of(
								Component.literal("Hunting-related Settings")))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Attribute Informations"))
								.description(OptionDescription.of(
										Component.literal("Show some Informations in the Hunting Box or Attribute Menu, related to Shards."),
										Component.literal("Allows you to add the number of missing Shards and display the total price to max it out.")))
								.binding(defaults.hunting.attributeInfos,
										() -> current.hunting.attributeInfos,
										newValue -> current.hunting.attributeInfos = newValue)
								.controller(this::createBooleanController)
								.build())
						.build())
                .group(OptionGroup.createBuilder()
                        .name(Component.literal("Fishing").withStyle(ChatFormatting.BOLD))
                        .collapsed(false)
                        .description(OptionDescription.of(
                                Component.literal("Fishing settings.")))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Bobber Timer Display"))
								.description(OptionDescription.of(
										Component.literal("Show the bobber timer in the center of the screen.")))
								.binding(defaults.fishing.bobberTimerDisplay,
										() -> current.fishing.bobberTimerDisplay,
										newValue -> current.fishing.bobberTimerDisplay = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Rare Sea Creature Warning"))
								.description(OptionDescription.of(
										Component.literal("Show a Title when you catch a Rare Sea Creature."),
										Component.literal("(Also detected Double Hooks)")))
								.binding(defaults.fishing.rareSeaCreatureWarning,
										() -> current.fishing.rareSeaCreatureWarning,
										newValue -> current.fishing.rareSeaCreatureWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Rare Sea Creature Sound"))
								.description(OptionDescription.of(
										Component.literal("If Rare Sea Creature Warning is enabled, play a Sound when you catch a Rare Sea Creature.")))
								.binding(defaults.fishing.rareSeaCreatureSound,
										() -> current.fishing.rareSeaCreatureSound,
										newValue -> current.fishing.rareSeaCreatureSound = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Fish Caught Warning"))
								.description(OptionDescription.of(
										Component.literal("Show a Title when you catch a fish.")))
								.binding(defaults.fishing.fishCaughtWarning,
										() -> current.fishing.fishCaughtWarning,
										newValue -> current.fishing.fishCaughtWarning = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Component.literal("| Hotspots").withStyle(ChatFormatting.BOLD)))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Locating Hotspots"))
								.description(OptionDescription.of(
										Component.literal("Locates Hotspots when using the Hotspot Radar.")))
								.binding(defaults.fishing.hotspotRadarGuess,
										() -> current.fishing.hotspotRadarGuess,
										newValue -> current.fishing.hotspotRadarGuess = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Highlighting Hotspots"))
								.description(OptionDescription.of(
										Component.literal("A colored circle appears if your bobber is within the hotspot radius.")))
								.binding(defaults.fishing.hotspotHighlight,
										() -> current.fishing.hotspotHighlight,
										newValue -> current.fishing.hotspotHighlight = newValue)
								.controller(this::createBooleanController)
								.build())
						.option(LabelOption.create(Component.empty()))
						.option(LabelOption.create(Component.empty()))
                        .build())
                .build();
    }
}
