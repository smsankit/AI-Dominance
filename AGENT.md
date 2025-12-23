Project Agent Notes

This project is scaffolded with MVVM + Clean Architecture using Jetpack Compose, Navigation Compose, Hilt, Retrofit+Gson, Coil, Coroutines/Flow, and DataStore.

Structure overview:
- core/: cross-cutting concerns (di, navigation, network, exception, validation, util, datastore)
- domain/: business models, repository interfaces, use cases
- data/: DTOs, mappers, repository implementations, local/remote sources
- presentation/: Compose UI, ViewModels, UI state and events

Build & Run:
- Android Gradle Plugin 8.9.0, Kotlin 2.0.21, Compose BOM 2024.09
- Hilt for DI with @HiltAndroidApp in LoggerApp

Conventions:
- Keep UI state immutable and exposed as StateFlow
- Repositories return Result/Flow<Result<>> as appropriate
- Map DTOs to domain models explicitly
- Add new features under feature-first subpackages across layers

