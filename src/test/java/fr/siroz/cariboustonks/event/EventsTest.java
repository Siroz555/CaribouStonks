package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ===== FALSE TEST (Personal Note) =====
 * <p>
 * Ces tests sont "fake", de base, je ne comprenais pas la logique des
 * event de Fabric API.
 * <p>
 * Il n'y a pas "besoin" d'avoir un principe de short-circuit, pour moi ça
 * paraissait logique, mais les Events sont traités en interne de cette manière,
 * c'est-à-dire qu'on peut directement return le listener dans la boucle for,
 * et avoir un return false en sortie.
 * <pre>{@code
 * Event<Event> createEvent() {
 *     return EventFactory.createArrayBacked(Event.class, listeners -> (a, b) -> {
 *         for (Event listener : listeners) {
 *             return listener.process(a, b);
 *         }
 *         return false;
 *     });
 * }
 * }</pre>
 * De ce fait, le premier listener qui return un true, sera retourné,
 * les listener false seront ignorés, mais quand même parcouru par l'API Fabric.
 * <p>
 * Cette class test est uniquement là pour moi, pour me souvenir de cette
 * logique.
 * Cependant, le principe de Logical And peu être appliqué. Je n'ai pas trouvé
 * d'informations à ce sujet à cette heure -_-
 * <p>
 * Test Impl Note :
 * <p>
 * Problème lié au fait que les tests s'exécutent séquentiellement et que
 * les listeners s'accumulent entre les tests.
 * Fabric API ne permet pas de réinitialiser les événements entre les tests
 * (de ce que j'ai vu).
 * J'utilise donc wrapper d'événement qui se créait à chaque test.
 */
class EventsTest {

	// ===== DIRECT =====

	@Test
	@DisplayName("Direct event should return first listener result when it's true")
	void testDirectEvent_returnsFirstListenerResult_whenTrue() {
		Event<DirectEvent> event = createDirectEvent();

		event.register((a, b) -> true);

		event.register((a, b) -> {
			fail("This listener should not be called");
			return false;
		});

		boolean result = event.invoker().process(5, 10);
		assertTrue(result, "The event should directly return the result of the first listener (true)");
	}

	@Test
	@DisplayName("Direct event should return first listener result when it's false")
	void testDirectEvent_returnsFirstListenerResult_whenFalse() {
		Event<DirectEvent> event = createDirectEvent();

		event.register((a, b) -> false);

		event.register((a, b) -> {
			fail("This listener should not be called");
			return true;
		});

		boolean result = event.invoker().process(5, 10);
		assertFalse(result, "The event should directly return the result of the first listener (false)");
	}

	@Test
	@DisplayName("Direct event should return false when there are no listeners")
	void testDirectEvent_returnsFalse_whenNoListeners() {
		Event<DirectEvent> event = createDirectEvent();

		boolean result = event.invoker().process(5, 10);
		assertFalse(result, "The event should return false when there are no listeners");
	}

	@Test
	@DisplayName("Direct event should correctly pass parameters to listeners")
	void testDirectEvent_parametersAreCorrectlyPassed() {
		Event<DirectEvent> event = createDirectEvent();
		final int[] capturedA = {0};
		final int[] capturedB = {0};

		event.register((a, b) -> {
			capturedA[0] = a;
			capturedB[0] = b;
			return true;
		});

		int testA = 42;
		int testB = 123;
		event.invoker().process(testA, testB);

		assertEquals(testA, capturedA[0], "The ‘a' parameter should be correctly passed to the listener");
		assertEquals(testB, capturedB[0], "The ‘b' parameter should be correctly passed to the listener");
	}

	@Test
	@DisplayName("Direct event should stop processing listeners after the first true result")
	void testDirectEvent_onlyFirstListenerIsExecuted() {
		Event<DirectEvent> event = createDirectEvent();
		final boolean[] firstListenerCalled = {false};
		final boolean[] secondListenerCalled = {false};

		event.register((a, b) -> {
			firstListenerCalled[0] = true;
			return true;
		});

		event.register((a, b) -> {
			secondListenerCalled[0] = true;
			return false;
		});

		event.invoker().process(1, 2);

		assertTrue(firstListenerCalled[0], "The first listener should be called");
		assertFalse(secondListenerCalled[0], "The second listener should be called");
	}

	// ===== SHORT CIRCUIT =====

	@Test
	@DisplayName("Short circuit event should return true when at least one listener returns true")
	void testShortCircuitEvent_returnsTrue_whenAnyListenerReturnsTrue() {
		Event<ShortCircuitEvent> testEvent = createShortCircuitEvent();

		for (int i = 0; i < 9; i++) {
			testEvent.register((horizontal, vertical) -> false);
		}

		testEvent.register((horizontal, vertical) -> true);

		boolean result = testEvent.invoker().process(1, 2);
		assertTrue(result, "The event should return true when at least one listener returns true");
	}

	@Test
	@DisplayName("Short circuit event should return false when all listeners return false")
	void testShortCircuitEvent_returnsFalse_whenAllListenersReturnFalse() {
		Event<ShortCircuitEvent> testEvent = createShortCircuitEvent();

		for (int i = 0; i < 10; i++) {
			testEvent.register((a, b) -> false);
		}

		boolean result = testEvent.invoker().process(1, 2);
		assertFalse(result, "The event should return false when all listeners return false");
	}

	@Test
	@DisplayName("Short circuit event should stop processing listeners after first true result")
	void testShortCircuitEvent_stopsProcessingListeners_afterFirstTrueResult() {
		Event<ShortCircuitEvent> testEvent = createShortCircuitEvent();

		final boolean[] listenersInvoked = new boolean[10];

		for (int i = 0; i < 9; i++) {
			final int index = i;
			testEvent.register((a, b) -> {
				listenersInvoked[index] = true;
				return false;
			});
		}

		testEvent.register((a, b) -> {
			listenersInvoked[9] = true;
			return true;
		});

		testEvent.register((a, b) -> {
			fail("This listener should not be called because a previous one returned true");
			return false;
		});

		boolean result = testEvent.invoker().process(1, 2);
		assertTrue(result, "L'événement devrait retourner true");

		// Vérifier que tous les listeners jusqu'au 10ème ont été appelés
		for (int i = 0; i < 10; i++) {
			assertTrue(listenersInvoked[i], "The listener " + i + " should have been invoked");
		}
	}

	// ===== LOGICAL AND =====

	@Test
	@DisplayName("Logical AND event should return true when all listeners return true")
	void testLogicalAndEvent_returnsTrue_whenAllListenersReturnTrue() {
		Event<LogicalAndEvent> event = createLogicalAndEvent();

		for (int i = 0; i < 5; i++) {
			event.register((a, b) -> true);
		}

		boolean result = event.invoker().allow(5, 10);
		assertTrue(result, "The event should return true when all listeners return true");
	}

	@Test
	@DisplayName("Logical AND event should return false when at least one listener returns false")
	void testLogicalAndEvent_returnsFalse_whenAnyListenerReturnsFalse() {
		Event<LogicalAndEvent> event = createLogicalAndEvent();

		for (int i = 0; i < 4; i++) {
			event.register((a, b) -> true);
		}

		event.register((a, b) -> false);

		boolean result = event.invoker().allow(5, 10);
		assertFalse(result, "The event should return false if at least one listener returns false");
	}

	@Test
	@DisplayName("Logical AND event should continue processing all listeners even after a false result")
	void testLogicalAndEvent_continuesProcessingAllListeners_evenAfterFalseResult() {
		Event<LogicalAndEvent> event = createLogicalAndEvent();
		final boolean[] listenersInvoked = new boolean[5];

		event.register((a, b) -> {
			listenersInvoked[0] = true;
			return true;
		});

		event.register((a, b) -> {
			listenersInvoked[1] = true;
			return false;
		});

		for (int i = 2; i < 5; i++) {
			final int index = i;
			event.register((a, b) -> {
				listenersInvoked[index] = true;
				return true;
			});
		}

		boolean result = event.invoker().allow(5, 10);
		assertFalse(result, "The event should return false");

		// Vérifier que tous les listeners ont été appelés
		for (int i = 0; i < 5; i++) {
			assertTrue(listenersInvoked[i], "The listener " + i + " should have been invoked");
		}
	}

	@Test
	@DisplayName("Logical AND event should return false when multiple listeners return false")
	void testLogicalAndEvent_returnsFalse_whenMultipleListenersReturnFalse() {
		Event<LogicalAndEvent> event = createLogicalAndEvent();

		event.register((a, b) -> true);
		event.register((a, b) -> true);

		event.register((a, b) -> false);
		event.register((a, b) -> false);

		boolean result = event.invoker().allow(5, 10);
		assertFalse(result, "Event should return false when multiple listeners return false");
	}

	@Test
	@DisplayName("Logical AND event should correctly pass parameters to listeners")
	void testLogicalAndEvent_parametersAreCorrectlyPassed() {
		Event<LogicalAndEvent> event = createLogicalAndEvent();
		final int[] capturedA = {0};
		final int[] capturedB = {0};

		event.register((a, b) -> {
			capturedA[0] = a;
			capturedB[0] = b;
			return true;
		});

		int testA = 42;
		int testB = 123;
		event.invoker().allow(testA, testB);

		assertEquals(testA, capturedA[0], "The 'a' parameter should be correctly passed to the listener");
		assertEquals(testB, capturedB[0], "The 'b' parameter should be correctly passed to the listener");
	}

	@Test
	@DisplayName("Logical AND event should return true when there are no listeners")
	void testLogicalAndEvent_returnsTrue_whenNoListeners() {
		Event<LogicalAndEvent> event = createLogicalAndEvent();

		boolean result = event.invoker().allow(5, 10);
		assertTrue(result, "The event should return true when there are no listeners");
	}

	@Test
	@DisplayName("Logical AND event's final result should not be affected by listener execution order")
	void testLogicalAndEvent_orderDoesNotMatter_forFinalResult() {
		Event<LogicalAndEvent> event1 = createLogicalAndEvent();
		event1.register((a, b) -> true);
		event1.register((a, b) -> false);
		event1.register((a, b) -> true);

		Event<LogicalAndEvent> event2 = createLogicalAndEvent();
		event2.register((a, b) -> true);
		event2.register((a, b) -> true);
		event2.register((a, b) -> false);

		boolean result1 = event1.invoker().allow(5, 10);
		boolean result2 = event2.invoker().allow(5, 10);

		assertFalse(result1, "The first event should return false");
		assertFalse(result2, "The second event should return false");
	}

	private Event<DirectEvent> createDirectEvent() {
		return EventFactory.createArrayBacked(DirectEvent.class, listeners -> (a, b) -> {
			for (DirectEvent listener : listeners) {
				return listener.process(a, b);
			}
			return false;
		});
	}

	@FunctionalInterface
	public interface DirectEvent {
		boolean process(int a, int b);
	}

	private Event<ShortCircuitEvent> createShortCircuitEvent() {
		return EventFactory.createArrayBacked(ShortCircuitEvent.class, listeners -> (a, b) -> {
			for (ShortCircuitEvent listener : listeners) {
				if (listener.process(a, b)) {
					return true;
				}
			}
			return false;
		});
	}

	@FunctionalInterface
	public interface ShortCircuitEvent {
		boolean process(int a, int b);
	}

	private Event<LogicalAndEvent> createLogicalAndEvent() {
		return EventFactory.createArrayBacked(LogicalAndEvent.class, listeners -> (a, b) -> {
			boolean allow = true;

			for (LogicalAndEvent listener : listeners) {
				allow &= listener.allow(a, b);
			}

			return allow;
		});
	}

	@FunctionalInterface
	public interface LogicalAndEvent {
		boolean allow(int a, int b);
	}
}
