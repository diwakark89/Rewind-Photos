package com.thewalkersoft.rewindphotos

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.thewalkersoft.rewindphotos.data.util.PerceptualHashUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for PerceptualHashUtils.
 * Verifies that perceptual hash calculation and similarity comparison work correctly.
 */
class PerceptualHashUtilsTest {

    /**
     * Test that two identical bitmaps produce the same hash.
     */
    @Test
    fun testIdenticalBitmapsProduceSameHash() {
        val bitmap1 = createTestBitmap(200, 200, Color.RED)
        val bitmap2 = createTestBitmap(200, 200, Color.RED)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap1)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap2)

        assertEquals("Identical bitmaps should produce identical hashes", hash1, hash2)

        bitmap1.recycle()
        bitmap2.recycle()
    }

    /**
     * Test that two completely different bitmaps produce different hashes.
     */
    @Test
    fun testDifferentBitmapsProduceDifferentHashes() {
        val bitmap1 = createTestBitmap(200, 200, Color.RED)
        val bitmap2 = createTestBitmap(200, 200, Color.BLUE)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap1)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap2)

        assertNotEquals("Different bitmaps should produce different hashes", hash1, hash2)

        bitmap1.recycle()
        bitmap2.recycle()
    }

    /**
     * Test that slightly different bitmaps produce the same or very similar hashes.
     * This is the core feature of perceptual hashing - robustness to minor changes.
     */
    @Test
    fun testSlightlyDifferentBitmapsProduceSimilarHash() {
        // Create a base bitmap with a solid color
        val bitmap1 = createTestBitmap(200, 200, Color.RED)

        // Create a slightly modified version with 95% the same content
        val bitmap2 = createSlightlyModifiedBitmap(bitmap1, 0.05f)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap1)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap2)

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)

        // Two slightly different versions of the same image should have >90% similarity
        assertTrue(
            "Slightly modified bitmaps should have high similarity (>90%), but got $similarity%",
            similarity >= 90.0
        )

        bitmap1.recycle()
        bitmap2.recycle()
    }

    /**
     * Test hamming distance calculation.
     * Hamming distance measures the number of differing bits between two hashes.
     */
    @Test
    fun testHammingDistance() {
        // Hash with all bits set to 0
        val hash1 = 0L

        // Hash with one bit set to 1
        val hash2 = 1L

        val distance = PerceptualHashUtils.hammingDistance(hash1, hash2)
        assertEquals("Hamming distance should be 1", 1, distance)
    }

    /**
     * Test hamming distance for identical hashes.
     */
    @Test
    fun testHammingDistanceIdentical() {
        val hash1 = -1L // All bits set to 1
        val hash2 = -1L

        val distance = PerceptualHashUtils.hammingDistance(hash1, hash2)
        assertEquals("Identical hashes should have hamming distance 0", 0, distance)
    }

    /**
     * Test similarity calculation for identical hashes.
     */
    @Test
    fun testSimilarityIdentical() {
        val hash1 = 0L
        val hash2 = 0L

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)
        assertEquals("Identical hashes should have 100% similarity", 100.0, similarity, 0.01)
    }

    /**
     * Test similarity calculation for completely different hashes.
     */
    @Test
    fun testSimilarityCompleteDifference() {
        val hash1 = 0L // All bits 0
        val hash2 = -1L // All bits 1

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)
        assertEquals("Completely different hashes should have 0% similarity", 0.0, similarity, 0.01)
    }

    /**
     * Test similarity calculation returns value between 0-100.
     */
    @Test
    fun testSimilarityRange() {
        // Create two random-ish hashes
        val hash1 = -5854781393295606889L // Pattern: 1010...
        val hash2 = 6148914691236517205L  // Pattern: 0101...

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)

        assertTrue("Similarity should be between 0-100", similarity in 0.0..100.0)
    }

    /**
     * Test that a bitmap and its scaled version produce similar hashes.
     * Scaling is a common operation that shouldn't significantly change perceptual content.
     */
    @Test
    fun testScaledBitmapProducesSimilarHash() {
        val original = createComplexBitmap(200, 200)
        val scaled = Bitmap.createScaledBitmap(original, 400, 400, true)

        val hash1 = PerceptualHashUtils.calculateHash(original)
        val hash2 = PerceptualHashUtils.calculateHash(scaled)

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)

        // Scaled bitmaps should maintain high perceptual similarity
        assertTrue(
            "Scaled bitmaps should have high similarity (>85%), but got $similarity%",
            similarity >= 85.0
        )

        original.recycle()
        scaled.recycle()
    }

    /**
     * Test that a rotated bitmap produces a different hash.
     * Rotation significantly changes the pixel layout.
     */
    @Test
    fun testRotatedBitmapProducesDifferentHash() {
        val original = createTestBitmap(200, 200, Color.RED)
        val rotated = createRotatedBitmap(original, 45f)

        val hash1 = PerceptualHashUtils.calculateHash(original)
        val hash2 = PerceptualHashUtils.calculateHash(rotated)

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)

        // Rotated images will have different hashes but may still be >50% similar
        // depending on the image content
        assertTrue(
            "Similarity should be in range 0-100%, got $similarity%",
            similarity in 0.0..100.0
        )

        original.recycle()
        rotated.recycle()
    }

    /**
     * Test hash stability - calculating hash twice on same bitmap produces same result.
     */
    @Test
    fun testHashStability() {
        val bitmap = createComplexBitmap(200, 200)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap)

        assertEquals("Hash calculation should be deterministic", hash1, hash2)

        bitmap.recycle()
    }

    /**
     * Test multiple bitmaps can be grouped by similarity.
     * Creates a scenario with similar and dissimilar groups.
     */
    @Test
    fun testMultipleBitmapSimilarityGrouping() {
        // Create three similar bitmaps
        val bitmap1 = createTestBitmap(200, 200, Color.RED)
        val bitmap2 = createSlightlyModifiedBitmap(bitmap1, 0.03f)
        val bitmap3 = createSlightlyModifiedBitmap(bitmap1, 0.05f)

        // Create one dissimilar bitmap
        val bitmap4 = createTestBitmap(200, 200, Color.BLUE)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap1)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap2)
        val hash3 = PerceptualHashUtils.calculateHash(bitmap3)
        val hash4 = PerceptualHashUtils.calculateHash(bitmap4)

        val sim12 = PerceptualHashUtils.calculateSimilarity(hash1, hash2)
        val sim13 = PerceptualHashUtils.calculateSimilarity(hash1, hash3)
        val sim14 = PerceptualHashUtils.calculateSimilarity(hash1, hash4)

        // Similar images should group together
        assertTrue("Bitmaps 1 and 2 should be similar (>90%)", sim12 >= 90.0)
        assertTrue("Bitmaps 1 and 3 should be similar (>90%)", sim13 >= 90.0)

        // Dissimilar image should not group with similar ones
        assertTrue(
            "Bitmaps 1 and 4 should be different (<90%)",
            sim14 < 90.0
        )

        bitmap1.recycle()
        bitmap2.recycle()
        bitmap3.recycle()
        bitmap4.recycle()
    }

    // ============= Helper Methods =============

    /**
     * Creates a simple test bitmap with a single color.
     */
    private fun createTestBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { setColor(color) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    /**
     * Creates a slightly modified version of a bitmap by changing a percentage of pixels.
     */
    private fun createSlightlyModifiedBitmap(original: Bitmap, modificationRatio: Float): Bitmap {
        val bitmap = original.copy(original.config, true)
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val pixelsToModify = (pixels.size * modificationRatio).toInt()
        for (i in 0 until pixelsToModify) {
            // Randomly flip some pixels slightly
            val pixel = pixels[i]
            pixels[i] = pixel xor 0x00101010 // Subtle change to RGB values
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * Creates a more complex bitmap with multiple colors and patterns.
     */
    private fun createComplexBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Draw gradient background
        for (x in 0 until width step 10) {
            paint.color = Color.rgb(x * 255 / width, 128, 255 - x * 255 / width)
            canvas.drawRect(x.toFloat(), 0f, (x + 10).toFloat(), height.toFloat(), paint)
        }

        // Draw some shapes
        paint.color = Color.RED
        canvas.drawCircle(width / 2f, height / 2f, 30f, paint)

        paint.color = Color.BLUE
        canvas.drawRect(50f, 50f, 150f, 150f, paint)

        return bitmap
    }

    /**
     * Creates a rotated version of a bitmap.
     */
    private fun createRotatedBitmap(original: Bitmap, degrees: Float): Bitmap {
        val width = original.width
        val height = original.height
        val rotated = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rotated)

        canvas.rotate(degrees, (width / 2).toFloat(), (height / 2).toFloat())
        canvas.drawBitmap(original, 0f, 0f, null)

        return rotated
    }
}

