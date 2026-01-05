# GitHub Copilot Project Instructions

This project follows MVVM with Clean Architecture using Jetpack Compose, Navigation Compose, Hilt, Retrofit+Gson, Coil, Coroutines/Flow, and DataStore.

Guidelines:
- Prefer feature-first structure under `presentation/`, `domain/`, and `data/`.
- Use `core/` for cross-cutting concerns: di, network, exception, validation, util, and datastore.
- Inject dependencies with Hilt. No service locators.
- Use Kotlin coroutines and Flow for async/reactive patterns.
- Keep UI state as immutable data classes, expose as StateFlow.
- Do not access `data` layer from `presentation` directly; go through `domain` (use cases).
- Use Repository interfaces in `domain`; implementations in `data`.
- Use Result wrappers and map DTOs to domain models explicitly.
- Compose screens are dumb; business logic stays in ViewModels/use cases.
- Add preview parameters and stable models for previews when possible.

- Create all composable screens under `presentation/feature_name/` and name should end with `Screen`.
- ViewModels should be in `presentation/feature_name/` and named with `ViewModel` suffix.
- Use `presentation/navigation/` for navigation setup and routes.
- Use `data/mapper/` for mappers.
- Use `data/remote/api` for remote data sources.
- Use `data/remote/dto` for remote DTOs.
- Use `data/repository/` for repository implementations.
- Use `domain/model/` for business models.
- Use `domain/repository/` for repository interfaces.
- Use `domain/usecase/` for use cases.
- Models in the `data/remote/dto` should end with `Dto` suffix.
- Models in the `domain/model` should end with `Data` suffix.
- Models in the `presentation/feature_name/model/` should end with `UiModel` suffix.
- Create common components in `presentation/common/` for reuse across features.
- Use `presentation/feature_name/mapper` for mapping between domain models and UI models.
- Create common  components for Buttons, TextFields, etc.
- User define colors, typography, and themes in `presentation/theme/`.
- Any colors used in the app should be defined in the `presentation/theme/Color.kt` files.
- Any string used in the app should be defined in the `res/values/strings.xml` file.
- Follow existing coding conventions and styles in the project.

