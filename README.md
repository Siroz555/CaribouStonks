# CaribouStonks

[![modrinth](https://img.shields.io/badge/dynamic/json?color=00AF5C&label=Downloads&prefix=+%20&query=downloads&url=https://api.modrinth.com/v2/project/fraWWQSJ&logo=modrinth)](https://modrinth.com/mod/cariboustonks)

Siroz555's Mod – Fabric mod for Hypixel SkyBlock on the latest versions of Minecraft

The main features are Bazaar/Auctions price displays on items, a graphical representation of prices over time,
in-game Zoom, Scrollable Tooltips, Reminders, and general SkyBlock Qol features.

**Supported versions**

| Version             |                     Status | Notes                                                               |
|---------------------|---------------------------:|---------------------------------------------------------------------|
| **1.21.7 / 1.21.8** | ✅ **Latest (recommended)** | Full support: new features, performance improvements and bug fixes. |
| **1.21.5**          | ⚠️ **Maintained (legacy)** | Still maintained: features from latest and bug fixes                |

### Installation

1. Requirement: [Fabric API](https://modrinth.com/mod/fabric-api)
2. Install directly in the `mods` folder

> [!IMPORTANT]
> This is a `personal` mod, it's not intended to be widely distributed or anything like that at the moment.
> The mod is currently stable and is compatible with most 1.21 mods.
> Updates are planned to correct translation and general issues.

### Features

**General**

- KeyShortcut: Link Keybinds to commands to be executed.

- Stonks
    - Configure the display of item prices in Tooltips.
    - Graphical representation of an item's price trend, quantity, and other information.
    - /stonks {item} Command: Display prices and other information simply anywhere.
    - Search engine for all SkyBlock items.
    - KeyBind to find an item.
    - Bazaar: Press ENTER to validate the last input.

- Reminders
    - Chocolate Factory: Reminders when the number of chocolates reaches the limit.
    - Split or Steal: Reminder when the Ubik's Cube is ready.
    - Forge: Reminder when a forge item is completed.
    - Enchanted Cloak: Reminders when a boost is ready.

**Vanilla**

- Hide the loading screen when changing the world.
- Hide Toast tutorials at the top of the screen.
- Stop the cursor reset position between GUIs.
- Display your own Nametag in F5.
- Overlay:
    - Hide fire on the screen.
    - Hide armor above inventory-hotbar.
    - Hide food above inventory-hotbar.
- Mobs:
    - Hide fire on entities
- Customize Held Item Appearance:
    - Change the size.
    - Change the positions.
    - Change the Swing Duration.
    - _Customization Screen & /cariboustonks heldItemCustomization <mainHand/offHand>_
- Mutes Vanilla Sounds (Lightning, Player Fall, Enderman, Phantom)

**UI & Visuals**

- Highlight selected Pet in the Pet Menu.
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
    - Cocooned Warning: Allows you to be alerted when a mob has been cocooned.
      - _(works with The Primordial Belt & Bloodshot Reforge Stone)_
    - Low health warning: Display a red screen when your health is low.
    - Spirit Mask / Bonzo Mask / Phoenix Pet:
        - Displays a Title when a Mask/Pet is used or ready back.
        - Display a HUD showing the time remaining, sorted and color-coded.

- Farming | Garden
    - KeyBind / Command to lock the camera during farming sessions.
    - Guess Pests and display a cursor line in the direction of the Pests.
    - Highlight Plots that are infested by pests with a border delimitation.

- Foraging
    - Show Tree progression as Overlay.
    - Hide the entities forming the Tree Break Animation.

- Hunting
    - Display Shard prices in Hunting Box, Attribute Menu, and Fusion Machine.
    - Display the number of missing Shards and the price to max it out.

- Fishing
    - Bobber Timer Display: Show the bobber timer in the center of the screen.
    - Rare Sea Creature Warning: Display a Title when you catch a Rare Sea Creature.
    - Fish Caught Warning: Display a Title when you catch a fish.
    - Guess Hotspot: Predict the location of the nearest hotspot.
    - Highlight Hotspot: A colored circle appears if your bobber is within the hotspot radius.

**Slayers**

- Slayer Boss Cocooned Warning: Show a Title and play a Sound when your Slayer Boss is cocooned.
- Spawn Alerts for Boss or Minibosses.
- Highlight Boss or Minibosses with a configurable glow color.
- Statistics Breakdown: Displays a message to show:
    - Spawn Time
    - Kill Time
    - Total Time
- Statistics Average: Displays a message to show:
    - Spawn Avg
    - Kill Avg
    - Boss/h
    - EXP/h
    - RNGMeter/h

- Tarantula Broodfather (Tier V):
    - Highlight Cocoon Eggs summoned by the Boss with a configurable glow color.
    - Show lines from your Cursor to the Cocoon Eggs.

**Events**

- Mythological Ritual
    - Guess Burrow: Waypoint that predicts the location of a Burrow (like 1.8).
    - Burrow Particle Finder: Displays a Waypoint according to the type of Burrow nearby.
    - Line to the closest Burrow.
    - Nearest Warp.
    - Share Inquisitors.
    - Inquisitor Share from other players.
    - Highlight Inquisitors with a configurable glow color.

**Misc**

- Highlight entities (/highlighter)
- Stop the Pickobulus Ability on Private Island
- Party Commands (!warp, !coords, !dice, ..)
- RoughlyEnoughItems (REI) Search Bar Calculator

**Commands**

- /cariboustonks (Main mod Screen)
- /sendCoords > Share your current position.
- /highlighter > Highlight a specific entity in the world.
- /stonks > Display prices and other information simply anywhere.
- /cariboustonks heldItemCustomization <mainHand/offHand>: Customize the appearance of Held Item.
- /cariboustonks resetDiana: Reset Mythological Ritual Waypoints.

---

### Release & Update Policy

* Major features → shipped on the **master** branch first.
* Bug fixes → backported to 1.21.5 when safe.
* Small QoL features may be added to both branches.

---

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
