package fr.siroz.cariboustonks.util.colors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorTest {

	@Test
	public void testConstructorRGB() {
		Color color = new Color(255, 0, 0);
		assertEquals(255, color.r);
		assertEquals(0, color.g);
		assertEquals(0, color.b);
		assertEquals(255, color.a);
	}

	@Test
	public void testConstructorRGBA() {
		Color color = new Color(255, 0, 0, 128); // Semi-transparent red
		assertEquals(255, color.r);
		assertEquals(0, color.g);
		assertEquals(0, color.b);
		assertEquals(128, color.a);
	}

	@Test
	public void testEquals() {
		Color color1 = new Color(255, 0, 0);
		Color color2 = new Color(255, 0, 0);
		Color color3 = new Color(0, 255, 0);

		assertEquals(color1, color2);
		assertNotEquals(color1, color3);
		assertNotEquals(null, color1);
		assertNotEquals(new Object(), color1);
	}

	@Test
	public void testFromHexString() {
		Color color1 = Color.fromHexString("#FF0000");
		assertEquals(255, color1.r);
		assertEquals(0, color1.g);
		assertEquals(0, color1.b);
		assertEquals(255, color1.a); // L'alpha par défaut doit être 255

		Color color2 = Color.fromHexString("#FF000080");
		assertEquals(255, color2.r);
		assertEquals(0, color2.g);
		assertEquals(0, color2.b);
		assertEquals(128, color2.a); // alpha doit être à 128

		Color color3 = Color.fromHexString("#GGGGGG");
		assertEquals(Color.DEFAULT, color3);
	}

	@Test
	public void testToHexString() {
		Color color = new Color(255, 0, 0, 128);
		assertEquals("#ff000080", color.toHexString());
	}

	@Test
	public void testAsInt() {
		Color color = new Color(255, 0, 0, 128);
		assertEquals(0x80ff0000, color.asInt());
	}

	@Test
	public void testWithAlpha() {
		Color color = new Color(255, 0, 0);
		Color colorWithAlpha = color.withAlpha(128);
		assertEquals(128, colorWithAlpha.a); // Vérifier si l'alpha est correctement modifié
		assertEquals(255, colorWithAlpha.r); // Les autres éléments doivent rester inchangés
	}

	@Test
	public void testConstructorFloat() {
		Color color = new Color(1f, 0f, 0f);
		assertEquals(255, color.r);
		assertEquals(0, color.g);
		assertEquals(0, color.b);
		assertEquals(255, color.a); // L'alpha par défaut doit être 255
	}

	@Test
	public void testAsFloatComponents() {
		Color color = new Color(255, 0, 0); // Red color
		float[] components = color.asFloatComponents();
		assertEquals(1f, components[0]); // R
		assertEquals(0f, components[1]); // G
		assertEquals(0f, components[2]); // B
	}

	@Test
	public void testCopyConstructor() {
		Color originalColor = new Color(255, 0, 0);
		Color copiedColor = new Color(originalColor);
		assertEquals(originalColor, copiedColor);
	}

	@Test
	public void testCopyConstructorWithAlpha() {
		Color originalColor = new Color(255, 0, 0);
		Color copiedColor = new Color(originalColor, 128); // autre alpha
		assertEquals(128, copiedColor.a); // Alpha devrait être modifié
		assertEquals(255, copiedColor.r); // Les autres component doivent rester inchangés
	}
}
