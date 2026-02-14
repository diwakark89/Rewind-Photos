# Rewind Photos – Development Roadmap

---

## Stage 1: The High-Performance Core

**Goal:** Establish the foundation with a fast, reliable local gallery.

### Features
- **Media Fetching:** Query MediaStore for `DATE_TAKEN` and `DATA` paths.
- **Optimized Grid:** 3-column `LazyVerticalGrid` using Material 3 Cards.
- **Smooth Loading:** Coil integration for memory-efficient thumbnail rendering.
- **Architecture:** Setup Hilt (DI) and Clean Architecture (Data/Domain/UI modules).

### Verification
- **Performance:** 120fps scrolling on high-refresh screens.
- **UI Preview:** `GalleryGridPreview` with Material 3 theming and mock data.

---

## Stage 2: Intelligent Cleanup (The "Utility" Release)

**Goal:** Add value by helping users manage their local storage.

### Features
- **Duplicate Detection:** Implement pHash (Perceptual Hashing) to find near-identical photos locally.
- **Declutter UI:** A "Cleanup" tab grouping similar photos with "Keep Best" logic.
- **Interaction:** "Scale on Press" animations using `animateFloatAsState`.

### Verification
- **Logic Check:** Verify pHash identifies images with >90% visual similarity.
- **UI Preview:** `DuplicateGroupPreview` showing side-by-side comparison cards.

---

## Stage 3: Time Machine & Hero Motion

**Goal:** Deliver the "Rewind" value proposition with premium animations.

### Features
- **Rewind Engine:** `GetMemoriesUseCase` to filter photos by `currentDay` and `currentMonth`.
- **Nostalgia Feed:** Vertical list sectioned by years (e.g., "5 Years Ago").
- **Hero Transitions:** `SharedTransitionLayout` for seamless thumbnail-to-full-screen expansion.

### Verification
- **Logic Check:** Photos from different years on the same date group correctly.
- **UI Preview:** `MemoryFeedPreview` with year headers and empty state illustration.

---

## Stage 4: Spatial Discovery (Offline Maps)

**Goal:** Visualize life journeys without cloud tracking.

### Features
- **Offline Atlas:** Use MapLibre with local GeoJSON map tiles (100% offline).
- **Travel Map UI:** Map view clustering photos as pins based on EXIF GPS data.

### Verification
- **Privacy:** Network Inspector confirms zero calls to Google Maps/external APIs.
- **UI Preview:** `MemoryMapPreview` showing photo clusters on a local map.

---

## Stage 5: Security & Proactive Habits

**Goal:** Secure the data and integrate into the user's daily routine.

### Features
- **Biometric Vault:** Use Android Biometric API; move files to internal `app_private_storage`.
- **Persistence:** Room DB to cache metadata for instant app launches.
- **Automation:** WorkManager for 8:00 AM notifications and a Glance API home widget.

### Verification
- **Security:** Vaulted photos are invisible to system file managers and other gallery apps.
- **UI Preview:** `VaultLockedPreview` showing the biometric unlock state.

---

## Stage 6: Semantic Intelligence (The AI Finale)

**Goal:** On-device search and people identification.

### Features
- **People ID:** Google ML Kit + TFLite (512-vector) face clustering.
- **Semantic Search:** Local CLIP model for object search (e.g., "Cake", "Sunset").
- **Growth Tracking:** "Then & Now" face matching collages.

### Verification
- **AI Accuracy:** 512-vector clustering groups the same person across different lighting.
- **Resource Check:** AI tasks pause when the device is hot or battery is < 20%.

### UI Preview
- `PeopleAlbumPreview` showing circular face avatars.

---

# Standard Industry Practice Check

To ensure **Rewind Photos** remains modular and scalable:

- **Dependency Injection:** Use Hilt to swap the `PhotoRepository` (Mock vs. Real MediaStore).
- **ML Isolation:** All TFLite and ML Kit code must live in a `:ml-module` to keep main `:app` build times fast.
- **State Management:** Use `StateFlow` in ViewModels to handle UI states (`Loading`, `Success`, `Empty`).