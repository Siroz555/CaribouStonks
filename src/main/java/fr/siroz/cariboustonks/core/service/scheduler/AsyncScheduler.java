package fr.siroz.cariboustonks.core.service.scheduler;

import fr.siroz.cariboustonks.CaribouStonks;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Small shared executor for blocking background tasks.
 */
public final class AsyncScheduler {

	// DEV-NOTE :
	// À la place d'utiliser un ForkJoinPool.commonPool(), il y a un blockingExecutor (ThreadPoolExecutor) dédié.
	// Le commonPool est optimisé pour les task CPU-bound, pas pour des appels bloquants comme I/O ou HTTP.
	// S'il y a des appels bloquants sur le commonPool, cela peut épuiser les worker voir
	// créer des comportements "imprévisibles" si d'autres Mods l'utilise, donc j'évite tout problème si jamais.

	private static final int THREAD_POOL_SIZE = 4;
	private static final int QUEUE_CAPACITY = 64;

	private final AtomicInteger threadCounter = new AtomicInteger();
	private final ExecutorService blockingExecutor;

	private AsyncScheduler() {
		ThreadFactory threadFactory = r -> {
			Thread thread = new Thread(r, "CaribouStonks-async-" + threadCounter.getAndIncrement());
			thread.setDaemon(true);
			thread.setUncaughtExceptionHandler((th, ex) -> {
				// log pour les exceptions non capturé
				CaribouStonks.LOGGER.error("[AsyncScheduler] Uncaught exception in thread {}: {}", th.getName(), ex.getMessage(), ex);
			});
			return thread;
		};

		// Fixed pool avec bounded queue -> empêche l'accumulation illimitée de tasks
		this.blockingExecutor = new ThreadPoolExecutor(
				THREAD_POOL_SIZE, // core
				THREAD_POOL_SIZE, // max
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(QUEUE_CAPACITY), // bounded queue
				threadFactory,
				new ThreadPoolExecutor.CallerRunsPolicy() // se dégrade "doucement" lorsque c'est full
		);
		// Techniquement useless car les Threads sont des Daemon.
		ClientLifecycleEvents.CLIENT_STOPPING.register(_client -> shutdownBlockingExecutor());
	}

	private static final class Holder {
		private static final AsyncScheduler INSTANCE = new AsyncScheduler();
	}

	public static AsyncScheduler getInstance() {
		return Holder.INSTANCE;
	}

	public @NotNull ExecutorService blockingExecutor() {
		return blockingExecutor;
	}

	/**
	 * Submit a runnable to the blocking executor and log rejections.
	 */
	@SuppressWarnings("unused")
	public void submit(@NotNull Runnable task) {
		try {
			blockingExecutor.execute(task);
		} catch (RejectedExecutionException rex) {
			// Defensive logging: car CallerRunsPolicy est utilisé
			CaribouStonks.LOGGER.warn("[AsyncScheduler] Task rejected. Running on caller thread as fallback.", rex);
			try {
				task.run();
			} catch (Throwable t) {
				CaribouStonks.LOGGER.error("[AsyncScheduler] Fallback run failed", t);
			}
		}
	}

	public void shutdownBlockingExecutor() {
		if (blockingExecutor.isShutdown()) return;

		blockingExecutor.shutdown(); // pour les nouvelles tasks
		try {
			if (!blockingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				blockingExecutor.shutdownNow(); // les tasks courantes
			}
		} catch (InterruptedException ignored) {
			// C'est très mal de faire comme ça, à ne pas reproduire chez vous.
			// Normalement, il faut interrupt le current thread après ce genre de chose
		}
	}
}
