// Quick Reference: Photo Data Class Location
// File: domain/model/Photo.kt

package com.thewalkersoft.rewindphotos.domain.model

/**
 * PHOTO DATA CLASS
 *
 * Location: domain/model/Photo.kt
 *
 * Properties:
 * - id: Long              → Unique identifier for the photo
 * - uri: String           → URI string of the photo location
 * - dateTaken: Long       → Timestamp (milliseconds) when photo was taken
 * - displayPath: String   → Human-readable path for displaying to user
 */
data class Photo(
    val id: Long,
    val uri: String,
    val dateTaken: Long,
    val displayPath: String
)

// ============================================================================
// DEPENDENCY INJECTION MODULES
// ============================================================================

// 1. AppModule.kt (di/)
//    - Provides coroutine dispatchers (@IoDispatcher, @MainDispatcher, @DefaultDispatcher)
//
// 2. RepositoryModule.kt (di/)
//    - Binds PhotoRepositoryImpl to PhotoRepository interface

// ============================================================================
// CLEAN ARCHITECTURE STRUCTURE
// ============================================================================

// domain/                  → Business Logic (No Android dependencies)
//   ├── model/
//   │   └── Photo.kt       → ⭐ Data class
//   ├── repository/
//   │   └── PhotoRepository.kt
//   └── usecase/
//       ├── GetAllPhotosUseCase.kt
//       └── GetPhotoByIdUseCase.kt
//
// data/                    → Data Sources & Repository Implementation
//   └── repository/
//       └── PhotoRepositoryImpl.kt
//
// ui/                      → Presentation Layer
//   ├── photos/
//   │   └── PhotosViewModel.kt (@HiltViewModel)
//   └── theme/
//       ├── Color.kt
//       ├── Theme.kt
//       └── Type.kt
//
// di/                      → Dependency Injection
//   ├── AppModule.kt
//   └── RepositoryModule.kt

// ============================================================================
// USAGE EXAMPLES
// ============================================================================

// Create a Photo instance:
val photo = Photo(
    id = 12345L,
    uri = "content://media/external/images/media/12345",
    dateTaken = System.currentTimeMillis(),
    displayPath = "/storage/emulated/0/DCIM/Camera/IMG_001.jpg"
)

// Inject ViewModel in Compose:
// @Composable
// fun PhotosScreen(viewModel: PhotosViewModel = hiltViewModel()) {
//     val uiState by viewModel.uiState.collectAsState()
//     // Use uiState to display photos
// }

// Inject Use Case in ViewModel:
// @HiltViewModel
// class MyViewModel @Inject constructor(
//     private val getAllPhotosUseCase: GetAllPhotosUseCase
// ) : ViewModel() {
//     // Use the use case
// }

