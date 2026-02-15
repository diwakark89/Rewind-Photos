package com.thewalkersoft.rewindphotos.test

import android.graphics.Bitmap
import android.graphics.Color
import com.thewalkersoft.rewindphotos.data.util.PerceptualHashUtils
import com.thewalkersoft.rewindphotos.domain.model.Photo
import com.thewalkersoft.rewindphotos.domain.model.PhotoGroup
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Perceptual Hash Utilities
 */
class PerceptualHashUtilsTest {

    @Test
    fun testCalculateHashReturnsLong() {
        val bitmap = createTestBitmap(width = 100, height = 100, color = Color.RED)
        val hash = PerceptualHashUtils.calculateHash(bitmap)
        assertNotEquals(0L, hash)
    }

    @Test
    fun testIdenticalBitmapsProduceSameHash() {
        val bitmap1 = createTestBitmap(width = 100, height = 100, color = Color.RED)
        val bitmap2 = createTestBitmap(width = 100, height = 100, color = Color.RED)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap1)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap2)

        assertEquals(hash1, hash2)
    }

    @Test
    fun testDifferentBitmapsProduceDifferentHashes() {
        val bitmap1 = createTestBitmap(width = 100, height = 100, color = Color.RED)
        val bitmap2 = createTestBitmap(width = 100, height = 100, color = Color.BLUE)

        val hash1 = PerceptualHashUtils.calculateHash(bitmap1)
        val hash2 = PerceptualHashUtils.calculateHash(bitmap2)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun testHammingDistanceCalculation() {
        // Same hashes should have distance 0
        val hash = 0xFF00FF00L
        val distance = PerceptualHashUtils.hammingDistance(hash, hash)
        assertEquals(0, distance)
    }

    @Test
    fun testHammingDistanceWithDifferentHashes() {
        val hash1 = 0b1111000011110000111100001111000011110000111100001111000011110000L
        val hash2 = 0b1111000011110000111100001111000011110000111100000000000000000000L

        val distance = PerceptualHashUtils.hammingDistance(hash1, hash2)
        assertEquals(16, distance)  // 16 bits are different
    }

    @Test
    fun testSimilarityCalculation() {
        // Identical hashes should have 100% similarity
        val hash = 0xFFFFFFFFFFFFFFFFL
        val similarity = PerceptualHashUtils.calculateSimilarity(hash, hash)
        assertEquals(100.0, similarity, 0.01)
    }

    @Test
    fun testSimilarityRange() {
        val hash1 = 0xFFFFFFFFFFFFFFFFL
        val hash2 = 0x0000000000000000L

        val similarity = PerceptualHashUtils.calculateSimilarity(hash1, hash2)

        // Similarity should be between 0 and 100
        assertTrue(similarity >= 0.0)
        assertTrue(similarity <= 100.0)
    }

    @Test
    fun testSimilaritySymmetry() {
        val hash1 = 0xABCDEF1234567890L
        val hash2 = 0x1234567890ABCDEFL

        val similarity1 = PerceptualHashUtils.calculateSimilarity(hash1, hash2)
        val similarity2 = PerceptualHashUtils.calculateSimilarity(hash2, hash1)

        assertEquals(similarity1, similarity2, 0.01)
    }

    // Helper function to create test bitmaps
    private fun createTestBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}

/**
 * Unit tests for PhotoGroup model
 */
class PhotoGroupTest {

    private val testPhotos = listOf(
        Photo(id = 1L, uri = "uri1", dateTaken = 1000L, displayPath = "/path1"),
        Photo(id = 2L, uri = "uri2", dateTaken = 2000L, displayPath = "/path2"),
        Photo(id = 3L, uri = "uri3", dateTaken = 3000L, displayPath = "/path3")
    )

    @Test
    fun testPhotoGroupCreation() {
        val group = PhotoGroup(photos = testPhotos, similarity = 95.5)
        assertEquals(3, group.photos.size)
        assertEquals(95.5, group.similarity, 0.01)
        assertEquals(0, group.selectedForDeletion.size)
    }

    @Test
    fun testTogglePhotoSelection() {
        val group = PhotoGroup(photos = testPhotos, similarity = 95.0)

        // Select photo 1
        val updated1 = group.togglePhotoSelection(1L)
        assertTrue(updated1.isPhotoSelected(1L))
        assertFalse(updated1.isPhotoSelected(2L))

        // Deselect photo 1
        val updated2 = updated1.togglePhotoSelection(1L)
        assertFalse(updated2.isPhotoSelected(1L))
    }

    @Test
    fun testSelectAll() {
        val group = PhotoGroup(photos = testPhotos, similarity = 95.0)
        val updated = group.selectAll()

        assertTrue(updated.isPhotoSelected(1L))
        assertTrue(updated.isPhotoSelected(2L))
        assertTrue(updated.isPhotoSelected(3L))
        assertEquals(3, updated.selectedForDeletion.size)
    }

    @Test
    fun testDeselectAll() {
        val group = PhotoGroup(
            photos = testPhotos,
            similarity = 95.0,
            selectedForDeletion = setOf(1L, 2L, 3L)
        )
        val updated = group.deselectAll()

        assertFalse(updated.isPhotoSelected(1L))
        assertFalse(updated.isPhotoSelected(2L))
        assertFalse(updated.isPhotoSelected(3L))
        assertEquals(0, updated.selectedForDeletion.size)
    }

    @Test
    fun testIsPhotoSelected() {
        val group = PhotoGroup(
            photos = testPhotos,
            similarity = 95.0,
            selectedForDeletion = setOf(1L, 2L)
        )

        assertTrue(group.isPhotoSelected(1L))
        assertTrue(group.isPhotoSelected(2L))
        assertFalse(group.isPhotoSelected(3L))
    }
}

/**
 * Integration tests (example structure)
 * Note: These would need actual context/repository dependencies
 */
class DuplicatePhotoGroupingIntegrationTest {

    // Example test structure (would need mocking/dependency injection)
    /*
    @Test
    fun testGroupPhotosBySimilarity() = runTest {
        // Setup
        val mockRepository = mock<PhotoRepository>()
        val useCase = GetDuplicatePhotosUseCase(mockRepository)

        // Create test photos with similar hashes
        val testPhotos = listOf(
            Photo(1, "uri1", 1000, "/path1"),
            Photo(2, "uri2", 2000, "/path2"),
            Photo(3, "uri3", 3000, "/path3")
        )

        // Mock repository behavior
        whenever(mockRepository.groupPhotosBySimilarity(90.0)).thenReturn(
            flowOf(listOf(
                PhotoGroup(
                    photos = testPhotos,
                    similarity = 92.5
                )
            ))
        )

        // Test
        val result = useCase(90.0)

        result.test {
            val groups = awaitItem()
            assertTrue(groups.isNotEmpty())
            assertEquals(1, groups.size)
            assertEquals(3, groups[0].photos.size)
        }
    }
    */
}

/**
 * Example test data builders for convenience
 */
object TestDataBuilders {

    fun createTestPhoto(
        id: Long = 1L,
        uri: String = "content://media/external/images/media/$id",
        dateTaken: Long = System.currentTimeMillis(),
        displayPath: String = "/storage/emulated/0/Pictures/photo_$id.jpg"
    ): Photo = Photo(
        id = id,
        uri = uri,
        dateTaken = dateTaken,
        displayPath = displayPath
    )

    fun createTestPhotoGroup(
        photoCount: Int = 3,
        similarity: Double = 92.5,
        selectedIds: Set<Long> = emptySet()
    ): PhotoGroup {
        val photos = (1L..photoCount).map { id ->
            createTestPhoto(id = id)
        }
        return PhotoGroup(
            photos = photos,
            similarity = similarity,
            selectedForDeletion = selectedIds
        )
    }

    fun createTestBitmap(
        width: Int = 100,
        height: Int = 100,
        color: Int = Color.RED
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}

/**
 * Performance benchmark example
 */
object PerformanceBenchmark {

    fun benchmarkHashCalculation() {
        val bitmap = TestDataBuilders.createTestBitmap(1000, 1000)

        val startTime = System.currentTimeMillis()
        val hash = PerceptualHashUtils.calculateHash(bitmap)
        val endTime = System.currentTimeMillis()

        println("Hash calculation time: ${endTime - startTime}ms")
        println("Hash value: $hash")
    }

    fun benchmarkGrouping(photoCount: Int = 100) {
        val photos = (1L..photoCount).map { id ->
            TestDataBuilders.createTestPhoto(id = id)
        }

        val hashes = photos.associateBy({ it.id }, {
            TestDataBuilders.createTestBitmap().let { bitmap ->
                PerceptualHashUtils.calculateHash(bitmap)
            }
        })

        val startTime = System.currentTimeMillis()

        // Simulate grouping
        val groups = mutableListOf<List<Long>>()
        val processed = mutableSetOf<Long>()

        for (photoId in hashes.keys) {
            if (photoId in processed) continue

            val group = mutableListOf(photoId)
            val hash = hashes[photoId]!!
            processed.add(photoId)

            for (otherId in hashes.keys) {
                if (otherId in processed) continue

                val otherHash = hashes[otherId]!!
                val similarity = PerceptualHashUtils.calculateSimilarity(hash, otherHash)

                if (similarity >= 90.0) {
                    group.add(otherId)
                    processed.add(otherId)
                }
            }

            if (group.size > 1) {
                groups.add(group)
            }
        }

        val endTime = System.currentTimeMillis()

        println("Grouping $photoCount photos took: ${endTime - startTime}ms")
        println("Groups found: ${groups.size}")
    }
}

