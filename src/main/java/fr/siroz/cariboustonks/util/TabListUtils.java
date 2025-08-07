package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.HudEvents;
import fr.siroz.cariboustonks.mixin.accessors.PlayerListHudAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TabListUtils {

    private static final String AREA_PREFIX = "Area: ";
    private static final String DUNGEON_PREFIX = "Dungeon: ";

    private static final ObjectArrayList<String> STRING_TAB = new ObjectArrayList<>();

    private TabListUtils() {
    }

    @Contract(" -> new")
    public static @NotNull ObjectArrayList<String> getStringTab() {
        return new ObjectArrayList<>(STRING_TAB);
    }

	public static @Nullable String getFooter() {
		Text footer = ((PlayerListHudAccessor) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).getFooter();
		return footer != null ? footer.getString() : null;
	}

    public static @NotNull IslandType getIsland() {
        IslandType fromArea = IslandType.getById(getArea());;
        return fromArea == IslandType.UNKNOWN ? IslandType.getById(getDungeon()) : fromArea;
    }

    public static @NotNull String getArea() {
        try {
            for (String name : STRING_TAB) {
                if (name.startsWith(AREA_PREFIX)) {
                    return name.substring(AREA_PREFIX.length());
                }
            }
        } catch (Throwable ignored) {
		}

        return "Unknown";
    }

    public static @NotNull String getDungeon() {
        try {
            for (String name : STRING_TAB) {
                if (name.startsWith(DUNGEON_PREFIX)) {
                    return name.substring(DUNGEON_PREFIX.length());
                }
            }
        } catch (Throwable ignored) {
        }

        return "Unknown";
    }

    static void internalUpdate(MinecraftClient client) {
        try {
            STRING_TAB.clear();

            if (client.getNetworkHandler() == null) {
				return;
			}

            ObjectArrayList<String> stringLines = new ObjectArrayList<>();
            for (PlayerListEntry playerListEntry : client.getNetworkHandler().getPlayerList()) {
                if (playerListEntry.getDisplayName() == null) {
					continue;
				}

                String name = playerListEntry.getDisplayName().getString();
                if (name.isEmpty() || name.startsWith("[")) {
					continue;
				}

                //String formatted = StonksUtils.strip(name); // ?
                stringLines.add(name);
            }

            STRING_TAB.addAll(stringLines);
            if (SkyBlockAPI.isOnSkyBlock()) {
				HudEvents.TAB_LIST_UPDATE.invoker().onUpdate(STRING_TAB);
			}
        } catch (Exception ignored) {
        }
    }
}
