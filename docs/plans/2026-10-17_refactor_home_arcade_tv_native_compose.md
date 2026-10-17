# Piano di Lavoro: Refactoring Completo HomeArcade-TV (Pattern StreamingHub)

## Obiettivo
Eseguire il refactoring completo di `HomeArcade-TV`, superando l'architettura legacy a WebView unica per allineare l'applicazione al pattern moderno nativo di `streaming-hub-tv`: interfaccia utente 100% Jetpack Compose for TV (Material 3 for TV), stack Ktor/Kotlinx Serialization, rilevamento automatico LAN, Dynamic Hero Billboard, caroselli orizzontali a scaffale, schede di dettaglio arricchite con supporto multi-disco, gestione profili e riproduzione streaming WebRTC/WASM dedicata.

## Scope & Non-Goals
- **In Scope**:
  - Aggiornamento dipendenze in `libs.versions.toml` e `app/build.gradle.kts` (Ktor, Coil, Serialization, Compose for TV).
  - Tema visivo **Retro Arcade** (Rosa Neon `#EC4899`, Violetto `#8B5CF6`, Sfondo profondo `#080A10`).
  - Componenti TV riutilizzabili: `TvFocusModifier` (1.08x scale, glow border), `TvButton`, `TvGameCard`, `HeroBillboard`, `TvShelfRow`, `TvLoadingSpinner`.
  - Data Layer: DTOs per API di `home-arcade-ha`, `HomeArcadeApi`, `ServerDiscoveryManager` (scansione porte 5000/9876 e subnet), `AppPreferences` (DataStore), `RomRepository`, `ProfileRepository`.
  - Schermate UI TV: `SetupScreen`, `ProfilePickerScreen`, `HomeScreen` (con Top Navigation Bar a tabs), `DetailsScreen` (metadati, multi-disco, preferiti), `SearchScreen`, `PlayerScreen` (schermo intero accelerato hardware per `/api/roms/:id/stream-player`), `SettingsScreen`.
  - Application class `HomeArcadeTvApp` con configurazione cache Coil (150MB disco) e `MainActivity`.
- **Non-Goals (YAGNI)**:
  - Nessuna implementazione di core emulatori locali in C++/NDK su TV (lo streaming WebRTC dell'add-on gestisce l'emulazione a 60 FPS sub-1ms).
  - Nessun sistema di login cloud esterno o server di autenticazione proprietario oltre ai profili locali di HomeArcade.

## Task Checklist
- [x] **Fase 1: Configurazione Build & Dipendenze**
  - [x] Aggiornare `gradle/libs.versions.toml` con Ktor, Coil, Serialization e icone.
  - [x] Aggiornare `app/build.gradle.kts` con plugin di serializzazione e dipendenze.
  - [x] Verificare la sincronizzazione Gradle.
- [x] **Fase 2: Design System & Componenti TV**
  - [x] Creare `Color.kt`, `Theme.kt`, `Type.kt` con la palette Retro Arcade.
  - [x] Creare `TvFocusModifier.kt` con supporto D-pad, scala elastica e bagliore neon.
  - [x] Creare `TvButton.kt` per telecomando TV.
  - [x] Creare `TvGameCard.kt` con aspect ratio copertina, badge console e valutazione.
  - [x] Creare `HeroBillboard.kt` dinamico reattivo al focus.
  - [x] Creare `TvShelfRow.kt` con padding overscan e intestazioni conteggio.
  - [x] Creare `TvLoadingSpinner.kt`.
- [x] **Fase 3: Data Layer, API & Rete**
  - [x] Definire DTOs in `data/model/Dtos.kt` allineati agli endpoint di `home-arcade-ha`.
  - [x] Implementare `ApiClient.kt` e `HomeArcadeApi.kt` (Ktor + OkHttp).
  - [x] Implementare `ServerDiscoveryManager.kt` con scansione automatica LAN e porte 5000/9876.
  - [x] Implementare `AppPreferences.kt` con DataStore Preferences.
  - [x] Implementare `RomRepository.kt` e `ProfileRepository.kt`.
- [x] **Fase 4: Schermate e Navigazione TV**
  - [x] Implementare `SetupScreen.kt` & `SetupViewModel.kt` (Discovery + Manuale).
  - [x] Implementare `ProfilePickerScreen.kt` & `ProfileViewModel.kt`.
  - [x] Implementare `HomeScreen.kt` & `HomeViewModel.kt` (NavBar, Hero, Scaffali).
  - [x] Implementare `DetailsScreen.kt` & `DetailsViewModel.kt` (Metadati, Multi-disco, Preferiti).
  - [x] Implementare `SearchScreen.kt` & `SearchViewModel.kt`.
  - [x] Implementare `PlayerScreen.kt` & `PlayerViewModel.kt` (WebRTC Container + Dialog Uscita).
  - [x] Implementare `SettingsScreen.kt` & `SettingsViewModel.kt`.
  - [x] Configurare `NavRoutes.kt` e `AppNavigation.kt`.
- [x] **Fase 5: Application, Activity & Cleanup**
  - [x] Implementare `HomeArcadeTvApp.kt` con Coil ImageLoaderFactory.
  - [x] Aggiornare `MainActivity.kt` e `AndroidManifest.xml`.
  - [x] Rimuovere il codice legacy obsoleto (`webview/`, schermate as-is).
- [x] **Fase 6: Verifica & Quality Gate**
  - [x] Eseguire `./gradlew assembleDebug` per validare la compilazione.
  - [x] Eseguire `./gradlew test` per verificare la logica e DTO.
  - [x] Aggiornare `docs/HANDOFF.md` e la documentazione del progetto.

## Strategia di Verifica
- **Build**: `./gradlew assembleDebug`
- **Lint**: `./gradlew lintDebug`
- **Compilazione rapida**: `./gradlew compileDebugKotlin`
