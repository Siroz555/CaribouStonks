package fr.siroz.cariboustonks.feature.bazaar;

import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

@Deprecated
public class BazaarOrdersInfoFeature extends Feature implements ContainerMatcherTrait, ContainerOverlay {

	private static final Pattern TITLE_PATTERN = Pattern.compile("(?:Co-op|Your) Bazaar Orders");

	public BazaarOrdersInfoFeature() {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
	public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
		return List.of();
	}
}
