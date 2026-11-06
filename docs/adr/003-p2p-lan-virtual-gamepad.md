# ADR 003: Architettura P2P LAN (WebSocket + mDNS) e Virtual Gamepad W3C per Controller Smartphone (Multiplayer P2-P4)

## Context
HomeArcade TV consente di eseguire giochi arcade e retro tramite WebRTC streaming o WASM (EmulatorJS). Sebbene un primo giocatore (P1) possa giocare collegando un controller fisico alla TV, non era possibile per un secondo giocatore (es. in titoli co-op come Metal Slug, Street Fighter, TMNT) unirsi alla partita locale senza disporre di un secondo controller hardware o interrompere la sessione per complessi accoppiamenti Bluetooth.

L'applicazione necessitava di:
- Un'esperienza di join a zero attrito ("apro l'app sullo smartphone -> rilevo partita TV -> tap Connetti -> smartphone diventa gamepad").
- Latenza sub-millisecondo (<5ms su Wi-Fi LAN) per giochi arcade reattivi a 60 FPS.
- Totale disaccoppiamento dal server Home Assistant (compatibilità sia con WASM locale che con WebRTC streaming).
- Fallback browser istantaneo via QR Code per ospiti senza APK installato o amici con iPhone.

## Decision
Abbiamo implementato un'architettura **Peer-to-Peer Wi-Fi LAN diretta** a tre pilastri:

1. **Embedded TV Input Server (`TvInputServer.kt`)**:
   - Micro-server Ktor CIO / socket embedded nella TV avviato all'ingresso in `PlayerScreen` (porta `9877`).
   - Registrazione mDNS automatica (`_homearcade._tcp`) con metadati gioco (`game`, `platform`, `rom_id`).
   - Endpoint WebSocket `/ws/gamepad` con supporto per pacchetti binari compatti a 6 byte (`[magic, slot, maskLow, maskHigh, ax, ay]`) e fallback JSON.
   - Servizio HTTP GET `/` per servire il Web Controller HTML5 (`web_controller.html`) con QR Code a video per ospiti veloci.

2. **W3C Gamepad API Virtual Bridge (`virtual_gamepad_bridge.js`)**:
   - Monkeypatch dello standard W3C `navigator.getGamepads()` iniettato nella `WebView` della TV al caricamento della partita.
   - Mantiene lo slot 0 (P1) per controller fisici / telecomando TV e popola dinamicamente gli slot 1, 2, 3 (P2, P3, P4) con oggetti Gamepad standard W3C conformi alla specifica.
   - Dispatcia eventi `gamepadconnected` e `gamepaddisconnected` per l'auto-rilevamento da parte di EmulatorJS e WebRTC runner.

3. **Modulo Android Nativo per Smartphone (`:mobile` - `com.homearcade.controller`)**:
   - Progetto Jetpack Compose Mobile integrato nel monorepo (`settings.gradle.kts`).
   - Radar mDNS (`LanGameRadar.kt`) con scansione automatica delle sessioni attive in LAN.
   - Controller touch dinamico (`ArcadeGamepadScreen.kt`) con preset Arcade (4 tasti ad arco A/B/C/D per Neo Geo tipo Metal Slug) e Console (diamante per SNES/PSX), tasti dedicati "INSERT COIN" e "START", e feedback aptico a vibrazione micro-click (`VibrationEffect`).

### Alternative Scartate
- **Bluetooth HID Device Profile (`BluetoothHidDevice`)**: Scartato per attrito eccessivo (richiede di navigare nelle impostazioni di Android TV con il telecomando per accoppiare il telefono, interrompendo il gioco) e per supporto frammentato/rotto su molte ROM Android OEM (Samsung, Xiaomi).
- **Relay Centralizzato via Home Assistant Add-on**: Scartato perché raddoppia la latenza di rete (Phone $\rightarrow$ Router $\rightarrow$ Server HA $\rightarrow$ Router $\rightarrow$ TV) e non supporta l'emulazione WASM locale senza caricare inutilmente il backend HA.
- **Solo Web App via QR Code (Stile Jackbox)**: Scartato come soluzione unica a causa di potenziali interferenze di gesture del browser (swipe per tornare indietro) e assenza di auto-discovery automatica ad app aperta. Mantenuto invece come eccellente canale di fallback complementare.

## Consequences
- **Vantaggi**:
  - Esperienza utente immediata a 1 tap: lo smartphone rileva la partita ed entra come P2 in meno di 2 secondi.
  - Latenza minima (<3ms su LAN Wi-Fi standard).
  - Supporta fino a 4 giocatori simultanei con auto-assegnazione dello slot libero.
  - Zero modifiche richieste all'add-on Home Assistant.
  - Due APK indipendenti e ottimizzati (`:app` per Leanback TV, `:mobile` per touchscreen smartphone).
- **Limiti e Debito Tecnico Accettato**:
  - Smartphone e Android TV devono essere connessi alla medesima sottorete Wi-Fi locale (scenario tipico domestico).
  - Nel caso di router con "AP Isolation / Guest Network" attiva, è necessario utilizzare l'inserimento IP manuale integrato.
