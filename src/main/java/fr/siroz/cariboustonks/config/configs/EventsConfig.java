package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.feature.diana.GuessBurrowLogic;
import fr.siroz.cariboustonks.feature.diana.TargetBurrowLogic;

import java.awt.Color;

public class EventsConfig {

    @SerialEntry
    public MythologicalRitual mythologicalRitual = new MythologicalRitual();

    public static class MythologicalRitual {

		@SerialEntry
		public boolean enabled = false;

        @SerialEntry
        public boolean guessBurrow = false;

        @SerialEntry
        public Color guessBurrowColor = Color.YELLOW;

        @SerialEntry
        public GuessBurrowLogic guessBurrowLogic = GuessBurrowLogic.NORMAL;

        @SerialEntry
        public TargetBurrowLogic targetBurrowLogic = TargetBurrowLogic.NORMAL;

        @SerialEntry
        public boolean burrowParticleFinder = false;

        @SerialEntry
        public Color burrowParticleFinderStartColor = Color.GREEN;

        @SerialEntry
        public Color burrowParticleFinderMobColor = Color.RED;

        @SerialEntry
        public Color burrowParticleFinderTreasureColor = Color.ORANGE;

        @SerialEntry
        public boolean lineToClosestBurrow = false;

        @SerialEntry
        public boolean nearestWarp = false;

        @SerialEntry
        public boolean shareInquisitor = false;

		@SerialEntry
		public boolean highlightInquisitor = false;

		@SerialEntry
		public Color highlightInquisitorColor = Color.RED;
    }
}
