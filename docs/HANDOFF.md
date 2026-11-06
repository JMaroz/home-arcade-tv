# Hand-off Snapshot: TV Remote Navigation, Layout & Performance Optimization

## Stato Corrente
- Completata l'analisi tecnica approfondita su tutti gli 8 punti sollevati per l'esperienza da telecomando Android TV.
- Confermato che ogni osservazione dell'utente è fondata:
  - Riga console non scalabile per decine di ROM (richiesta griglia 2D).
  - Lo zoom `scale` e le ombre dinamiche muovono o soffocano visivamente gli elementi e causano jank GPU.
  - La schermata di ricerca si apre vuota e non permette l'input D-pad né apre l'IME tastiera.
  - La Hero Billboard (380dp) e le card (145x205dp) consumano troppo spazio verticale (65-70% dello schermo 1080p).
  - L'aggiornamento immediato dell'hero su ogni cambio focus satura il thread immagini di Coil senza debounce.
  - Il dettaglio gioco apre il focus sul tasto Back anziché sul tasto primario "Gioca Ora".
  - I pulsanti "Motore" e "Cerca Info" affollano l'action bar anziché essere automatici o in settings.
  - Il tasto Back perde il focus attivo e teletrasporta l'utente in cima alla home sul tab "Tutti".
- Creato il piano di lavoro operativo dettagliato in `docs/plans/2026-11-06_tv_remote_navigation_ux.md`.

## File Rilevanti
- `app/src/main/java/com/homearcade/tv/ui/components/TvFocusModifier.kt`: Riprogettazione indicatore di focus statico (glow neon e bordo nitido senza distorsione di scala o ombre pesanti).
- `app/src/main/java/com/homearcade/tv/ui/home/HomeScreen.kt`: Hero compatto (~240dp), card ottimizzate, griglia 2D per console e restore del focus.
- `app/src/main/java/com/homearcade/tv/ui/home/HomeViewModel.kt`: Debounce (250ms) per il caricamento dell'Hero e tracciamento `lastFocusedRomId`.
- `app/src/main/java/com/homearcade/tv/ui/search/SearchScreen.kt`: Visualizzazione catalogo/recenti a vuoto e gestione focus/tastiera TV.
- `app/src/main/java/com/homearcade/tv/ui/search/SearchViewModel.kt`: Popolamento catalogo iniziale.
- `app/src/main/java/com/homearcade/tv/ui/details/DetailsScreen.kt`: Focus automatico su "Gioca Ora" e pulizia action bar.
- `app/src/main/java/com/homearcade/tv/ui/details/DetailsViewModel.kt`: Auto-scraping trasparente in background se metadati mancanti.

## Prossimi Passi (Next Steps)
- Eseguire Fase 1 e 2: Stabilizzazione `TvFocusModifier` e introduzione del debounce hero in `HomeViewModel`.
- Eseguire Fase 3: Ridisegno Home compatta e implementazione griglia 2D per la sezione console.
- Eseguire Fase 4: Ripristino e fix della schermata di ricerca.
- Eseguire Fase 5 e 6: Focus "Gioca Ora", rimozione pulsanti superflui con auto-scraping e ripristino posizione su Back.
- Compilazione, verifica test (`./gradlew test`) e deploy su Android TV (`adb -s 192.168.1.50:5555`).

## Comandi Utili
- Test Unitari: `./gradlew test`
- Build APK Debug: `./gradlew assembleDebug`
- Installazione Android TV: `adb -s 192.168.1.50:5555 install -r app/build/outputs/apk/debug/app-debug.apk`
