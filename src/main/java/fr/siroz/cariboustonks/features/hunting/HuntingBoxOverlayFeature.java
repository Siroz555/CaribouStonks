package fr.siroz.cariboustonks.features.hunting;

import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import java.util.Collections;
import java.util.regex.Pattern;

public class HuntingBoxOverlayFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Hunting Box.*");

	public HuntingBoxOverlayFeature() {
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.trait(MatcherTrait.pattern(TITLE_PATTERN))
				.content(slots -> {

					return Collections.emptyList();
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}
}
