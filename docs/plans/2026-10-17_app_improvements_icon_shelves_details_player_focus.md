# Piano di Lavoro: Miglioramenti HomeArcade TV (Icona, Caroselli, Dettagli Gioco, Fix Focus Player)

## Obiettivo
Risolvere le criticità riscontrate durante il test su Android TV / Google TV:
1. Allineare l'icona e il banner dell'app TV a quella ufficiale dell'add-on HomeArcade.
2. Aggiungere nella Home ("Tutti") i caroselli dedicati per ciascuna piattaforma con le ultime aggiunte ordinate cronologicamente.
3. Arricchire radicalmente la schermata dettagli del gioco con metadati tecnici, statistiche di gioco, HowLongToBeat, opzione di scrape e carosello di giochi correlati della stessa piattaforma.
4. Risolvere il blocco del focus e dei tasti D-pad nella dialog di uscita durante la riproduzione del gioco in `PlayerScreen`.

---

## Scope & Non-Goals
- **In-Scope**:
  - Generazione icone mipmap ed export banner TV 16:9 (`@drawable/banner`) dagli asset dell'add-on.
  - Aggiornamento `AndroidManifest.xml` con banner ed icona corretti.
  - Logica di raggruppamento e ordinamento per ultime aggiunte per piattaforma in `HomeViewModel` e visualizzazione caroselli in `HomeScreen`.
  - Estensione modello `RomDto` (size, scrapeStatus, raGameId) e integrazione endpoint HLTB (`/api/roms/:id/hltb`) e scrape (`/api/roms/:id/scrape-art`).
  - Riprogettazione UI di `DetailsScreen` con schede tecniche, statistiche, HLTB, sinossi e carosello "Altri titoli per [Piattaforma]".
  - Fix gestione eventi D-pad, disabilitazione input WebView e gestione `FocusRequester` + `BackHandler` in `PlayerScreen`.
- **Non-Goals**:
  - Riscrivere il motore WebRTC stream runner o il protocollo di rete backend.
  - Modificare la gestione delle credenziali di signing o l'architettura base dei repository.

---

## Task Checklist

### Fase 1: Allineamento Icona e Banner Add-on (Punto 1)
- [x] Recuperare `/Users/andrea/Repository/home-arcade-ha/icon.png` e `cabinet_bridge/logo.png`.
- [x] Generare le densità mipmap (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) per `ic_launcher.png`.
- [x] Generare il banner Android TV 16:9 (`320x180` dp/px) `res/drawable/banner.png` e `res/drawable-xhdpi/banner.png`.
- [x] Aggiornare `AndroidManifest.xml` per collegare `android:banner="@drawable/banner"`.

### Fase 2: Caroselli Piattaforme con Ultime Aggiunte (Punto 2)
- [x] Creare mappa descrittiva dei sistemi con nomi estesi (es. `PS1` -> "PlayStation", `SNES` -> "Super Nintendo", ecc.) e colori dedicati.
- [x] In `HomeViewModel`: implementare il raggruppamento per console con ordinamento decrescente per `createdAt`/`id` (ultimi aggiunti per piattaforma).
- [x] In `HomeScreen`: inserire sotto "Continua a Giocare" e "Preferiti" i caroselli `"${shelf.displayName} • Ultime Aggiunte"` per ogni piattaforma con giochi disponibili.

### Fase 3: Arricchimento Schermata Dettagli Gioco (Punto 3)
- [x] Estendere `RomDto` con `size: Long? = null`, `scrapeStatus: String? = null`, `raGameId: Int? = null`.
- [x] Aggiungere DTO `HltbResponseDto` e metodo `getHltb(id: Int)` in `HomeArcadeApi` e `RomRepository`.
- [x] Aggiungere metodo `scrapeRomArt(id: Int)` per permettere l'aggiornamento online dei metadati mancanti.
- [x] In `DetailsViewModel`: caricare dettagli completi, dati HLTB, e giochi correlati della stessa piattaforma (`relatedGames`).
- [x] In `DetailsScreen`:
  - [x] Aggiungere badge bar completa (sistema esteso, anno, genere, giocatori, rating, pill formato file es. CHD/ISO).
  - [x] Aggiungere pulsanti azione ("Gioca Ora", "Preferito", "Motore WebRTC/WASM", "Cerca Info Online").
  - [x] Aggiungere griglia schede TV: "Statistiche di Gioco" (tempo giocato, sessioni, ultima partita), "Scheda Tecnica & File" (nome file, dimensione MB, core consigliato), "HowLongToBeat" (storia, extra, completista).
  - [x] Aggiungere carosello in basso: "Altri titoli per [Piattaforma]".

### Fase 4: Fix Focus e Uscita Partita in PlayerScreen (Punto 4)
- [x] In `PlayerScreen.kt`: aggiornare `WebView.setOnKeyListener` per bypassare completamente l'invio tasti a JS quando `uiState.isConfirmingExit == true`.
- [x] Gestire lo stato di focus del `WebView`: disabilitare focus (`isFocusable = false`, `clearFocus()`) quando la dialog è attiva e ripristinarlo alla chiusura.
- [x] Aggiungere `FocusRequester` al pulsante principale della dialog ("Continua a Giocare" / "Esci al Catalogo") con `LaunchedEffect(uiState.isConfirmingExit) { requestFocus() }`.
- [x] Correggere `BackHandler`: se la dialog è già aperta, chiuderla tornando al gioco; se è chiusa, aprirla.
- [x] Gestire D-pad Left/Right per navigare tra i pulsanti e tasto B per annullare l'uscita.

### Fase 5: Verifica e Validazione
- [x] Esecuzione test unitari (`./gradlew test`).
- [x] Verifica compilazione debug (`./gradlew assembleDebug`).
- [x] Aggiornamento snapshot `docs/HANDOFF.md`.

---

## Strategia di Verifica
1. **Verifica Asset & Manifest**:
   - `file app/src/main/res/drawable/banner.png` (deve essere 320x180 o proporzionale 16:9).
   - Verifica dichiarazione in `AndroidManifest.xml`.
2. **Verifica Compilazione & Test**:
   - `./gradlew test` (esito 0 errori).
   - `./gradlew assembleDebug` (build APK completata con successo).
3. **Verifica Funzionale**:
   - Test navigazione Home e verifica presenza dei caroselli piattaforme ordinati per ultime aggiunte.
   - Test apertura schermata dettagli e verifica visualizzazione schede tecniche, statistiche, HLTB e carosello correlati.
   - Test simulazione flusso dialog uscita e verifica passaggio focus sui pulsanti senza consumazione tasti da parte della WebView.
