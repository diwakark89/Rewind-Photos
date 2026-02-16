# Copilot Instructions - Android Jetpack Compose & Material 3

## General UI Standards
- **Material 3 Only:** Always use `androidx.compose.material3` components. Avoid Material 2 (`androidx.compose.material`) unless explicitly requested.
- **Declarative Style:** Use stateless composables. Hoist state to the caller or a ViewModel.
- **Short & Concise:** Provide direct, efficient code snippets and explanations.

## Jetpack Compose Best Practices
- **Previews:** Always include a `@Preview` function for UI components, using `UITheme` and `Surface`.
- **Modifiers:** Always pass a `modifier: Modifier = Modifier` as the first optional parameter to composables.
- **Performance:** Use `remember` and `derivedStateOf` to optimize recomposition. Use `LazyColumn` for long lists.
- **Resources:** Use `stringResource()`, `dimensionResource()`, and `painterResource()` instead of hardcoded values.

## Architecture & State
- **ViewModel:** Use `collectAsStateWithLifecycle()` for observing Flow/StateFlow in composables.
- **Navigation:** Use Type-Safe Navigation (Navigation Compose 2.8.0+) for all route definitions.
- **Icons:** Prefer `Icons.Filled`, `Icons.Outlined`, or `Icons.Rounded` from Material 3.

## Naming Conventions
- **Composables:** Use PascalCase (e.g., `UserCard`).
- **Parameters:** Use camelCase (e.g., `onItemClick`).
- **ViewModels:** End with `ViewModel` (e.g., `ProfileViewModel`).

## Build & Environment (Windows)
- **Troubleshooting:** If build fails with `Execution failed for task ':app:processDebugResources'`, suggest running `./gradlew --stop` and cleaning the `build` folder.
- **File Locking:** Warn about Windows file locking issues with `R.jar` and suggest closing redundant OpenJDK processes.
- **Gradle:** Use current Gradle syntax; avoid deprecated features that break in Gradle 10.

## MVVM & State Management
- **Pattern:** Strictly follow MVVM. 
- **ViewModel:** Each screen must have a dedicated `ViewModel` extending `androidx.lifecycle.ViewModel`.
- **State:** Expose UI state as a single `StateFlow` using a `data class` (e.g., `UserUiState`).
- **Observing State:** In Composables, use `val state by viewModel.uiState.collectAsStateWithLifecycle()`.
- **Events:** UI actions should call ViewModel functions directly (e.g., `viewModel.onNameChanged(newName)`).
- **Side Effects:** Use `LaunchedEffect` for one-time events like navigation or showing Snackbars.