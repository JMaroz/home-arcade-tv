# ADR 001: Architettura Ibrida Compose TV + Fullscreen WebView

## Context
HomeArcade è un frontend per retro gaming per Home Assistant basato su EmulatorJS. L'obiettivo del client Android TV (`HomeArcade-TV`) è consentire l'avvio e la fruizione diretta dei giochi su grande schermo tramite controller o telecomando TV, bypassando l'interfaccia web standard della dashboard di Home Assistant.

L'applicazione necessita di:
- Un'esperienza di onboarding e configurazione semplice ottimizzata per telecomando TV (D-pad).
- Caricamento fluido e a schermo intero dei giochi gestiti da EmulatorJS.
- Inoltro trasparente degli input da telecomando (D-pad) e gamepad fisici (Bluetooth/USB) al layer web.
- Monitoraggio della connettività con il server HomeArcade locale (`/api/health`).

## Decision
Abbiamo adottato un'architettura ibrida a due livelli:

1. **Native UI Layer (Jetpack Compose for TV)**:
   - Utilizzo di `androidx.tv:tv-material:1.1.0` e `androidx.navigation.compose` per le schermate di Splash, Configurazione Server (Setup), Impostazioni e Overlay di errore connessione.
   - Persistenza della configurazione server (Host IP e Porta) tramite AndroidX DataStore Preferences.
   - Gestione dello stato asincrono e test di connettività tramite `SetupViewModel` e coroutine.

2. **Gameplay & Web Execution Layer (Android WebView + Bridges)**:
   - Esecuzione fullscreen di HomeArcade tramite Android `WebView` con hardware acceleration e mixed content abilitato per reti locali private (`network_security_config.xml`).
   - `TVKeyBridge`: intercetta i codici `KeyEvent` del telecomando TV / D-pad e li propaga tramite `evaluateJavascript` scatenando eventi sintetici `KeyboardEvent` compatibili con i key handler del player EmulatorJS.
   - `JSBridge`: interfaccia bidirezionale `@JavascriptInterface` per consentire alla webapp di visualizzare Toast nativi o richiedere la chiusura dell'app.
   - `GamepadPoller`: poller basato su `android.hardware.input.InputManager` per mappare i controller collegati e iniettare gli stati in `window.__tvGamepads`.
   - `ConnectionMonitor`: polling periodico ogni 15s sull'endpoint `/api/health` con gestione di fallback e auto-retry su `HomeScreen`.

### Alternative Scartate
- **Client 100% nativo (Libretro / NDK)**: scartato per YAGNI e complessità manutentiva. Avrebbe richiesto il porting e la compilazione multipiattaforma di tutti i core di emulazione in Android NDK, vanificando la compatibilità con il backend HomeArcade.
- **PWA / Browser TV generico**: scartato per la mancanza di supporto nativo ai gamepad su molti browser TV e per l'assenza di gestione automatica dell'IP server locale e auto-reconnect.

## Consequences
- **Vantaggi**:
  - Tempo di sviluppo ridotto e riuso al 100% delle funzionalità server di HomeArcade.
  - Setup snello con D-pad su TV senza tastiere fisiche.
  - Controllo granulare sul ciclo di vita della pagina, errori di rete e disconnessioni.
- **Limiti e Debito Tecnico Accettato**:
  - Le performance di emulazione dipendono dalla versione di Android System WebView installata sulla TV.
  - Il bridging degli input tramite `evaluateJavascript` introduce una latenza minima (< 16ms), considerata ampiamente accettabile per giochi arcade/retro tramite gamepad passthrough.
