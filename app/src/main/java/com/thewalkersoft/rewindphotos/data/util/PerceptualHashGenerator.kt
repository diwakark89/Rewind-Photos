package com.thewalkersoft.rewindphotos.data.util

import android.graphics.Bitmap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Perceptual Hash (pHash) generator for image similarity detection.
 *
 * Algorithm:
 * 1. Downscale image to 32x32 pixels
 * 2. Convert to grayscale
 * 3. Compute 2D DCT (Discrete Cosine Transform)
 * 4. Extract top-left 8x8 coefficients
 * 5. Generate 64-bit hash by comparing to average
 * 6. Compute Hamming distance for similarity
 *
 * This approach is robust to:
 * - Slight color adjustments
 * - Minor compression artifacts
 * - Small crop variations
 * - Brightness changes
 *
 * But will detect significant changes like:
 * - Different photos entirely
 * - Major rotations/crops
 * - Significant edits
 */
object PerceptualHashGenerator {
    private const val HASH_SIZE = 8
    private const val DCT_SIZE = 32

    /**
     * Generates a perceptual hash for a bitmap.
     *
     * @param bitmap Bitmap to hash (will not be modified)
     * @return 64-bit hash as Long
     */
    fun generateHash(bitmap: Bitmap): Long {
        // Downscale to 32x32 grayscale
        val reduced = downscaleToGrayscale(bitmap, DCT_SIZE)

        // Compute DCT
        val dct = computeDCT(reduced)

        // Extract top-left 8x8 (64 coefficients)
        val topLeft = Array(HASH_SIZE) { DoubleArray(HASH_SIZE) }
        for (i in 0 until HASH_SIZE) {
            for (j in 0 until HASH_SIZE) {
                topLeft[i][j] = dct[i][j]
            }
        }

        // Compute average
        var sum = 0.0
        for (i in 0 until HASH_SIZE) {
            for (j in 0 until HASH_SIZE) {
                sum += topLeft[i][j]
            }
        }
        val average = sum / (HASH_SIZE * HASH_SIZE)

        // Generate hash
        var hash = 0L
        var bit = 0
        for (i in 0 until HASH_SIZE) {
            for (j in 0 until HASH_SIZE) {
                if (topLeft[i][j] >= average) {
                    hash = hash or (1L shl bit)
                }
                bit++
            }
        }

        return hash
    }

    /**
     * Calculates Hamming distance between two hashes.
     * Lower distance = more similar images
     *
     * @param hash1 First hash
     * @param hash2 Second hash
     * @return Hamming distance (0-64)
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        val xor = hash1 xor hash2
        return xor.countOneBits()
    }

    /**
     * Calculates similarity score between two hashes.
     *
     * @param hash1 First hash
     * @param hash2 Second hash
     * @return Similarity score (0.0 to 1.0)
     *
     * Score calculation:
     * similarity = 1 - (hammingDistance / 64)
     *
     * Examples:
     * - hammingDistance = 0 → similarity = 1.0 (identical)
     * - hammingDistance = 6 → similarity = 0.906 (90.6%)
     * - hammingDistance = 32 → similarity = 0.5 (50%)
     * - hammingDistance = 64 → similarity = 0.0 (completely different)
     */
    fun calculateSimilarity(hash1: Long, hash2: Long): Float {
        val distance = hammingDistance(hash1, hash2)
        return 1f - (distance / 64f)
    }

    /**
     * Downscales bitmap to specified size and converts to grayscale.
     *
     * @param bitmap Original bitmap
     * @param size Target size (size x size)
     * @return Grayscale pixel array as 2D array of doubles (0.0-1.0)
     */
    private fun downscaleToGrayscale(bitmap: Bitmap, size: Int): Array<DoubleArray> {
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val grayscale = Array(size) { DoubleArray(size) }

        for (i in 0 until size) {
            for (j in 0 until size) {
                val pixel = scaled.getPixel(j, i)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Convert to grayscale using luminosity method
                val gray = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                grayscale[i][j] = gray
            }
        }

        return grayscale
    }

    /**
     * Computes 2D Discrete Cosine Transform.
     *
     * @param data Input grayscale pixel array (32x32)
     * @return DCT coefficients (32x32)
     */
    private fun computeDCT(data: Array<DoubleArray>): Array<DoubleArray> {
        val n = data.size
        val dct = Array(n) { DoubleArray(n) }

        for (u in 0 until n) {
            for (v in 0 until n) {
                var sum = 0.0
                val cu = if (u == 0) 1.0 / sqrt(2.0) else 1.0
                val cv = if (v == 0) 1.0 / sqrt(2.0) else 1.0

                for (x in 0 until n) {
                    for (y in 0 until n) {
                        val cosX = cos((2 * x + 1) * u * PI / (2 * n))
                        val cosY = cos((2 * y + 1) * v * PI / (2 * n))
                        sum += data[x][y] * cosX * cosY
                    }
                }

                dct[u][v] = cu * cv * sum * 4.0 / (n * n)
            }
        }

        return dct
    }
}

