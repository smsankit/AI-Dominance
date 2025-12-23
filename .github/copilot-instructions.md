# GitHub Copilot Project Instructions

This project follows MVVM with Clean Architecture using Jetpack Compose, Navigation Compose, Hilt, Retrofit+Gson, Coil, Coroutines/Flow, and DataStore.

Guidelines:
- Prefer feature-first structure under `presentation/`, `domain/`, and `data/`.
- Use `core/` for cross-cutting concerns: di, network, exception, validation, util, navigation, and datastore.
- Inject dependencies with Hilt. No service locators.
- Use Kotlin coroutines and Flow for async/reactive patterns.
- Keep UI state as immutable data classes, expose as StateFlow.
- Do not access `data` layer from `presentation` directly; go through `domain` (use cases).
- Use Repository interfaces in `domain`; implementations in `data`.
- Use Result wrappers and map DTOs to domain models explicitly.
- Compose screens are dumb; business logic stays in ViewModels/use cases.
- Add preview parameters and stable models for previews when possible.

