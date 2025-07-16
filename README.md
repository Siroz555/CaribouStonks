# CaribouStonks

Siroz555's Mod – Fabric mod for Hypixel SkyBlock on the latest Minecraft version.

Minecraft Version: **1.21.5**

The main features are Bazaar/Auctions price displays on items, a graphical representation of prices over time,
in-game Zoom, Scrollable Tooltips, Reminders and general SkyBlock Qol features.

### Installation

1. Requirement: [Fabric API](https://modrinth.com/mod/fabric-api)
2. Install directly in the `mods` folder

> [!IMPORTANT]
> This is a `personal` mod, it's not intended to be widely distributed or anything like that at the moment.
> The mod is currently stable and is compatible with most 1.21 mods.
> Updates are planned to correct translation and general issues.

### Features

**General**

- Stonks
    - Configure display of item prices in Tooltips.
    - Graphical representation of an item's price trend, quantity, and other information. (BETA)
    - Search engine for all SkyBlock items.
    - KeyBind to find an item.
    - Bazaar: Press ENTER to validate the last input.

- Reminders
    - Chocolate Factory: Reminders when the number of chocolates reaches the limit.
    - Split or Steal: Reminder when the Ubik's Cube is ready.
    - Forge: Reminder when a forge item is completed.
    - Enchanted Cloak: Reminders when a boost is ready.

- KeyShortcut: Link Keybinds to commands to be executed.

**Vanilla**

- Hide the loading screen when changing the world.
- Hide Toast tutorials at the top of the screen.
- Stop the cursor reset position between GUIs.
- Overlay:
    - Hide fire on the screen.
    - Hide armor above inventory-hotbar.
    - Hide food above inventory-hotbar.
- Customize display of items on hand:
    - Change the size.
    - Change the positions.
    - Change the Swing Duration.

**UI & Visuals**

- Highlight selected Pet
- Create & highlight favorite contacts in the Abiphone Menu
- Zoom KeyBind (default to C).
- Scrollable tooltips.
- Create and customize Waypoints.
- Display a Waypoint in the world when a player shares its position.
- Color and change the colors of enchantments on Items, according to their levels
- Colored Enchantments
    - Change the color of enchantments according to their levels.
    - Single color or with animated rainbow effect.
- Display a colored border around Tooltip items according to rarity.
- Overlay
    - Etherwarp Teleport Target display.
    - Gyrokinetic Wand radius display.
- HUDs
    - Fps HUD
    - Ping HUD
    - Tps HUD
    - Day HUD

**Chat**

- Copy message (CTRL + LEFT CLICK).
- Change history size.
- Change the color of Party messages.
- Change the color of Guild messages.
- Send your current position (/sendCoords).

**Skills**

- Combat
    - Low health warning: Display a red screen when your health is low.

- Farming | Garden
    - KeyBind / Command to lock the camera during farming sessions.
    - Guess Pests and display a cursor line in the direction of the Pests.
    - Highlight Plots that are infested by pests with a border delimitation.

- Foraging
    - Show Tree progression as Overlay.
    - Hide the entities forming the Tree Break Animation.

- Hunting
    - Display Shard prices in Hunting Box, Attribute Menu and Fusion Machine.

- Fishing
    - Guess Hotspot: Predict the location of the nearest hotspot
    - Highlight Hotspot: A colored circle appears if your bobber is within the hotspot radius.

**Events**

- Mythological Ritual (BETA)
    - Guess Burrow: Waypoint that predicts the location of a Burrow (like 1.8).
    - Burrow Particle Finder: Display a Waypoint according to the type of Burrow nearby.
    - Line to the closest Burrow.
    - Nearest Warp. (BETA)

**Misc**

- Highlight entities (/highlighter)
- Stop the Pickobulus Ability on Private Island
- Party Commands (!warp, !coords, !dice, ..)
- RoughlyEnoughItems (REI) Search Bar Calculator

### Credits & Open Source Software

- **Fabric** & **Yarn** for the development environment.
- **YACL** (for the system and configuration GUI) as a library.
- **HypixelModAPI** (by AzureAaron) as a library.
- **Skyblocker** (<3) & **Wynntils** (<3) for some code logic in the latest version of Minecraft, such as Mixins.
- **SkyHanni** & **NotEnoughUpdates** for ideas and adaptations since version 1.8.

| Software                                                                 | License                                                                                           |
|--------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| [Fabric](https://github.com/FabricMC/fabric)                             | [Apache License 2.0](https://github.com/FabricMC/fabric/blob/1.21.1/LICENSE)                      |
| [Yarn](https://github.com/FabricMC/yarn)                                 | [Creative Commons CC0](https://github.com/FabricMC/yarn/blob/24w40a/LICENSE)                      |
| [YACL](https://github.com/isXander/YetAnotherConfigLib)                  | [LGPL-3.0 license](https://github.com/isXander/YetAnotherConfigLib/blob/multiversion/dev/LICENSE) |
| [Skyblocker](https://github.com/SkyblockerMod/Skyblocker)                | [LGPL-3.0](https://github.com/SkyblockerMod/Skyblocker/blob/master/LICENSE)                       |
| [Wynntils](https://github.com/Wynntils/Wynntils)                         | [AGPL 3.0](https://github.com/Wynntils/Wynntils/blob/development/LICENSE)                         |
| [HypixelModAPI](https://github.com/azureaaron/hm-api)                    | [Apache-2.0 license ](https://github.com/AzureAaron/hm-api/blob/master/LICENSE)                   |
| [SkyHanni](https://github.com/hannibal002/SkyHanni)                      | [LGPL-2.1 license](https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE)                     |
| [SoopyV2](https://github.com/Soopyboo32/SoopyV2)                         | [GPL-3.0 license](https://github.com/Soopyboo32/SoopyV2/blob/master/LICENSE)                      |
| [Skytils](https://github.com/Skytils/SkytilsMod)                         | [AGPL-3.0 license ](https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md)                    |
| [NotEnoughUpdates](https://github.com/NotEnoughUpdates/NotEnoughUpdates) | [LGPL-3.0 license ](https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/master/COPYING)     |
