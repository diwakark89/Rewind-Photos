# RewindPhotos — Project Architecture Blueprint

> **Generated:** 2026-03-08  
> **Architecture Pattern:** Clean Architecture + MVVM  
> **Technology Stack:** Android · Kotlin · Jetpack Compose · Hilt · Coroutines/Flow  
> **Package:** `com.thewalkersoft.rewindphotos`  
> **Min SDK:** 26 (Android 8.0) · **Target SDK:** 34 (Android 14)

---

## Table of Contents

1. [Architectural Overview](#1-architectural-overview)
2. [Architecture Visualization](#2-architecture-visualization)
3. [Core Architectural Components](#3-core-architectural-components)
4. [Architectural Layers and Dependencies](#4-architectural-layers-and-dependencies)
5. [Data Architecture](#5-data-architecture)
6. [Cross-Cutting Concerns](#6-cross-cutting-concerns)
7. [Service Communication Patterns](#7-service-communication-patterns)
8. [Technology-Specific Patterns](#8-technology-specific-patterns)
9. [Implementation Patterns](#9-implementation-patterns)
10. [Testing Architecture](#10-testing-architecture)
11. [Deployment Architecture](#11-deployment-architecture)
12. [Extension and Evolution Patterns](#12-extension-and-evolution-patterns)
13. [Architectural Decision Records](#13-architectural-decision-records)
14. [Architecture Governance](#14-architecture-governance)
15. [Blueprint for New Development](#15-blueprint-for-new-development)

---

## 1. Architectural Overview

RewindPhotos is an Android photo gallery and memory-browsing application. It is built on **Clean Architecture** with three strict concentric layers (Domain → Data → UI) combined with the **MVVM** presentation pattern driven by Jetpack Compose and Kotlin StateFlow.

### Guiding Principles

| Principle | Application |
|---|---|
| **Dependency Rule** | Inner layers (Domain) know nothing about outer layers (Data, UI) |
| **Single Responsibility** | Each use case performs one operation; each ViewModel owns one screen's state |
| **Interface Segregation** | Repository and detector contracts are minimal, focused interfaces |
| **Reactive State** | All UI state is driven by `StateFlow`; async work uses `Flow` |
| **Lifecycle Safety** | `collectAsStateWithLifecycle()` ensures no memory leaks in Composables |
| **Privacy First** | All processing is on-device; no cloud calls, no third-party analytics |

### Architectural Boundaries Enforced By

- **Package structure** (`domain/`, `data/`, `ui/`) — violations are immediately visible
- **Hilt DI** — the DI graph wires interfaces to implementations at the `data` layer boundary
- **Interface contracts** — `domain/repository/` defines the contract; `data/repository/` provides implementations
- All **use cases** sit exclusively in the domain layer and depend only on domain interfaces

---

## 2. Architecture Visualization

### C4 Level 1 — System Context

```
┌─────────────────────────────────────────────────────┐
│                    Android Device                    │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │             RewindPhotos App                 │   │
│  │                                              │   │
│  │  User ──► Compose UI ──► ViewModels          │   │
│  │                            │                 │   │
│  │                         Use Cases            │   │
│  │                            │                 │   │
│  │                    Data Repositories         │   │
│  │                            │                 │   │
│  └────────────────────────────┼─────────────────┘   │
│                               │                     │
│              ┌────────────────┼────────────────┐    │
│              ▼                ▼                ▼    │
│        MediaStore          File I/O          EXIF   │
│         (photos)          (bitmaps)         (GPS)   │
└─────────────────────────────────────────────────────┘
```

### C4 Level 2 — Container Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      RewindPhotos Application                    │
│                                                                 │
│  ┌──────────────────────┐    ┌──────────────────────────────┐   │
│  │   UI Layer           │    │   Domain Layer               │   │
│  │  (Presentation)      │    │   (Business Logic)           │   │
│  │                      │    │                              │   │
│  │  Compose Screens     │    │  Use Cases                   │   │
│  │  ViewModels          │───►│  Repository Interfaces       │   │
│  │  UI States           │    │  Domain Models               │   │
│  │  Navigation          │    │  Utility (PermissionUtils)   │   │
│  └──────────────────────┘    └──────────────┬───────────────┘   │
│                                             │ (implements)       │
│                              ┌──────────────▼───────────────┐   │
│                              │   Data Layer                 │   │
│                              │                              │   │
│                              │  Repository Implementations  │   │
│                              │  MediaStore Queries          │   │
│                              │  pHash Utilities             │   │
│                              │  Duplicate Detector          │   │
│                              └──────────────────────────────┘   │
│                                                                 │
│  ┌──────────────────────┐    ┌──────────────────────────────┐   │
│  │   DI (Hilt)          │    │   Image Loading (Coil)       │   │
│  │  AppModule           │    │  Memory cache: 30% RAM       │   │
│  │  RepositoryModule    │    │  Disk cache: 1 GB            │   │
│  │  CoilModule          │    │  Video frame support         │   │
│  └──────────────────────┘    └──────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### C4 Level 3 — Component Diagram (Data Flow)

```
User Action
    │
    ▼
[Compose Screen]
    │  calls ViewModel function
    ▼
[ViewModel]  ◄──── collectAsStateWithLifecycle() ◄────────┐
    │  invokes use case                                    │
    ▼                                                      │
[Use Case]                                                 │
    │  calls repository interface                          │
    ▼                                             emits StateFlow
[PhotoRepository / DuplicateDetector]  (interface)        │
    │  (Hilt wires impl at runtime)                        │
    ▼                                                      │
[MediaStoreRepository / LocalDuplicateDetector]            │
    │  queries device storage                              │
    ▼                                                      │
[Android MediaStore / File I/O]                            │
    │  raw data returned                                   │
    └──────► Domain Models ──► Use Case ──► ViewModel ─────┘
```

### Navigation Graph

```
RewindPhotosApp (NavHost)
    │
    ├── "rewind"  (start)
    │       └── RewindScreen
    │
    ├── "gallery"
    │       └── GalleryScreen
    │               └── "photo_detail?photo_uri={}&photo_date={}"
    │                       └── PhotoDetailScreen
    │
    ├── "settings"
    │       └── SettingsScreen
    │
    └── "selection"
            └── SelectionScreen
```

---

## 3. Core Architectural Components

### 3.1 Domain Layer

**Purpose:** Pure Kotlin business logic — zero Android framework imports except `@HiltViewModel` and coroutines.

#### Domain Models

| Class | Responsibility | Key Fields |
|---|---|---|
| `Photo` | Primary media item (image or video) | `id`, `uri`, `dateTaken`, `displayPath`, `location`, `mediaType` |
| `PhotoGroup` | Grouped similar photos with selection state | `photos`, `isSelected`, `groupId` |
| `DuplicateGroup` | Result of duplicate detection | `representativePhoto`, `similarPhotos`, `similarityScore` |
| `MemoryGroup` | "On This Day" memory cluster | `year`, `yearsAgo`, `photos` |
| `MonthSection` | Photos grouped by year + month | `year`, `month`, `location`, `photos`, `photoCount` |
| `GroupedPhotosData` | Container of month sections with scroll hint | `sections`, `closestSectionIndex` |

**`PhotoGroup` — Interactive Selection Methods:**
```kotlin
data class PhotoGroup(
    val photos: List<Photo>,
    val groupId: String,
    private val _selectedPhotoIds: MutableSet<Long> = mutableSetOf()
) {
    fun togglePhotoSelection(photoId: Long)
    fun selectAll()
    fun deselectAll()
    fun isPhotoSelected(photoId: Long): Boolean
}
```

#### Repository Interfaces (Domain Contracts)

**`PhotoRepository.kt`**
```kotlin
interface PhotoRepository {
    fun getAllPhotos(): Flow<List<Photo>>
    suspend fun getPhotoById(id: Long): Photo?
    fun groupPhotosBySimilarity(similarityThreshold: Double = 90.0): Flow<List<PhotoGroup>>
    suspend fun deletePhoto(photoId: Long): Boolean
    suspend fun deletePhotos(photoIds: List<Long>): Int
}
```

**`DuplicateDetector.kt`**
```kotlin
interface DuplicateDetector {
    suspend fun detectDuplicates(
        photos: List<Photo>,
        threshold: Float = 0.90f
    ): Flow<List<DuplicateGroup>>
    suspend fun clearCache()
}
```

#### Use Cases

Each use case is a single-responsibility class with an `invoke` operator:

| Use Case | Returns | Purpose |
|---|---|---|
| `GetAllPhotosUseCase` | `Flow<List<Photo>>` | Fetch all device photos/videos |
| `GetPhotoByIdUseCase` | `Photo?` | Fetch single photo |
| `GetDuplicatePhotosUseCase` | `Flow<List<PhotoGroup>>` | Fetch similarity groups |
| `GetMemoriesUseCase` | `List<MemoryGroup>` | Filter "On This Day" by current date |
| `DetectDuplicatesUseCase` | `Flow<List<DuplicateGroup>>` | Full duplicate detection workflow |
| `CleanupUseCase` | `Flow<List<PhotoGroup>>` | Orchestrate cleanup operations |
| `RequestDeletePhotosUseCase` | `IntentSender?` | Create Android 13+ delete request |

---

### 3.2 Data Layer

**Purpose:** Android-aware implementations — MediaStore access, file I/O, hash computation.

#### Repository Implementations

**`MediaStoreRepository.kt`** — Primary data source (~557 lines)
- Queries `MediaStore.Images.Media` and `MediaStore.Video.Media`
- Extracts EXIF GPS location data
- Computes perceptual hashes for all photos
- Groups photos by pHash similarity using a bucketing algorithm (avoids O(n²))
- Handles deletion: `ContentResolver.delete()` on API < 30, `MediaStore.createDeleteRequest()` on API 30+
- Uses `@IoDispatcher` for MediaStore queries and `@DefaultDispatcher` for hash computation

**`PhotoRepositoryImpl.kt`** — Adapter/Wrapper
- Implements `PhotoRepository` interface
- Delegates all operations to `MediaStoreRepository`
- Enables easy substitution (e.g., for testing with a mock)

**`LocalDuplicateDetector.kt`** (~206 lines)
- Implements `DuplicateDetector` interface
- Bucketing-based grouping (not O(n²) brute-force)
- In-memory hash cache for the session
- Returns `Flow<List<DuplicateGroup>>` with similarity scores
- Sorts results by similarity descending

#### Utility Classes

**`PerceptualHashUtils.kt`** — Simple 8×8 pHash
```kotlin
fun calculateHash(bitmap: Bitmap): Long
fun hammingDistance(hash1: Long, hash2: Long): Int
fun calculateSimilarity(hash1: Long, hash2: Long): Double  // 0–100
```

**`PerceptualHashGenerator.kt`** — Advanced 32×32 DCT pHash
```kotlin
fun generateHash(bitmap: Bitmap): Long
fun hammingDistance(hash1: Long, hash2: Long): Int
fun calculateSimilarity(hash1: Long, hash2: Long): Float   // 0.0–1.0
```
Algorithm: Downscale → 2D DCT → top-left 8×8 coefficients → 64-bit hash.

---

### 3.3 Presentation Layer (UI)

**Purpose:** Jetpack Compose screens driven by MVVM ViewModels.

#### ViewModels

| ViewModel | Screen | State Type | Key Responsibilities |
|---|---|---|---|
| `MainViewModel` | MainScreen | `MainUiState` | Load photos, extract years, manage current date |
| `GalleryViewModel` | GalleryScreen | `GalleryUiState` | Group photos by month, reverse-chronological order |
| `RewindViewModel` | RewindScreen | `RewindUiState` | Memories, selection mode, year filtering, delete flow |
| `PermissionViewModel` | Permission gate | `PermissionUiState` | Permission state machine |
| `SelectionViewModel` | SelectionScreen | `SelectionUiState` | Multi-select state |
| `PhotosViewModel` | Photos subscreen | `PhotosUiState` | Photos loading state |

**`RewindViewModel.kt`** — Most Complex (~422 lines)

Manages multiple concurrent state flows:
```kotlin
private val _uiState = MutableStateFlow<RewindUiState>(RewindUiState.Loading)
private val _selectedPhotos = MutableStateFlow<Set<Long>>(emptySet())
private val _isSelectionMode = MutableStateFlow(false)
private val _scrollRequest = MutableStateFlow<Int?>(null)
private val _deleteIntentSender = MutableStateFlow<IntentSender?>(null)
```

Key operations:
- `togglePhotoSelection(photoId)` — add/remove from selection set
- `selectAllPhotosInMonth(monthSection)` — toggle entire month
- `selectAllPhotos()` — toggle all visible photos
- `onYearSelected(year)` — filter + trigger auto-scroll
- `deleteSelectedPhotos()` — initiate Android 13+ delete flow
- `onDeletePermissionGranted/Denied()` — callback handlers

#### Composable Screens

| Screen | Route | Primary Feature |
|---|---|---|
| `RewindScreen` | `rewind` | "On This Day" memory feed with year chips + selection mode |
| `GalleryScreen` | `gallery` | `LazyVerticalGrid` 3-column photo browser with month headers |
| `PhotoDetailScreen` | `photo_detail` | Full-screen photo viewer |
| `SettingsScreen` | `settings` | App configuration |
| `SelectionScreen` | `selection` | Multi-photo selection with batch actions |
| `PermissionGatedGalleryScreen` | — | Permission request gate composable |

#### UI State Pattern

All screens use the sealed class pattern:
```kotlin
sealed class GalleryUiState {
    data object Loading : GalleryUiState()
    data object Empty : GalleryUiState()
    data class Success(val groupedPhotosData: GroupedPhotosData) : GalleryUiState()
    data class Error(val message: String) : GalleryUiState()
}
```

---

## 4. Architectural Layers and Dependencies

```
┌──────────────────────────────────────┐
│            UI Layer                  │  ← Jetpack Compose + ViewModels
│  ui/{screen}/Screen.kt              │
│  ui/{screen}/ViewModel.kt           │
│  ui/{screen}/UiState.kt             │
│  ui/theme/                          │
└──────────────┬───────────────────────┘
               │ depends on
┌──────────────▼───────────────────────┐
│          Domain Layer                │  ← Pure Kotlin (no Android)
│  domain/model/                      │
│  domain/repository/ (interfaces)    │
│  domain/usecase/                    │
│  domain/util/                       │
└──────────────┬───────────────────────┘
               │ implements
┌──────────────▼───────────────────────┐
│           Data Layer                 │  ← Android-specific
│  data/repository/                   │
│  data/util/                         │
└──────────────────────────────────────┘
               │ wired by
┌──────────────▼───────────────────────┐
│       DI Layer (Hilt)                │
│  di/AppModule.kt                    │
│  di/RepositoryModule.kt             │
│  di/CoilModule.kt                   │
└──────────────────────────────────────┘
```

### Dependency Rules

- **UI → Domain**: ✅ ViewModels inject use cases and domain models only
- **Domain → Data**: ❌ Never. Domain defines interfaces; Data implements them
- **Data → Domain**: ✅ Implements domain interfaces, maps to domain models
- **UI → Data**: ❌ Never. UI never imports data layer classes directly
- **Hilt wiring**: Data implementations bound to Domain interfaces in `RepositoryModule`

### Circular Dependency Check

No circular dependencies exist because:
1. The domain layer has no dependencies on data or UI
2. The data layer only depends on domain interfaces/models
3. The UI layer only depends on domain use cases/models

---

## 5. Data Architecture

### Domain Model Structure

```
Photo  ──────────────────────────► MediaType (IMAGE | VIDEO)
  │
  ├── used in ──► PhotoGroup      (similar photo cluster, selection state)
  ├── used in ──► DuplicateGroup  (duplicate detection result + score)
  ├── used in ──► MemoryGroup     (same-day memories across years)
  └── used in ──► MonthSection    (chronological grouping)
                        │
                        └── aggregated in ──► GroupedPhotosData
```

### Data Access Pattern

```
MediaStore (Android OS)
    │
    ▼
MediaStoreRepository  ──► [Dispatchers.IO]  ──► ContentResolver queries
    │                                               (images + videos)
    │
    ├──► [Dispatchers.Default]  ──► PerceptualHashGenerator
    │                                   (32×32 DCT pHash per photo)
    │
    └──► Group by pHash similarity ──► PhotoGroup / DuplicateGroup
```

### Caching Strategy

| Cache | Location | Size | Contents |
|---|---|---|---|
| Image thumbnails | Memory (Coil) | 30% of app RAM | Decoded bitmaps |
| Image disk cache | Disk (Coil) | 1 GB | Compressed image data |
| pHash cache | In-memory (session) | Unbounded | `Map<Long, Long>` (photoId → hash) |
| Video frames | Coil video decoder | Shared with image cache | First-frame bitmaps |

### Data Validation

- **Photo existence**: `getPhotoById()` returns `null` if photo no longer exists in MediaStore
- **Permission check**: Repository access is gated behind runtime permission in `PermissionGatedGalleryScreen`
- **Delete safety**: Android 13+ deletion uses `MediaStore.createDeleteRequest()` requiring user confirmation before removal

---

## 6. Cross-Cutting Concerns

### 6.1 Authentication & Authorization

The app uses **Android runtime permissions** as its security model:

```
Android 13+ (API 33+):
  READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, DELETE_PHOTOS

Android 12 and below (API ≤ 32):
  READ_EXTERNAL_STORAGE (maxSdkVersion="32")
  WRITE_EXTERNAL_STORAGE (maxSdkVersion="32")
```

Permission flow:
```
MainActivity.onCreate()
    │
    └── LaunchedEffect ──► requestMultiplePermissions (API 33+)
                           requestPermission (API < 33)
                               │
              ┌────────────────┴────────────────┐
              ▼ granted                         ▼ denied
    RewindPhotosApp(hasPermission=true)   PermissionGatedGalleryScreen
    (bottom nav visible)                  (request rationale shown)
```

`PermissionUtils.kt` centralizes version-specific permission logic:
```kotlin
fun getRequiredMediaPermissions(): List<String>   // API-aware
fun getPermissionRationale(): String              // User-friendly message
fun shouldShowPermissionRationale(activity): Boolean
```

### 6.2 Error Handling & Resilience

| Scenario | Handling |
|---|---|
| MediaStore access failure | Caught; ViewModel emits `UiState.Error(message)` |
| Photo deletion SecurityException | `RecoverableSecurityException` handled; triggers Android 13+ intent flow |
| Missing photo (null from MediaStore) | `getPhotoById()` returns `null`; callers handle gracefully |
| Hash computation failure | Logged; photo skipped in similarity grouping |
| Empty gallery | Dedicated `UiState.Empty` state shown to user |

### 6.3 Logging & Monitoring

- Android `Log` (via `android.util.Log`) used for debug output in `MediaStoreRepository`
- Comprehensive logging tags for MediaStore queries and deletion operations
- No crash reporting or analytics SDK (privacy-first design)

### 6.4 Validation

| Validation Type | Location | Implementation |
|---|---|---|
| Permission validation | `PermissionViewModel` + `MainActivity` | State machine + `ActivityResultLauncher` |
| Input validation (navigation args) | `GalleryNavigation.kt` | `navArgument` type constraints + nullable checks |
| Business rule (similarity threshold) | `PhotoRepository.groupPhotosBySimilarity()` | Default 90.0; passed through to pHash comparator |
| Photo existence | `PhotoRepositoryImpl` | Returns `null` on miss; callers must handle |

### 6.5 Configuration Management

| Config | Location | Strategy |
|---|---|---|
| Build variants | `app/build.gradle.kts` | `debug` / `release` |
| Similarity threshold | `PhotoRepository` default parameter | Compile-time default (90.0%), overridable per call |
| Image cache sizes | `CoilModule.kt` | Runtime constants (30% memory, 1 GB disk) |
| Coroutine dispatchers | `AppModule.kt` | Injected via `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher` |
| Android permissions | `AndroidManifest.xml` + `PermissionUtils` | Centralized version detection |

---

## 7. Service Communication Patterns

### 7.1 Synchronous vs. Asynchronous Communication

| Operation | Pattern | Dispatcher |
|---|---|---|
| MediaStore queries | `Flow<T>` (cold stream) | `Dispatchers.IO` |
| pHash calculation | `suspend fun` + `Flow` | `Dispatchers.Default` |
| Photo deletion | `suspend fun` | `Dispatchers.IO` |
| UI state updates | `StateFlow<T>` | Main thread (via collect) |
| Delete request (API 30+) | `IntentSender` callback | Main thread |

### 7.2 Data Flow Pattern

```
[MediaStore] ──► Flow<List<Photo>> ──► UseCase ──► ViewModel StateFlow ──► Compose UI
                 (cold, on IO)          (maps)      (hot, on Main)        (collects)
```

### 7.3 ViewModel ↔ Screen Communication

All screens observe ViewModel state reactively:

```kotlin
// In Composable
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ViewModel exposes
val uiState: StateFlow<RewindUiState> = _uiState.asStateFlow()
```

User actions call ViewModel methods directly (no event/command bus):
```kotlin
// Screen calls:
viewModel.togglePhotoSelection(photoId)
viewModel.onYearSelected(year)
viewModel.deleteSelectedPhotos()
```

### 7.4 Navigation Communication

Screens communicate navigation intent via lambdas passed as composable parameters:
```kotlin
RewindScreen(
    onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
    onPhotoClick = { photo -> navController.navigate(...) }
)
```

---

## 8. Technology-Specific Patterns

### 8.1 Android Architectural Patterns

#### Application Bootstrap
```
RewindPhotosApplication (@HiltAndroidApp)
    └── Hilt component graph initialized at app start

MainActivity (@AndroidEntryPoint)
    └── setContent { RewindPhotosTheme { RewindPhotosApp(...) } }
    └── Permission handling via ActivityResultLauncher
    └── DisposableEffect for lifecycle-based permission recheck
```

#### MediaStore Integration
- `ContentResolver.query()` with projection arrays for performance
- URI-based access: `MediaStore.Images.Media.EXTERNAL_CONTENT_URI`
- Sorting: `MediaStore.Images.Media.DATE_TAKEN DESC`
- Android version branching: API 29+ uses `MediaStore.Images.Media.RELATIVE_PATH`

#### Coroutine Integration Pattern
```kotlin
// In ViewModel
viewModelScope.launch {
    useCase.invoke()
        .collect { result ->
            _uiState.value = RewindUiState.Success(result)
        }
}
```

#### Jetpack Compose Patterns

**State hoisting:** All mutable state is in ViewModels, not composable functions.

**Preview convention:**
```kotlin
@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    RewindPhotosTheme {
        Surface { ScreenComposable(/* mock data */) }
    }
}
```

**Modifier threading:** All composables accept `modifier: Modifier = Modifier` as the first optional parameter.

**LazyList for performance:**
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    contentPadding = PaddingValues(4.dp)
) {
    items(photos) { photo -> PhotoCard(photo) }
}
```

#### Hilt DI Patterns

| Annotation | Usage Location | Scope |
|---|---|---|
| `@HiltAndroidApp` | `RewindPhotosApplication` | App lifetime |
| `@AndroidEntryPoint` | `MainActivity` | Activity lifetime |
| `@HiltViewModel` | All ViewModels | ViewModel lifetime |
| `@Singleton` | Repositories, dispatchers, Coil | App lifetime |
| `@Binds` | Interface → Implementation binding | `RepositoryModule` |
| `@Provides` | Instance provision (dispatchers, Coil) | `AppModule`, `CoilModule` |

Custom qualifier annotations for dispatchers:
```kotlin
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher
```

### 8.2 Navigation Compose Patterns

Route constants are co-located with their screen in `{screen}/Navigation.kt`:
```kotlin
const val GALLERY_ROUTE = "gallery"
const val PHOTO_DETAIL_ROUTE = "photo_detail"
const val PHOTO_URI_ARG = "photo_uri"
```

NavGraphBuilder extension functions encapsulate screen registration:
```kotlin
fun NavGraphBuilder.photoDetailScreen(navController: NavController) {
    composable(
        route = "$PHOTO_DETAIL_ROUTE?$PHOTO_URI_ARG={$PHOTO_URI_ARG}",
        arguments = listOf(navArgument(PHOTO_URI_ARG) { type = NavType.StringType })
    ) { /* ... */ }
}
```

### 8.3 Coil Image Loading Pattern

Custom `ImageLoader` configured in `CoilModule`:
```kotlin
ImageLoader.Builder(context)
    .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.30).build() }
    .diskCache { DiskCache.Builder().maxSizeBytes(1024L * 1024 * 1024).build() }
    .components { add(VideoFrameDecoder.Factory()) }
    .crossfade(true)
    .build()
```

Usage in Composables:
```kotlin
AsyncImage(
    model = photo.uri,
    contentDescription = null,
    imageLoader = LocalContext.current.imageLoader,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
)
```

---

## 9. Implementation Patterns

### 9.1 Use Case Pattern

```kotlin
class GetAllPhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    operator fun invoke(): Flow<List<Photo>> = photoRepository.getAllPhotos()
}
```

Key characteristics:
- `@Inject constructor` for Hilt injection
- Single `invoke()` operator — callable as a function
- Returns `Flow<T>` for streaming data, `suspend fun` for one-shot operations
- No business logic beyond orchestrating repository calls (unless filtering/mapping is needed)

### 9.2 ViewModel Pattern

```kotlin
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getAllPhotosUseCase: GetAllPhotosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            getAllPhotosUseCase()
                .catch { e -> _uiState.value = GalleryUiState.Error(e.message ?: "Unknown error") }
                .collect { photos ->
                    _uiState.value = if (photos.isEmpty()) GalleryUiState.Empty
                                     else GalleryUiState.Success(groupByMonth(photos))
                }
        }
    }
}
```

### 9.3 Repository Implementation Pattern

```kotlin
class PhotoRepositoryImpl @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : PhotoRepository {

    override fun getAllPhotos(): Flow<List<Photo>> =
        mediaStoreRepository.getAllPhotos()

    override suspend fun deletePhoto(photoId: Long): Boolean =
        mediaStoreRepository.deletePhoto(photoId)
}
```

This adapter layer enables:
- Swapping `MediaStoreRepository` for a test fake
- Adding caching or transformation without touching domain logic

### 9.4 Screen Composable Pattern

```kotlin
@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    hasPermission: Boolean,
    onPhotoClick: (Photo) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is GalleryUiState.Loading -> LoadingIndicator()
        is GalleryUiState.Empty   -> EmptyState()
        is GalleryUiState.Error   -> ErrorState((uiState as GalleryUiState.Error).message)
        is GalleryUiState.Success -> PhotoGrid(
            data = (uiState as GalleryUiState.Success).groupedPhotosData,
            onPhotoClick = onPhotoClick
        )
    }
}
```

### 9.5 UI State Sealed Class Pattern

```kotlin
sealed class ScreenUiState {
    data object Loading : ScreenUiState()
    data object Empty : ScreenUiState()
    data class Success(val data: DomainModel) : ScreenUiState()
    data class Error(val message: String) : ScreenUiState()
}
```

This pattern is consistent across **all** screens — always four states: `Loading`, `Empty`, `Success`, `Error`.

---

## 10. Testing Architecture

### Current Test Files

| File | Type | Location |
|---|---|---|
| `ExampleUnitTest.kt` | Unit test (placeholder) | `test/` |
| `ExampleInstrumentedTest.kt` | Instrumented test (placeholder) | `androidTest/` |

### Recommended Testing Strategy (Aligned with Architecture)

| Layer | Test Type | Approach |
|---|---|---|
| **Domain — Use Cases** | Unit test | Mock `PhotoRepository` interface; inject fake; test business logic |
| **Domain — Models** | Unit test | Test `PhotoGroup.togglePhotoSelection()`, `DuplicateGroup.similarityPercentage` |
| **Data — pHash Utils** | Unit test | Provide synthetic bitmaps; verify hash output and similarity scores |
| **Data — Repositories** | Integration test | Mock `ContentResolver`; verify queries and transformations |
| **UI — ViewModels** | Unit test | Use `TestCoroutineDispatcher`; verify state transitions |
| **UI — Composables** | Compose UI test | `ComposeTestRule`; verify rendering for each `UiState` variant |

### Test Double Strategy

```kotlin
// Fake repository for use case testing
class FakePhotoRepository : PhotoRepository {
    var photosToReturn: List<Photo> = emptyList()
    override fun getAllPhotos() = flowOf(photosToReturn)
    override suspend fun getPhotoById(id: Long) = photosToReturn.find { it.id == id }
    // ...
}
```

### Testing Infrastructure

- `junit:junit:4.13.2` — unit tests
- `androidx.test.ext:junit:1.1.5` — AndroidX JUnit
- `androidx.test.espresso:espresso-core:3.5.1` — UI automation
- `androidx.compose.ui:ui-test-junit4` — Compose UI tests

---

## 11. Deployment Architecture

### Build Configuration

```
app/build.gradle.kts
├── compileSdk: 34
├── minSdk: 26  (Android 8.0+)
├── targetSdk: 34
├── buildTypes
│   ├── debug   (minify: false)
│   └── release (minify: true, ProGuard rules in proguard-rules.pro)
└── compileOptions: Java 17 / Kotlin JVM 17
```

### Build Tools

| Tool | Version | Purpose |
|---|---|---|
| Gradle | 8.x | Build automation |
| Kotlin | 1.9+ | Language |
| KSP | 1.9.x | Annotation processing (Hilt) |
| Compose Compiler | 1.5.14 | Jetpack Compose |

### Gradle Performance Settings (`gradle.properties`)

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.parallel=true
org.gradle.daemon=true
kotlin.daemon.jvm.options="-Xmx2048m"
```

### Signing & Release

- Debug: automatically signed with debug keystore
- Release: ProGuard minification enabled (`proguard-rules.pro`)
- Data extraction and backup rules defined in `res/xml/`

### Environment-Specific Configuration

| Environment | MediaStore | Permissions | Delete Method |
|---|---|---|---|
| API 26–28 | `READ_EXTERNAL_STORAGE` | Legacy permission | File delete |
| API 29–32 | `READ_EXTERNAL_STORAGE` (max 32) | Legacy permission | `ContentResolver.delete()` |
| API 33+ | `READ_MEDIA_IMAGES` + `READ_MEDIA_VIDEO` | Granular media permissions | `MediaStore.createDeleteRequest()` |

---

## 12. Extension and Evolution Patterns

### 12.1 Adding a New Screen

1. Create folder: `ui/{screen_name}/`
2. Create `{Screen}UiState.kt` — sealed class with `Loading | Empty | Success | Error`
3. Create `{Screen}ViewModel.kt` — `@HiltViewModel`, inject required use cases
4. Create `{Screen}Screen.kt` — composable with `hiltViewModel()`, `collectAsStateWithLifecycle()`
5. (Optional) Create `{Screen}Navigation.kt` — route constant + `NavGraphBuilder` extension
6. Register in `RewindPhotosApp.kt` NavHost

### 12.2 Adding a New Feature (Domain-First)

1. **Model** (if needed): Add data class to `domain/model/`
2. **Repository interface** (if needed): Add method to `PhotoRepository` or create new interface in `domain/repository/`
3. **Use case**: Create class in `domain/usecase/` with `invoke()` operator
4. **Data implementation**: Implement in `data/repository/`
5. **Hilt binding**: Add `@Binds` in `RepositoryModule` for any new interface
6. **ViewModel**: Inject use case; add state/method
7. **Screen**: Observe ViewModel state; render UI

### 12.3 Adding External Data Sources (Future)

Follow the **Anti-Corruption Layer** pattern:
```
data/remote/
    ├── api/         ← Retrofit/Ktor interface
    ├── model/       ← Network DTOs
    ├── mapper/      ← DTO ↔ Domain model mappers
    └── {Feature}RemoteDataSource.kt
```

The repository implementation merges local (MediaStore) and remote sources, always returning domain models.

### 12.4 Planned Architecture Extensions (from Architecture.md)

| Stage | Feature | Architectural Impact |
|---|---|---|
| Stage 4 | Offline Maps (MapLibre + GeoJSON) | New `ui/map/` screen + `domain/model/PhotoLocation` enrichment |
| Stage 5 | Biometric Vault | New `data/vault/` + Android Biometric API integration |
| Stage 5 | Room DB caching | New `data/local/` with Room DAO layer beneath repositories |
| Stage 5 | WorkManager notifications | New `data/worker/` with `@HiltWorker` classes |
| Stage 6 | ML Kit face clustering | New `:ml-module` Gradle module (isolated) |
| Stage 6 | On-device CLIP search | New `:ml-module` — TFLite interpreter |

### 12.5 Adapter Pattern for New Data Sources

```kotlin
// New interface in domain/repository/
interface MapTileRepository {
    fun getTilesForRegion(bounds: LatLngBounds): Flow<List<MapTile>>
}

// Binding in RepositoryModule
@Binds @Singleton
abstract fun bindMapTileRepository(impl: LocalMapTileRepository): MapTileRepository
```

---

## 13. Architectural Decision Records

### ADR-001: Clean Architecture

**Context:** Need clear separation of business logic from Android framework for testability and maintainability.  
**Decision:** Adopt Clean Architecture with three layers: Domain (pure Kotlin), Data (Android-aware), UI (Compose + ViewModels).  
**Consequences (+):** Business logic independently testable; can swap MediaStore for Room/remote without touching domain.  
**Consequences (−):** More boilerplate; simple operations require interface + use case + implementation.

---

### ADR-002: Hilt for Dependency Injection

**Context:** Need compile-time-safe DI with minimal boilerplate on Android.  
**Decision:** Use Hilt (Dagger 2 under the hood) with `@HiltViewModel`, `@Singleton`, `@Binds`.  
**Alternatives considered:** Manual DI (too fragile), Koin (runtime errors).  
**Consequences (+):** Compile-time verification; IDE navigation support; test substitution via `@TestInstallIn`.  
**Consequences (−):** Build time cost due to Kapt/KSP annotation processing.

---

### ADR-003: Kotlin Flow + StateFlow for Reactive State

**Context:** Need lifecycle-safe reactive UI updates without LiveData boilerplate.  
**Decision:** `StateFlow<UiState>` in ViewModels; `collectAsStateWithLifecycle()` in Composables.  
**Alternatives considered:** LiveData (less composable-friendly), RxJava (heavy dependency).  
**Consequences (+):** Kotlin-native; coroutine-friendly; automatic lifecycle cancellation.  
**Consequences (−):** StateFlow requires initial value; cold Flow vs. hot StateFlow distinction must be understood by developers.

---

### ADR-004: Perceptual Hashing for Duplicate Detection

**Context:** Need to detect visually similar photos (not just byte-identical) including photos saved at different resolutions or after minor edits.  
**Decision:** 32×32 DCT-based pHash with 64-bit hash and Hamming distance comparison. Default threshold: 90% similarity.  
**Alternatives considered:** MD5/SHA (exact match only), SSIM (too slow for mobile), cloud ML (privacy concern).  
**Consequences (+):** On-device, privacy-preserving; tolerant of JPEG compression artifacts and minor edits.  
**Consequences (−):** CPU intensive; requires `Dispatchers.Default` offloading; hash cache needed for repeated comparisons.

---

### ADR-005: Coil for Image Loading

**Context:** Need efficient thumbnail loading for large photo grids with video support.  
**Decision:** Coil 2.7.0 with custom ImageLoader (30% memory cache, 1 GB disk cache, VideoFrameDecoder).  
**Alternatives considered:** Glide (Java-first API), Picasso (no video support).  
**Consequences (+):** Kotlin-first API; Compose-native `AsyncImage`; coroutine integration.  
**Consequences (−):** Version pinning required for video frame decoder compatibility.

---

### ADR-006: Navigation Compose (String Routes)

**Context:** Need in-app navigation for Compose screens.  
**Decision:** Navigation Compose with string-based routes and `NavGraphBuilder` extension functions per screen.  
**Future evolution note:** Architecture.md specifies Type-Safe Navigation (Navigation Compose 2.8.0+) as the target.  
**Consequences (+):** Works today; encapsulates route concerns per screen.  
**Consequences (−):** String routes are not type-safe; argument passing via URI encoding is error-prone.  
**Migration path:** Replace string routes with `@Serializable` data class destinations.

---

### ADR-007: Privacy-First (All On-Device)

**Context:** Users are sharing sensitive personal photos; privacy is paramount.  
**Decision:** Zero network calls for core functionality; all photo analysis (pHash, memories, grouping) runs on-device.  
**Consequences (+):** No privacy leaks; works offline; no API keys or cloud costs.  
**Consequences (−):** AI features (ML Kit, CLIP) require more sophisticated on-device models; no sync across devices.

---

## 14. Architecture Governance

### Structural Enforcement

| Rule | Enforcement Mechanism |
|---|---|
| Domain has no Android imports | Package structure + code review |
| UI never imports `data.*` | Hilt DI graph — only domain interfaces are injected into ViewModels |
| All screens use `collectAsStateWithLifecycle()` | Code review checklist |
| All ViewModels are `@HiltViewModel` | Hilt compile-time check |
| StateFlow exposed as `asStateFlow()` | Code review (no mutable state leakage) |

### Naming Conventions

| Component | Convention | Example |
|---|---|---|
| Composable | PascalCase | `GalleryScreen`, `PhotoCard` |
| ViewModel | `{Screen}ViewModel` | `GalleryViewModel` |
| Use Case | `{Verb}{Noun}UseCase` | `GetAllPhotosUseCase` |
| Repository Interface | `{Entity}Repository` | `PhotoRepository` |
| Repository Impl | `{Strategy}{Entity}Repository` | `MediaStoreRepository` |
| UI State | `{Screen}UiState` | `GalleryUiState` |
| Route constant | `{SCREEN}_ROUTE` | `GALLERY_ROUTE` |
| DI Qualifier | `@{Qualifier}Dispatcher` | `@IoDispatcher` |

### Code Review Checklist for New Features

- [ ] New domain model is in `domain/model/`
- [ ] New use case is in `domain/usecase/` with `invoke()` operator
- [ ] New repository interface is in `domain/repository/`
- [ ] Data implementation has no UI imports
- [ ] ViewModel uses `viewModelScope.launch` with `.catch {}` for error handling
- [ ] UI state is a sealed class with `Loading | Empty | Success | Error`
- [ ] Screen uses `collectAsStateWithLifecycle()`, not `collectAsState()`
- [ ] Composable has `modifier: Modifier = Modifier` as first optional parameter
- [ ] New screen includes `@Preview` composable

---

## 15. Blueprint for New Development

### 15.1 Development Workflow by Feature Type

#### Adding a New Photo Feature (e.g., "Favorites")

```
1. domain/model/FavoritePhoto.kt          — Add domain model
2. domain/repository/FavoriteRepository.kt — Define interface
3. domain/usecase/ToggleFavoriteUseCase.kt — Implement use case
4. data/repository/LocalFavoriteRepository.kt — Implement (Room or SharedPreferences)
5. di/RepositoryModule.kt                 — Add @Binds binding
6. ui/favorites/FavoritesUiState.kt       — Define UI states
7. ui/favorites/FavoritesViewModel.kt     — Wire use case → StateFlow
8. ui/favorites/FavoritesScreen.kt        — Build Compose UI
9. RewindPhotosApp.kt                     — Add route to NavHost
```

#### Adding a New Utility (e.g., "Export Photos")

```
1. domain/usecase/ExportPhotosUseCase.kt  — Orchestrate export logic
2. data/util/ExportUtils.kt               — File I/O implementation
3. ui/{screen}/ViewModel.kt               — Add export action + progress state
4. ui/{screen}/Screen.kt                  — Add export button + progress dialog
```

### 15.2 Implementation Templates

#### Use Case Template

```kotlin
class YourFeatureUseCase @Inject constructor(
    private val repository: PhotoRepository,           // or other domain interfaces
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(/* params */): Flow<Result<YourDomainModel>> =
        repository.yourRepositoryMethod()
            .map { data -> /* transform */ }
            .flowOn(ioDispatcher)
            .catch { e -> emit(Result.failure(e)) }
}
```

#### ViewModel Template

```kotlin
@HiltViewModel
class YourFeatureViewModel @Inject constructor(
    private val yourUseCase: YourFeatureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<YourFeatureUiState>(YourFeatureUiState.Loading)
    val uiState: StateFlow<YourFeatureUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            yourUseCase()
                .catch { e -> _uiState.value = YourFeatureUiState.Error(e.message ?: "") }
                .collect { result ->
                    _uiState.value = if (result.isEmpty()) YourFeatureUiState.Empty
                                     else YourFeatureUiState.Success(result)
                }
        }
    }

    fun onUserAction(/* params */) {
        viewModelScope.launch { /* handle action */ }
    }
}
```

#### Composable Screen Template

```kotlin
@Composable
fun YourFeatureScreen(
    modifier: Modifier = Modifier,
    onNavigateTo: (destination: String) -> Unit = {},
    viewModel: YourFeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is YourFeatureUiState.Loading -> LoadingContent(modifier)
        is YourFeatureUiState.Empty   -> EmptyContent(modifier)
        is YourFeatureUiState.Error   -> ErrorContent(state.message, modifier)
        is YourFeatureUiState.Success -> YourFeatureContent(state.data, modifier)
    }
}

@Preview(showBackground = true)
@Composable
private fun YourFeatureScreenPreview() {
    RewindPhotosTheme {
        Surface {
            YourFeatureScreen()
        }
    }
}
```

#### UI State Template

```kotlin
sealed class YourFeatureUiState {
    data object Loading : YourFeatureUiState()
    data object Empty : YourFeatureUiState()
    data class Success(val data: YourDomainModel) : YourFeatureUiState()
    data class Error(val message: String) : YourFeatureUiState()
}
```

### 15.3 File Organization for New Screens

```
ui/
└── yourfeature/
    ├── YourFeatureScreen.kt         ← Composable UI
    ├── YourFeatureViewModel.kt      ← State + business logic bridge
    ├── YourFeatureUiState.kt        ← UI state sealed class
    └── YourFeatureNavigation.kt     ← Route constants + NavGraphBuilder extension (if needed)
```

### 15.4 Common Pitfalls to Avoid

| Pitfall | Correct Approach |
|---|---|
| Importing `data.*` in ViewModel | Only import domain interfaces and models |
| Using `collectAsState()` | Use `collectAsStateWithLifecycle()` to respect lifecycle |
| Storing `MutableStateFlow` as `val uiState` | Expose as `asStateFlow()` to prevent external mutation |
| Calling `MediaStore` directly from ViewModel | Route through use case → repository interface |
| Hardcoding strings in Compose | Use `stringResource(R.string.your_string)` |
| Skipping `@Preview` on composables | Add preview for every new composable |
| Ignoring `Empty` state | Always handle the empty collection case explicitly |
| Long-running work on Main dispatcher | Use `@IoDispatcher` for I/O, `@DefaultDispatcher` for CPU |
| Catching all exceptions silently | Emit `Error` state; let user retry |
| Sharing `MutableStateFlow` between ViewModels | Each ViewModel owns its own state; communicate via shared use cases |

---

*This blueprint was generated on 2026-03-08 by analyzing the RewindPhotos codebase at commit HEAD.*  
*Keep this document updated when new architectural patterns are introduced or layers are restructured.*  
*Major updates are recommended when: new Gradle modules are added, the navigation strategy changes to Type-Safe Navigation, Room DB is introduced (Stage 5), or the `:ml-module` is extracted (Stage 6).*
