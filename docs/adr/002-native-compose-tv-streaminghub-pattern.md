# ADR 002: Adozione del Pattern StreamingHub e UI Nativa Jetpack Compose for TV con WebRTC Stream Player

## Context
La versione as-is di `HomeArcade-TV` (documentata in ADR 001) utilizzava una singola Android WebView sia per il catalogo giochi sia per il gameplay. Questo approccio presentava forti limiti:
- Interfaccia web non ottimizzata per i principi 10-foot TV (mancanza di safe-area overscan da 48dp, navigazione a scaffali lenta, scroll non fluido con D-pad).
- Breaking changes introdotte in `home-arcade-ha` (v2.65.1): introduzione del runner WebRTC Native, gestione multi-profilo, API metadati multi-sorgente e selezione multi-disco per giochi complessi (PSX, Saturn, Dreamcast).
- Necessità di adottare la medesima architettura nativa e fluida collaudata su `streaming-hub-tv`.

## Decision
Abbiamo riscritto l'intera applicazione migrando al pattern moderno di `streaming-hub-tv`:

1. **Catalogo, Navigazione e Metadati 100% Nativi Jetpack Compose for TV**:
   - `androidx.tv:tv-material` e `androidx.tv:tv-foundation`: componenti TV dedicati con scala elastica di focus (1.08x), glow neon fucsia e feedback reattivo.
   - **StreamingHub TV Pattern**: Top Navigation Bar ("Tutti", "Preferiti", "Più Giocati", "Per Console"), Hero Billboard dinamico che reagisce al focus mostrando backdrop art e metadati, caroselli orizzontali `TvShelfRow` raggruppati per sistema o categoria.
   - **Stack di Rete & Data Layer**: Ktor Client 3.0 con motore OkHttp e Kotlinx Serialization JSON per un consumo efficiente e tipizzato delle REST API di `home-arcade-ha`.
   - **Zero-Config LAN Discovery**: `ServerDiscoveryManager` che scansiona la subnet locale sulle porte 5000 e 9876 con fallback di inserimento manuale.
   - **Gestione Multi-Profilo e Dettaglio**: Scheda dettagli ricca con supporto multi-disco, gestione save state visivi e selezione profilo utente all'avvio.

2. **Player Dedicato Hardware-Accelerated (WebRTC / WASM)**:
   - Separazione netta tra esperienza di browsing (nativa) ed esperienza di gioco.
   - La schermata `PlayerScreen` isola la riproduzione su `/api/roms/:id/stream-player` (streaming video WebRTC a 60 FPS sub-millisecondo gestito dal server) con fallback automatico su EmulatorJS WASM `/player`.
   - Dialog modale di uscita TV su pressione del tasto Back per prevenire uscite accidentali.

### Alternative Scartate
- **Mantenere la WebView monoview legacy**: Scartata perché offriva un'esperienza utente scadente su Android TV e non supportava adeguatamente le nuove funzionalità di `home-arcade-ha`.
- **Core Libretro/NDK embedded nell'APK TV**: Scartata per rispetto del principio YAGNI; l'add-on esegue già l'emulazione con accelerazione server-side e streaming a bassissima latenza WebRTC.

## Consequences
- **Vantaggi**:
  - Esperienza utente fluida a 60 FPS su TV con navigazione D-pad reattiva e layout overscan-safe.
  - Avvio istantaneo e caricamento asincrono delle immagini con cache disco Coil da 150MB.
  - Piena compatibilità con le API v2.65.1+ di `home-arcade-ha`.
  - Manutenibilità del codice migliorata grazie alla separazione netta tra data layer, UI components e viewmodel reattivi.
- **Limiti e Debito Tecnico Accettato**:
  - Lo streaming WebRTC richiede che la TV supporti la decodifica video hardware H.264/VP8 (garantita da Android 8.0+ / API 26+).
