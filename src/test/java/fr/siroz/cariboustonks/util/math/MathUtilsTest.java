package fr.siroz.cariboustonks.util.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {

    @Test
    public void testLerpFloat() {
        assertEquals(5.0f, MathUtils.lerp(0f, 10f, 0.5f), "Lerp should return the middle value when delta is 0.5");
        assertEquals(0.0f, MathUtils.lerp(0f, 10f, 0f), "Lerp should return the start value when delta is 0");
        assertEquals(10.0f, MathUtils.lerp(0f, 10f, 1f), "Lerp should return the end value when delta is 1");
    }

    @Test
    public void testLerpDouble() {
        assertEquals(5.0, MathUtils.lerp(0.0, 10.0, 0.5), "Lerp should return the middle value when delta is 0.5");
        assertEquals(0.0, MathUtils.lerp(0.0, 10.0, 0.0), "Lerp should return the start value when delta is 0");
        assertEquals(10.0, MathUtils.lerp(0.0, 10.0, 1.0), "Lerp should return the end value when delta is 1");
    }

    @Test
    public void testInverseLerpFloat() {
        assertEquals(0.5f, MathUtils.inverseLerp(0f, 10f, 5f), "Inverse lerp should return 0.5 when value is in the middle");
        assertEquals(0f, MathUtils.inverseLerp(0f, 10f, 0f), "Inverse lerp should return 0 when value is equal to start");
        assertEquals(1f, MathUtils.inverseLerp(0f, 10f, 10f), "Inverse lerp should return 1 when value is equal to end");
    }

    @Test
    public void testInverseLerpDouble() {
        assertEquals(0.5, MathUtils.inverseLerp(0.0, 10.0, 5.0), "Inverse lerp should return 0.5 when value is in the middle");
        assertEquals(0.0, MathUtils.inverseLerp(0.0, 10.0, 0.0), "Inverse lerp should return 0 when value is equal to start");
        assertEquals(1.0, MathUtils.inverseLerp(0.0, 10.0, 10.0), "Inverse lerp should return 1 when value is equal to end");
    }

    @Test
    public void testMapFloat() {
        assertEquals(5.0f, MathUtils.map(5.0f, 0f, 10f, 0f, 10f), "Map should return the same value when the ranges are identical");
        assertEquals(0.0f, MathUtils.map(0.0f, 0f, 10f, 0f, 10f), "Map should return the start value when source is at the start of the range");
        assertEquals(10.0f, MathUtils.map(10.0f, 0f, 10f, 0f, 10f), "Map should return the end value when source is at the end of the range");
        assertEquals(5.0f, MathUtils.map(10.0f, 0f, 20f, 0f, 10f), "Map should scale the value correctly when the source range is different");
    }

    @Test
    public void testClampInt() {
        assertEquals(5, MathUtils.clamp(5, 1, 10), "Clamp should return the value when it's within the range");
        assertEquals(1, MathUtils.clamp(0, 1, 10), "Clamp should return the minimum value when it's below the range");
        assertEquals(10, MathUtils.clamp(15, 1, 10), "Clamp should return the maximum value when it's above the range");
    }

    @Test
    public void testClampFloat() {
        assertEquals(5.0f, MathUtils.clamp(5.0f, 1.0f, 10.0f), "Clamp should return the value when it's within the range");
        assertEquals(1.0f, MathUtils.clamp(0.0f, 1.0f, 10.0f), "Clamp should return the minimum value when it's below the range");
        assertEquals(10.0f, MathUtils.clamp(15.0f, 1.0f, 10.0f), "Clamp should return the maximum value when it's above the range");
    }

    @Test
    public void testFloor() {
        assertEquals(3, MathUtils.floor(3.7), "Floor should round down to the nearest integer");
        assertEquals(-4, MathUtils.floor(-3.7), "Floor should round down to the nearest integer for negative numbers");
    }

    @Test
    public void testFastFloor() {
        assertEquals(3, MathUtils.fastFloor(3.7), "FastFloor should return the integer part of the number");
        assertEquals(-4, MathUtils.fastFloor(-3.7), "FastFloor should return the integer part of the number for negative numbers");
    }

    @Test
    public void testCeil() {
        assertEquals(4, MathUtils.ceil(3.7), "Ceil should round up to the nearest integer");
        assertEquals(-3, MathUtils.ceil(-3.7), "Ceil should round up to the nearest integer for negative numbers");
    }
}
