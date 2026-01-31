package fr.siroz.cariboustonks.feature.ui;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.system.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SelectedPetHighlightFeature extends Feature implements ContainerMatcherTrait, ContainerOverlay {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Pets.*");
    private static final Pattern SELECTED_PATTERN = Pattern.compile("Click to despawn!");

    @Override
    public boolean isEnabled() {
        return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.highlightSelectedPet;
    }

	@Override
	public @Nullable Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
    public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {

            ItemStack itemStack = entry.getValue();
            if (itemStack == null || !itemStack.is(Items.PLAYER_HEAD)) {
				continue;
			}

            List<Component> lore = ItemUtils.getLore(itemStack);
            if (lore.isEmpty()) {
				continue;
			}

            String concatenateLore = ItemUtils.concatenateLore(lore);
            if (SELECTED_PATTERN.matcher(concatenateLore).find()) {
                highlights.add(ColorHighlight.green(entry.getIntKey(), 0.25f));
            }
        }

        return highlights;
    }
}
