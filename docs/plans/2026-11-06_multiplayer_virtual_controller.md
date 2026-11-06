# Piano di Lavoro: HomeArcade Mobile Controller & Multiplayer Locale (Fino a 4 Giocatori)

## Obiettivo
Consentire a un secondo utente (e fino a 4 giocatori simultanei) di unirsi a una partita in corso su **HomeArcade TV** (es. *Metal Slug*, *Street Fighter*, *Cadillacs and Dinosaurs*, *TMNT*) utilizzando il proprio smartphone Android come controller virtuale touch ultra-reattivo (<5ms su Wi-Fi LAN), senza hardware aggiuntivo o configurazioni complesse.

## Decisioni Architetturali Consolidate (/grill-me)
1. **Connessione LAN P2P Diretta**: WebSocket locale sulla TV (porta 9877) + auto-discovery mDNS (`_homearcade._tcp`). Nessun passaggio da Home Assistant, nessun pairing Bluetooth.
2. **Layout Dinamico Intelligente**: Preset automatico Arcade a 4 tasti ad arco (A: Fuoco, B: Salto, C: Granata, D: Speciale) per Neo Geo / Arcade, e preset a diamante (A/B/X/Y + L/R) per console (SNES/PSX).
3. **Monorepo Multi-Modulo**: Modulo `:mobile` integrato nel repository `home-arcade-tv`, con riuso di palette neon e DTO, producendo due APK indipendenti (`:app-release.apk` e `:mobile-release.apk`).
4. **Dual Channel (Nativo + Web Fallback)**: App Android nativa come canale primario, affiancata da una mini-webpage (<15KB) servita dalla TV con QR Code per consentire l'accesso rapido a ospiti o amici con iPhone.
5. **Supporto Fino a 4 Giocatori**: Auto-assegnazione del primo slot libero (P2, poi P3, poi P4).
6. **Haptic Feedback Tattile**: Vibrazione micro-click (`VibrationEffect`) a ogni tocco, con toggle rapido di disattivazione.

## Scope & Non-Goals
- **In Scope**:
  - Modulo `:mobile` in Kotlin + Jetpack Compose con layout touch arcade responsive e feedback aptico.
  - Zero-Config Auto-Discovery su rete locale (Android Network Service Discovery / mDNS).
  - Micro-server WebSocket embedded in `PlayerScreen` dell'app TV (`:app`) per ricezione pacchetti di controllo.
  - Shim W3C `navigator.getGamepads()` iniettato nella `WebView` della TV per esporre Player 2/3/4 sia in modalità WASM che WebRTC.
  - Fallback Web Controller (micro web page servita dal server TV con QR code per ospiti iOS o senza app).
- **Non-Goals (YAGNI)**:
  - Nessuna dipendenza da server cloud o internet (100% offline & LAN-only).
  - Nessuna modifica necessaria al backend `home-arcade-ha` (architettura indipendente).
  - No Bluetooth pairing (scartato per attrito e limitazioni hardware).

## Task Checklist
- [x] **Fase 1: TV Core Engine & W3C Gamepad Shim (`:app`)**
  - [x] Implementare `virtual_gamepad_bridge.js` per shimmare `navigator.getGamepads()` in WebView per gli slot 1, 2, 3 (P2, P3, P4).
  - [x] Implementare `TvInputServer.kt` (Ktor CIO / WebSocket su porta 9877) con gestione pacchetti di input e lifecycle in `PlayerScreen.kt`.
  - [x] Integrare registrazione mDNS via `NsdManager` (`_homearcade._tcp`) con metadati del gioco attivo (titolo, piattaforma, slot disponibili).
  - [x] Servire la mini-webpage HTML5 Web Controller per accesso browser diretto via QR Code.
- [x] **Fase 2: Modulo Android `:mobile` (HomeArcade Controller)**
  - [x] Configurare modulo `:mobile` in `settings.gradle.kts` e `mobile/build.gradle.kts` con Jetpack Compose Mobile.
  - [x] Implementare `LanGameRadar.kt` per ascolto mDNS e auto-rilevamento sessione attiva.
  - [x] Realizzare schermata `DiscoveryLobbyScreen.kt` (Radar animato, scheda gioco in corso, tasto "Entra come Giocatore 2/3/4").
  - [x] Realizzare `ArcadeGamepadScreen.kt` con preset dinamico (Arcade 4 tasti ad arco per Neo Geo / Diamante per console) e pulsanti dedicati "INSERT COIN" e "START".
  - [x] Aggiungere feedback aptico (`VibrationEffect`) con toggle rapido ON/OFF.
  - [x] Gestione connessione WebSocket ad alta frequenza (60Hz / event-based su variazione di stato) con TCP_NODELAY.
- [x] **Fase 3: UX TV & Overlay Presenza Giocatori**
  - [x] Aggiungere indicatore HUD non invasivo su TV ("P2 Connesso", "P3 Connesso" con animazione neon).
  - [x] Istruzioni di connessione e IP in overlay di pausa TV per rapido join o accesso web.
- [x] **Fase 4: Verifica Empirica & Testing**
  - [x] Test unitari eseguiti con successo (`./gradlew test` con `GamepadProtocolTest`).
  - [x] Build completa di entrambi gli APK verificata (`./gradlew assembleDebug` per `:app` e `:mobile`).
  - [x] Validazione handshake WebSocket e parsing frame binari/JSON.

## Strategia di Verifica
- **Build**: `./gradlew assembleDebug` per compilare sia `:app` che `:mobile`.
- **Test Unitari**: `./gradlew test` per validare serializzazione pacchetti e gestione code input.
- **Test Funzionale**: Lancio emulatore TV + emulatore/dispositivo mobile su stessa subnet LAN e verifica handshake WebSocket e ricezione comandi su WebView.
