package fr.siroz.cariboustonks.util.colors;

import org.junit.jupiter.api.Test;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class ColorUtilsTest {

    @Test
    public void testInterpolatedColor() {
        Color startColor = new Color(255, 0, 0); // Red
        Color endColor = new Color(0, 0, 255);   // Blue

        // Test interpolation at factor 0 (start color)
        Color result0 = ColorUtils.interpolatedColor(startColor, endColor, 0f);
        assertEquals(startColor, result0, "The interpolated color at factor 0 should be the start color.");

        // Test interpolation at factor 1 (end color)
        Color result1 = ColorUtils.interpolatedColor(startColor, endColor, 1f);
        assertEquals(endColor, result1, "The interpolated color at factor 1 should be the end color.");

        // Test interpolation at factor 0.5 (should be a blend of red and blue)
        Color resultHalf = ColorUtils.interpolatedColor(startColor, endColor, 0.5f);
        Color expectedHalf = new Color(127, 0, 127); // Purple
        assertEquals(expectedHalf, resultHalf, "The interpolated color at factor 0.5 should be a blend of red and blue.");

        // Test interpolation with a factor less than 0 (should be clamped to 0)
        Color resultNeg = ColorUtils.interpolatedColor(startColor, endColor, -0.5f);
        assertEquals(startColor, resultNeg, "The interpolated color with a negative factor should be clamped to the start color.");

        // Test interpolation with a factor greater than 1 (should be clamped to 1)
        Color resultOver = ColorUtils.interpolatedColor(startColor, endColor, 1.5f);
        assertEquals(endColor, resultOver, "The interpolated color with a factor greater than 1 should be clamped to the end color.");
    }

    @Test
    public void testChangeAlpha() {
        int originalColor = new Color(255, 0, 0).getRGB(); // Red color with full opacity
        int newAlpha = 128; // 50% opacity

        int modifiedColor = ColorUtils.changeAlpha(originalColor, newAlpha);
        Color modifiedColorObj = new Color(modifiedColor, true);

        assertEquals(newAlpha, modifiedColorObj.getAlpha(), "The alpha value should be changed to the specified value.");
        assertEquals(255, modifiedColorObj.getRed(), "The red component should remain the same.");
        assertEquals(0, modifiedColorObj.getGreen(), "The green component should remain the same.");
        assertEquals(0, modifiedColorObj.getBlue(), "The blue component should remain the same.");
    }

    @Test
    public void testGetFloatComponents() {
        int color = new Color(255, 128, 64).getRGB(); // RGB(255, 128, 64)
        float[] components = ColorUtils.getFloatComponents(color);

        assertEquals(1.0f, components[0], "The red component should be 1.0 after conversion.");
        assertEquals(0.50196f, components[1], 0.0001, "The green component should be 0.50196 after conversion.");
        assertEquals(0.25098f, components[2], 0.0001, "The blue component should be 0.25098 after conversion.");
    }
}