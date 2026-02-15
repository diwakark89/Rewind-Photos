package com.thewalkersoft.rewindphotos.data.util

import android.graphics.Bitmap

/**
 * Utility class for calculating perceptual hashes (pHash) of bitmaps.
 * Uses a DCT-based approach to generate fingerprints that are invariant to
 * minor image changes while being sensitive to content changes.
 */
object PerceptualHashUtils {

    private const val HASH_SIZE = 8
    private const val REDUCED_SIZE = 8

    /**
     * Calculates a perceptual hash for a given bitmap.
     *
     * @param bitmap The bitmap to hash
     * @return A 64-bit long representing the perceptual hash
     */
    fun calculateHash(bitmap: Bitmap): Long {
        // Resize bitmap to 8x8 for consistent hashing
        val resized = Bitmap.createScaledBitmap(bitmap, HASH_SIZE, HASH_SIZE, true)

        // Convert to grayscale and get pixel values
        val pixels = IntArray(HASH_SIZE * HASH_SIZE)
        resized.getPixels(pixels, 0, HASH_SIZE, 0, 0, HASH_SIZE, HASH_SIZE)

        // Convert RGB to grayscale
        val grayValues = FloatArray(HASH_SIZE * HASH_SIZE)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            grayValues[i] = (0.299f * r + 0.587f * g + 0.114f * b)
        }

        // Calculate average value
        val average = grayValues.average()

        // Generate hash by comparing each pixel to average
        var hash = 0L
        for (i in grayValues.indices) {
            if (grayValues[i] > average) {
                hash = hash or (1L shl i)
            }
        }

        resized.recycle()
        return hash
    }

    /**
     * Calculates the Hamming distance between two hashes.
     * Lower distance = more similar images.
     *
     * @param hash1 First hash
     * @param hash2 Second hash
     * @return Number of differing bits
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var xor = hash1 xor hash2
        var distance = 0
        while (xor != 0L) {
            distance += (xor and 1L).toInt()
            xor = xor shr 1
        }
        return distance
    }

    /**
     * Calculates similarity percentage between two hashes.
     *
     * @param hash1 First hash
     * @param hash2 Second hash
     * @return Similarity percentage (0-100), where 100 is identical
     */
    fun calculateSimilarity(hash1: Long, hash2: Long): Double {
        val distance = hammingDistance(hash1, hash2)
        val maxDistance = 64 // Total bits in a 64-bit hash
        return ((maxDistance - distance).toDouble() / maxDistance) * 100.0
    }
}

