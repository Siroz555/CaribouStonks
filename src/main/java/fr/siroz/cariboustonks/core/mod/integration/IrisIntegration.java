package fr.siroz.cariboustonks.core.mod.integration;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.rendering.CaribouRenderPipelines;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

/**
 * Provides compatibility with Iris.
 * <p>
 * <a href="https://github.com/SkyblockerMod/Skyblocker/pull/1691">Skyblocker PR</a>
 * <a href="https://github.com/nea89o/Firmament/pull/306">Firmament PR</a>
 *
 * @author MicrocontrollersDev (<a href="https://github.com/MicrocontrollersDev">MicrocontrollersDev GitHub</a>)
 */
public final class IrisIntegration {

	private static final boolean IRIS_ENABLED = FabricLoader.getInstance().isModLoaded("iris");
	private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";
	private static final String IRIS_PROGRAM_CLASS = "net.irisshaders.iris.api.v0.IrisProgram";
	private static final MethodHandle GET_IRIS_API = getIrisApiHandle();
	private static final MethodHandle REGISTER_PIPELINE = registerPipelineHandle();
	private static final MethodHandle GET_IRIS_PROGRAM = getIrisProgramHandle();

	private IrisIntegration() {
	}

	/**
	 * Assigns all the mod's necessary pipelines to an iris program.
	 */
	public static void assignPipelines() {
		if (IRIS_ENABLED) {
			// assignPipeline(RenderPipelines.DEBUG_FILLED_BOX, "BASIC");
			assignPipeline(CaribouRenderPipelines.FILLED, "BASIC");
			assignPipeline(CaribouRenderPipelines.FILLED_THROUGH_BLOCKS, "BASIC");
			assignPipeline(CaribouRenderPipelines.LINE_STRIP, "LINES");
			assignPipeline(CaribouRenderPipelines.LINES_THROUGH_BLOCKS, "LINES");
			assignPipeline(CaribouRenderPipelines.QUADS_THROUGH_BLOCKS, "BASIC");
			assignPipeline(CaribouRenderPipelines.TEXTURE, "TEXTURED");
			assignPipeline(CaribouRenderPipelines.TEXTURE_THROUGH_BLOCKS, "TEXTURED");
			assignPipeline(CaribouRenderPipelines.CIRCLE, "BASIC");
			assignPipeline(CaribouRenderPipelines.CIRCLE_THROUGH_BLOCKS, "BASIC");
		}
	}

	/**
	 * Assigns a pipeline to a given {@code IrisProgram}.
	 *
	 * @param pipeline        The pipeline to be assigned.
	 * @param irisProgramName The exact name of the {@code IrisProgram} enum entry.
	 */
	private static void assignPipeline(RenderPipeline pipeline, String irisProgramName) {
		try {
			Objects.requireNonNull(GET_IRIS_API, "Iris API handle must be present to assign a pipeline.");
			Objects.requireNonNull(REGISTER_PIPELINE, "Iris register pipeline handle must be present to assign a pipeline.");
			Objects.requireNonNull(GET_IRIS_PROGRAM, "Iris Program handle must be present to assign a pipeline.");
			REGISTER_PIPELINE.invoke(GET_IRIS_API.invoke(), pipeline, GET_IRIS_PROGRAM.invoke(irisProgramName));
		} catch (IllegalStateException ignored) {
			// The pipeline was probably already registered
		} catch (Throwable ex) {
			CaribouStonks.LOGGER.error("[IrisCompatibility] Failed to assign pipeline {} to {}.", pipeline.getLocation(), irisProgramName, ex);
		}
	}

	private static @Nullable MethodHandle getIrisApiHandle() {
		try {
			Class<?> irisApiClass = Class.forName(IRIS_API_CLASS);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodType type = MethodType.methodType(irisApiClass);
			return lookup.findStatic(irisApiClass, "getInstance", type);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static @Nullable MethodHandle registerPipelineHandle() {
		try {
			Class<?> irisApiClass = Class.forName(IRIS_API_CLASS);
			Class<?> irisProgramClass = Class.forName(IRIS_PROGRAM_CLASS);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodType type = MethodType.methodType(void.class, RenderPipeline.class, irisProgramClass);
			return lookup.findVirtual(irisApiClass, "assignPipeline", type);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static @Nullable MethodHandle getIrisProgramHandle() {
		try {
			Class<?> irisProgramClass = Class.forName(IRIS_PROGRAM_CLASS);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodType type = MethodType.methodType(Enum.class, Class.class, String.class);
			MethodHandle enumValueOf = lookup.findStatic(Enum.class, "valueOf", type);
			return MethodHandles.insertArguments(enumValueOf, 0, irisProgramClass);
		} catch (Exception ignored) {
			return null;
		}
	}
}
