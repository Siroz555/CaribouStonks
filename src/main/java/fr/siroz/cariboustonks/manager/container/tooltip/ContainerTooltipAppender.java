package fr.siroz.cariboustonks.manager.container.tooltip;

import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An interface that allows appending custom {@code tooltip} to items within a container.
 * <p>
 * To use this interface correctly, the implementing class must also
 * implement the {@link ContainerMatcherTrait} to define where and how
 * the appender will be applied. This trait allows specifying a pattern to detect
 * the containers with which the appender will be associated.
 * <p>
 * Without implementing the {@code ContainerMatcherTrait}, the appender will not
 * be properly associated with containers.
 */
public interface ContainerTooltipAppender {

	/**
	 * Appends custom tooltip information to an item within a container.
	 * <h3>Append:</h3>
	 * <pre>{@code
	 * lines.add(Text.literal("A"));
	 * lines.add(Text.empty());
	 * lines.add(Text.literal("Z"));
	 * }</pre>
	 *
	 * @param focusedSlot the currently focused slot in the container, or {@code null}
	 * @param item        the item stack for which the tooltip is being generated
	 * @param lines       the list of text lines representing the current tooltip
	 */
	void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Text> lines);

	/**
	 * Retrieves the priority of this tooltip appender. Higher values indicate lower priority,
	 * meaning the appender will insert its tooltip content closer to the bottom.
	 *
	 * @return an integer representing the priority of this tooltip appender
	 */
	int getPriority();
}
