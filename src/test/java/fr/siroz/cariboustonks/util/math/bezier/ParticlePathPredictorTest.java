package fr.siroz.cariboustonks.util.math.bezier;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticlePathPredictorTest {

	private ParticlePathPredictor predictor;

	@BeforeEach
	public void setup() {
		predictor = new ParticlePathPredictor(3); // Cubique
	}

	@Test
	@DisplayName("The predictor should return null without sufficient points")
	public void testNullResultWithoutPoints() {
		Vec3d result = predictor.solve();
		assertNull(result);
	}

	@Test
	@DisplayName("The predictor should return null with a single point")
	public void testNullResultWithOnePoint() {
		predictor.addPoint(new Vec3d(1, 1, 1));
		Vec3d result = predictor.solve();
		assertNull(result);
	}

	@Test
	@DisplayName("The predictor should compute a solution with enough points")
	public void testSolutionWithSufficientPoints() {
		// Trajectoire de particule plausible
		predictor.addPoint(new Vec3d(0, 0, 0));
		predictor.addPoint(new Vec3d(1, 1, 0));
		predictor.addPoint(new Vec3d(2, 1.8, 0));
		predictor.addPoint(new Vec3d(3, 2.5, 0));
		predictor.addPoint(new Vec3d(4, 3.0, 0));

		Vec3d result = predictor.solve();
		assertNotNull(result);
		assertTrue(result.x > 4.0, "Le point prédit devrait être après le dernier point en x");
	}

	@Test
	@DisplayName("The predictor should calculate a trajectory consistent with the points provided")
	public void testTrajectoryConsistency() {
		// Trajectoire parabolique (bow)
		predictor.addPoint(new Vec3d(0, 0, 0));
		predictor.addPoint(new Vec3d(1, 1, 0));
		predictor.addPoint(new Vec3d(2, 1.8, 0));
		predictor.addPoint(new Vec3d(3, 2.2, 0));
		predictor.addPoint(new Vec3d(4, 2.0, 0));
		predictor.addPoint(new Vec3d(5, 1.5, 0));

		// Tendance descendante
		Vec3d result = predictor.solve();
		assertNotNull(result);
		assertTrue(result.x > 5.0, "Le point prédit devrait être après le dernier point en x");
		assertTrue(result.y < 1.5, "Le point prédit devrait continuer la tendance descendante en y");
	}

	@Test
	@DisplayName("The predictor should correctly calculate the pitch weighting")
	public void testPitchWeightCalculation() {
		ParticlePathPredictor testPredictor = new ParticlePathPredictor(3);

		// Horizontale
		double weight1 = testPredictor.computePitchWeight(new Vec3d(1, 0, 0));
		// Vers le bas
		double weight2 = testPredictor.computePitchWeight(new Vec3d(0, -1, 0));
		// Vers le haut
		double weight3 = testPredictor.computePitchWeight(new Vec3d(0, 1, 0));

		assertTrue(weight1 > 0);
		assertTrue(weight2 > 0);
		assertTrue(weight3 > 0);
		assertNotEquals(weight1, weight2, 0.1);
		assertNotEquals(weight1, weight3, 0.1);
		assertNotEquals(weight2, weight3, 0.1);
	}

	@Test
	@DisplayName("The predictor should correctly calculate the pitch from the derivative")
	public void testPitchFromDerivative() {
		ParticlePathPredictor testPredictor = new ParticlePathPredictor(3);

		// Horizontale
		double pitch1 = testPredictor.getPitchFromDerivative(new Vec3d(1, 0, 0));
		// Vers le bas
		double pitch2 = testPredictor.getPitchFromDerivative(new Vec3d(0, -1, 0));
		// Vers le haut
		double pitch3 = testPredictor.getPitchFromDerivative(new Vec3d(0, 1, 0));

		assertTrue(pitch2 > 0, "Le pitch pour un vecteur vers le bas devrait être positif");
		assertTrue(pitch3 < 0, "Le pitch pour un vecteur vers le haut devrait être négatif");
		assertTrue(pitch2 > pitch1, "Le pitch vers le bas devrait être plus grand que le pitch horizontal");
		assertTrue(pitch1 > pitch3, "Le pitch horizontal devrait être plus grand que le pitch vers le haut");
		// le "assertFalse(Double.isNaN(pitchX))" sera toujours en false
		assertFalse(Double.isInfinite(pitch1));
		assertFalse(Double.isInfinite(pitch2));
		assertFalse(Double.isInfinite(pitch3));
	}
}
