package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.HudEvents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class ScoreboardUtils {

    private static final ObjectArrayList<String> STRING_SCOREBOARD = new ObjectArrayList<>();

    private ScoreboardUtils() {
    }

    @Contract(" -> new")
    public static @NotNull ObjectArrayList<String> getStringScoreboard() {
        return new ObjectArrayList<>(STRING_SCOREBOARD);
    }

    public static @Nullable String getIslandArea() {
        for (String sidebarLine : STRING_SCOREBOARD) {
            if (sidebarLine.contains("⏣") || sidebarLine.contains("ф")) {
				return sidebarLine.strip();
			}
        }

        return null;
    }

	@ApiStatus.Internal
    static void handleInternalUpdate(MinecraftClient client) {
        try {
            STRING_SCOREBOARD.clear();

			if (client.world == null || client.world.getScoreboard() == null) {
				return;
			}

			Scoreboard scoreboard = client.world.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            ObjectArrayList<String> stringLines = new ObjectArrayList<>();

            for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
                if (scoreboard.getScoreHolderObjectives(scoreHolder).containsKey(objective)) {
                    Team team = scoreboard.getScoreHolderTeam(scoreHolder.getNameForScoreboard());

                    if (team != null) {
                        String strLine = team.getPrefix().getString() + team.getSuffix().getString();

                        if (!strLine.trim().isEmpty()) {
                            String formatted = StonksUtils.stripColor(strLine);
                            stringLines.add(formatted);
                        }
                    }
                }
            }

            if (objective != null) {
                stringLines.add(objective.getDisplayName().getString());
                Collections.reverse(stringLines);
            }

            STRING_SCOREBOARD.addAll(stringLines);
			if (SkyBlockAPI.isOnSkyBlock()) {
				HudEvents.SCOREBOARD_UPDATE.invoker().onUpdate(STRING_SCOREBOARD);
			}
        } catch (Exception ignored) {
        }
    }
}
