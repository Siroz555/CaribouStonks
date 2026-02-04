package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.feature.Feature;

/**
 * Marker interface for all feature components in the component-based architecture.
 * <p>
 * Components are modular, reusable pieces of functionality that can be attached to
 * {@link Feature} instances to extend their capabilities. Each component encapsulates
 * a specific aspect of feature behavior, such as keybindings, commands or HUD elements.
 * <p>
 * This interface follows the Entity-Component-System (ECS) architectural pattern:
 * <ul>
 *   <li>Entity: {@link Feature} instances that contain components</li>
 *   <li>Component: Data and behavior modules (implementations of this interface)</li>
 *   <li>System Managers that process components ({@code ReminderSystem}, {@code HudSystem})</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class ExampleFeature extends Feature {
 *     public ExampleFeature() {
 *         this.addComponent(KeybindComponent.class,
 *             KeybindComponent.builder()
 *                 .add(new KeyBind(...))
 *                 .build()
 *         );
 *
 *         this.addComponent(ReminderComponent.class,
 *             ReminderComponent.builder("EXAMPLE_REMINDER")
 *                 .display(...)
 *                 .onExpire(timedObject -> ...)
 *                 .build()
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see Feature
 * @see Feature#addComponent(Class, Component)
 * @see Feature#getComponent(Class)
 */
public sealed interface Component permits
		CommandComponent,
		ContainerMatcherComponent,
		ContainerOverlayComponent,
		EntityGlowComponent,
		HudComponent,
		KeybindComponent,
		ReminderComponent,
		TooltipAppenderComponent {
}
