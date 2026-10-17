# Handoff Project Snapshot: Refactoring HomeArcade-TV (Pattern StreamingHub)

## Stato Corrente
- **Refactoring Completato con Successo**: Architettura monoview WebView legacy rimossa e rimpiazzata al 100% dal pattern nativo Jetpack Compose for TV (Material 3 for TV).
- **Integrazione Breaking Changes `home-arcade-ha`**:
  - DTOs e Ktor Client configurati per il runner WebRTC (`/api/roms/:id/stream-player`), profili multi-utente, salvataggi e metadati multi-sorgente.
  - Scoperta automatica server in LAN su subnet locali (porte 5000 e 9876) con fallback manuale.
- **Componenti TV & Schermate Attive**:
  - `HeroBillboard` reattivo al focus con trailer/background, `TvShelfRow`, `TvGameCard` (3:4 ratio con badge console, preferiti e tempo di gioco).
  - Navigazione completa: `SetupScreen`, `ProfilePickerScreen`, `HomeScreen` (Top Navbar), `DetailsScreen` (multi-disco e save states), `SearchScreen`, `PlayerScreen` (schermo intero con dialog di uscita TV), `SettingsScreen`.
- **Verifica Empirica**:
  - Compilazione APK completata con successo: `./gradlew assembleDebug` (Build Successful).
  - Test unitari eseguiti e superati: `./gradlew test` (49 task eseguiti, 100% superati).

## File Rilevanti
- `app/src/main/java/com/homearcade/tv/HomeArcadeTvApp.kt`: Configurazione Application con cache immagini Coil (150MB su disco).
- `app/src/main/java/com/homearcade/tv/MainActivity.kt`: Entry point Android TV pulito con `AppNavigation`.
- `app/src/main/java/com/homearcade/tv/data/api/`: `ApiClient.kt`, `HomeArcadeApi.kt`, `ServerDiscoveryManager.kt`.
- `app/src/main/java/com/homearcade/tv/ui/`: Design system Retro Arcade (`Color.kt`, `Theme.kt`), componenti TV riutilizzabili (`TvFocusModifier.kt`, `HeroBillboard.kt`, `TvShelfRow.kt`, `TvGameCard.kt`) e tutte le feature screen.
- `app/src/test/java/com/homearcade/tv/data/model/DtosTest.kt`: Test unitari per serializzazione JSON, URL artefatti e mapping colori console.
- `docs/plans/2026-10-17_refactor_home_arcade_tv_native_compose.md`: Piano di lavoro con tutte le fasi completate.

## Prossimi Passi (Next Steps)
1. Eseguire commit atomico dei file su Git (`feat: refactor HomeArcade-TV to native Jetpack Compose for TV with WebRTC stream player`).
2. Eventuale test manuale su dispositivo reale Android TV o emulatore collegato all'add-on Home Assistant reale.
3. Test di input gamepad fisici Bluetooth/USB mappati direttamente tramite WebRTC DataChannel o Android InputManager.

## Comandi Utili
- **Compilazione Debug**: `./gradlew assembleDebug`
- **Esecuzione Test Unitari**: `./gradlew test`
- **Compilazione Kotlin Rapida**: `./gradlew compileDebugKotlin`
- **Installazione su TV/Emulatore**: `./gradlew installDebug`
