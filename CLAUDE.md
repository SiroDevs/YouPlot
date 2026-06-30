# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

YouPlot is an Android adventure planning app built with modern Kotlin, Compose, and architecture best practices. It allows users to plot routes on maps, create activity plans around those routes, and track their progress during activities. The app uses offline-first architecture with Room database and integrates with OpenStreetMap services.

## Build & Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build and install on connected device
./gradlew installDebug
./gradlew installRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run tests for a specific module (e.g., feature:route)
./gradlew :feature:route:test
```

### Linting & Code Quality
```bash
# Lint check
./gradlew lint

# Build everything with checks
./gradlew build
```

### Other Useful Commands
```bash
# Clean build artifacts
./gradlew clean

# Sync Gradle dependencies
./gradlew syncLibraries

# Check Gradle dependency tree
./gradlew dependencies
```

## Architecture Overview

### Multi-Module Structure (Clean Architecture)
The project follows **modular architecture with clear separation of concerns**:

**Core Modules** (reusable, no feature-specific logic):
- `:core:common` – Shared entities, enums, utilities, constants, and helpers (no dependencies on other modules)
- `:core:domain` – Domain entities and use cases (business logic, independent of Android)
- `:core:database` – Room database, DAOs, and database entities with converters
- `:core:data` – Repository implementations, dependency injection bindings, preference management
- `:core:designsystem` – Material3 theme, colors, typography, and theme selector dialog
- `:core:ui` – Reusable UI components (maps, dialogs, state indicators, data components, timelines)

**Feature Modules** (independent, can be tested/built separately):
- `:feature:dashboard` – Main dashboard with recent routes and plans overview
- `:feature:route` – Route creation (plotter), route list, and route detail views
- `:feature:plan` – Plan creation/editing, plan list, and plan details
- `:feature:tracker` – Activity tracking with real-time location updates
- `:feature:settings` – Theme selection, preferences, and app configuration
- `:feature:extra` – About and help/feedback screens

**App Module** (`:app`):
- Main application shell, navigation graph, theme setup, and Sentry error tracking
- Hilt application setup and WorkManager configuration

### Key Architectural Patterns

**MVVM + StateFlow**:
- Each feature has ViewModels (Hilt-injected) managing UI state via `MutableStateFlow<UiState>`
- Screens receive state as `StateFlow` and emit actions back to ViewModel
- Example: `DashboardViewModel` collects routes and plans using use cases, exposes `state: StateFlow<DashboardUiState>`

**Repository Pattern**:
- Domain defines repository interfaces (e.g., `RouteRepo`, `PlanRepo`)
- Data layer provides implementations injected at singleton scope via Hilt
- Repositories expose `Flow<T>` for reactive updates from Room database

**Use Cases**:
- Domain layer contains use cases (e.g., `GetAllRoutesUseCase`, `SaveRouteUseCase`)
- Use cases are injected into ViewModels and called directly or via Flow

**Dependency Injection (Hilt)**:
- `@HiltAndroidApp` in `YouPlotApp`
- `@AndroidEntryPoint` on Activities and Fragments (not used in this compose-only app)
- `@HiltViewModel` for ViewModels with use case and repo injection
- `DataModule` binds repository implementations to interfaces in singleton scope
- Kotlin Serialization / Parcelize for entity serialization

**Navigation**:
- Compose Navigation with `NavHost` in `AppNavHost.kt`
- Routes defined in `core:common` as string constants with helper functions (e.g., `Routes.routeDetail(routeId)`)
- Hilt + Compose Navigation integration for ViewModel injection in composables

**Database**:
- Room database with 5 entities: `RouteEntity`, `WaypointEntity`, `PlanEntity`, `PlanEventEntity`, `SessionEntity`
- Converters transform between database entities and domain models
- DAOs use Flow for reactive queries

**State Management**:
- Screens expose composable `UiState` data classes holding all UI data
- State updates via `_state.update { copy(...) }`
- Screens derive UI from state without imperative callbacks (except for navigation)

### Build Configuration

**Convention Plugins** (in `:build-logic`):
- `you.plot.android.library` – Base library setup (compileSdk=37, minSdk=26, JVM11)
- `you.plot.android.library.compose` – Library + Compose enablement
- `you.plot.android.feature` – Feature module setup (auto-applies compose, hilt, adds standard dependencies)
- `you.plot.hilt` – Adds Hilt and KSP configuration

**Key Dependencies** (see `gradle/libs.versions.toml`):
- Compose: Material3, Navigation Compose, Activity Compose, Foundation
- Persistence: Room 2.8.4
- DI: Hilt 2.59.2
- Maps: OSMDroid 6.1.20 (offline maps)
- Networking: Ktor 3.5.0, Play Services Location 21.3.0
- Error Tracking: Sentry 6.13.0
- Kotlin: 2.3.21, AGP 9.2.1

## Code Organization Guidelines

### Feature Module Structure
Each feature module follows this pattern:
```
feature/dashboard/
  ├── viewmodel/         # StateFlow-based ViewModels
  ├── utils/             # UI state, enums, utilities
  ├── view/
  │   ├── screen/        # Full-screen composables
  │   └── components/    # Reusable UI components
```

### Package Naming
- **Views/Screens**: `com.you.plot.feature.{module}.{submodule}.view.screen`
- **Components**: `com.you.plot.feature.{module}.{submodule}.view.components`
- **ViewModels**: `com.you.plot.feature.{module}.{submodule}.viewmodel`
- **Utils/State**: `com.you.plot.feature.{module}.{submodule}.utils`

### Common Utilities
- `AppConstants` – App metadata, map settings, route constants, notification settings
- `MapConstants` – Map colors, tile server URLs, zoom levels
- `Routes` – Navigation routes and helper functions for building route strings
- `CountryUtils`, `RouteUtils`, `AppUtils` – Reusable business logic

## Key Data Flows

### Route Creation Flow
1. User opens plotter (`PlotterScreen`) → `PlotterViewModel` manages stages (start point → destination → waypoints → route suggestions)
2. User selects a route → `SaveRouteUseCase` saves to database
3. Dashboard fetches routes via `GetAllRoutesUseCase` → updates `DashboardViewModel.state`
4. Route list and detail screens subscribe to `RouteRepo.getAllRoutes()` Flow

### Plan Creation Flow
1. User creates plan from route → `PlannerScreen` with `PlannerViewModel`
2. Plan saved via `SavePlanUseCase` → stored in Room
3. Plan events created during creation → persisted in `PlanEventEntity`
4. Dashboard and plan list refresh automatically via Flow subscriptions

### Tracking Flow
1. `TrackerScreen` displays plan with current waypoint tracking
2. Location updates via `LocationRepoImpl` using Play Services Fused Location
3. Session entity records progress → stored in Room
4. Real-time updates via `SessionRepoImpl` Flow

## Important Notes

- **No Test Suite**: This project currently has no unit or instrumentation tests. When adding features, consider test-driven development.
- **Android 26+ Target**: Min SDK is 26; use AndroidX and modern APIs.
- **Build Config**: Sentry integration configured; requires `futuristicken` org settings.
- **Local Properties**: Build requires `local.properties` with `OSM_USER_AGENT` and `PAYSTACK_SECRET_KEY` for map requests and payments.
- **Navigation**: Always use `Routes` helper functions (e.g., `Routes.routeDetail(id)`) rather than building route strings manually.
- **StateFlow Pattern**: Prefer `MutableStateFlow` with `.update()` over `.value =` for thread safety.
- **Database Versioning**: Room schema is at version 1; migrations not yet implemented.

## Contributing

- Branch from `stable` branch
- Limit PRs to <10 files
- Follow existing MVVM + Compose patterns
- Use Hilt for all dependency injection
- Leverage use cases for business logic access
- Test locally on Android 26+ devices

## Instructions
- fix the following bugs
  - in route plotting, app crashes in stage 3 and stage 4 sometimes when I manage to navigate to stage 4
  - in planning when I tap "generate schedule" the app just crashes immediately
- write unit tests and intergration tests,
- write previews for all screens: dashboard, route screens and stages, plan screens and steps, tracker screen, settings