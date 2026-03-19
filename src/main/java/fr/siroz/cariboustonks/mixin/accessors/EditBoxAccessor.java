package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditBox.class)
public interface EditBoxAccessor {

	@Accessor
	int getHighlightPos();

	@Accessor
	int getMaxLength();

	@Invoker
	void invokeOnValueChange(String value);
}
