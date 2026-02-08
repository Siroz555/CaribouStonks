package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.feature.Feature;

/**
 * Marker interface for all feature components.
 * <p>
 * Components are modular, reusable pieces of functionality that can be attached to
 * {@link Feature} instances to extend their capabilities. Each component encapsulates
 * a specific aspect of feature behavior, such as keybindings, commands or HUD elements.
 * <p>
 * This interface can be interpreted as ECS architecture, without actually being so.
 * Half of the Component adhere to the basic, while others incorporate handlers
 * to transmit business logic, while remaining simple.
 * <p>
 * <ul>
 *     <li>{@link CommandComponent} provides client-side command registration</li>
 *     <li>{@link KeybindComponent} provides Keybinds registration</li>
 *     <li>{@link HudComponent} provides Hud registration</li>
 *     <li>{@link ContainerMatcherComponent} provide a container (Screen) matching Pattern</li>
 *     <li>{@link ContainerOverlayComponent} provide overlays creation in containers (Screen)</li>
 *     <li>{@link TooltipAppenderComponent} provide tooltips appender in containers (Screen)</li>
 *     <li>{@link EntityGlowComponent} provide glowing strategies for entities</li>
 *     <li>{@link ReminderComponent} provide handlers logic for reminders</li>
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
