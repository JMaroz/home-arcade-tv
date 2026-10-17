# Hand-off Snapshot: HomeArcade TV App Improvements

## Stato Corrente
Completati con successo tutti i 4 interventi richiesti per l'applicazione Android TV / Google TV:
1. **Icona e Banner TV**: Allineate le icone mipmap (`ic_launcher.png`) e generato il Leanback banner 16:9 (`@drawable/banner`) dall'asset originale dell'add-on HomeArcade.
2. **Caroselli Piattaforme con Ultime Aggiunte**: In `HomeScreen` ("Tutti"), dopo "Continua a Giocare" e "Preferiti", ogni piattaforma espone il proprio carosello dedicato (`"${shelf.displayName} • Ultime Aggiunte"`) ordinato per data di aggiunta decrescente.
3. **Schermata Dettagli Gioco (`DetailsScreen`)**: Arricchita con badge bar estesa, schede informative tecniche ("Statistiche di Gioco", "Scheda Tecnica & File", "HowLongToBeat"), azione "Cerca Info Online" (`/api/roms/:id/scrape-meta`) e carosello "Altri titoli per [Piattaforma]".
4. **Fix Focus & Uscita Partita (`PlayerScreen`)**: Risolto il blocco D-pad nella dialog di uscita: disattivata l'intercettazione tasti del `WebView` quando `isConfirmingExit == true`, associato `FocusRequester` al pulsante principale con `LaunchedEffect`, e gestito `BackHandler` / tasto B del gamepad.

## File Rilevanti
- `app/src/main/AndroidManifest.xml`: Collegato `android:banner="@drawable/banner"` e icona add-on.
- `app/src/main/res/drawable/banner.png` & `drawable-xhdpi/banner.png`: Banner 16:9 standard Leanback.
- `app/src/main/res/mipmap-*/ic_launcher.png`: Icone ad alta risoluzione per tutte le densità.
- `app/src/main/java/com/homearcade/tv/data/model/Dtos.kt`: Esteso `RomDto` (size, scrapeStatus, raGameId, format helpers) e aggiunto `HltbResponseDto`.
- `app/src/main/java/com/homearcade/tv/data/api/HomeArcadeApi.kt` & `RomRepository.kt`: Aggiunti endpoint `getHltb(id)` e `scrapeMeta(id)`.
- `app/src/main/java/com/homearcade/tv/ui/home/HomeViewModel.kt` & `HomeScreen.kt`: Esposti e renderizzati i caroselli piattaforme con ultime aggiunte.
- `app/src/main/java/com/homearcade/tv/ui/details/DetailsViewModel.kt` & `DetailsScreen.kt`: Nuova scheda tecnica completa, HLTB e carosello correlati.
- `app/src/main/java/com/homearcade/tv/ui/player/PlayerScreen.kt`: Risolto consumo eventi D-pad e focus management della WebView.
- `app/src/main/java/com/homearcade/tv/ui/components/TvFocusModifier.kt`, `TvGameCard.kt`, `TvShelfRow.kt`: Ottimizzazione micro-zoom e preallocazione padding caroselli.

## Prossimi Passi (Next Steps)
- Eseguire testing di regressione su TV fisica (Philips TPM191E) verificando il ciclo di gioco e l'uscita con telecomando / gamepad Bluetooth.
- Valutare caching offline di HowLongToBeat nel database SQLite backend dell'add-on per istantaneità senza round-trip.

## Comandi Utili
- Esecuzione Test Unitari: `./gradlew test`
- Build APK Debug: `./gradlew assembleDebug`
- Installazione diretta su TV via ADB: `adb connect <IP_TV>:5555 && adb install -r app/build/outputs/apk/debug/app-debug.apk`
