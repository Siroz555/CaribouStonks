package fr.siroz.cariboustonks.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public final class Symbols {

    private Symbols() {
    }

    public static final String ARROW_UP = "📈";
    public static final String ARROW_DOWN = "📉";
    public static final String MONEY_BAG = "💰";
    public static final String COIN = "🪙";
    public static final String DOLLAR = "$";
    public static final String DIAMOND = "💎";
    public static final String FIRE = "🔥";
    public static final String WARNING = "⚠️"; // -_-
    public static final String CHECK = "✅";
    public static final String CROSS = "❌";
    public static final String CLOCK = "⏰";

    public static final String PICKAXE = "\uD83E\uDE93"; // 🔓🪓
    public static final String CREEPER = "\uD83E\uDD96"; // 🐍
    public static final String SWORD = "⚔️"; // -_-
    public static final String SHIELD = "🛡️"; // -_-
    public static final String STAR = "⭐";
    public static final String BLOCK = "⬛";

    public static final String STONKS = "STONKS";
    public static final String ROCKET = "🚀";
    public static final String BANK = "🏦";
    public static final String CHART = "💹";
    public static final String GIFT = "🎁";
    public static final String HANDSHAKE = "🤝";
    public static final String TROPHY = "🏆";

    public static final Map<String, String> SYMBOLS = Map.ofEntries(
            Map.entry("up", ARROW_UP),
            Map.entry("down", ARROW_DOWN),
            Map.entry("money_bag", MONEY_BAG),
            Map.entry("coin", COIN),
            Map.entry("dollar", DOLLAR),
            Map.entry("diamond", DIAMOND),
            Map.entry("fire", FIRE),
            Map.entry("warning", WARNING),
            Map.entry("check", CHECK),
            Map.entry("cross", CROSS),
            Map.entry("clock", CLOCK),
            Map.entry("pickaxe", PICKAXE),
            Map.entry("creeper", CREEPER),
            Map.entry("sword", SWORD),
            Map.entry("shield", SHIELD),
            Map.entry("star", STAR),
            Map.entry("block", BLOCK),
            Map.entry("stonks", STONKS),
            Map.entry("rocket", ROCKET),
            Map.entry("bank", BANK),
            Map.entry("chart", CHART),
            Map.entry("gift", GIFT),
            Map.entry("handshake", HANDSHAKE),
            Map.entry("trophy", TROPHY)
    );

    public static Text getStyled(String symbol, int color, Formatting... formatting) {
        return Text.literal(symbol).withColor(color).formatted(formatting);
    }
}
