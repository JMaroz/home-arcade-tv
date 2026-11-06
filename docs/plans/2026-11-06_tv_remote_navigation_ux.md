# Piano di Lavoro: Ottimizzazione Navigazione TV, Layout e Fluidità da Telecomando

## Obiettivo
Risolvere le criticità di navigazione tramite telecomando Android TV identificate dall'utente:
1. Sostituire la riga orizzontale della sezione "Per Console" con una griglia 2D navigabile.
2. Eliminare lo scuotimento degli elementi adiacenti dovuto all'animazione di focus (scale/border/font shift), adottando indicatori di focus stabili (glow neon + bordo statico).
3. Correggere la schermata di ricerca: visualizzare contenuti iniziali (catalogo/recenti) e consentire l'input da telecomando.
4. Ribilanciare le dimensioni della Home (Hero Billboard compatto e card ottimizzate per visualizzare più righe contemporaneamente).
5. Migliorare la fluidità complessiva (debounce aggiornamento Hero su cambio focus, rimozione overhead ombre dinamiche, pooling immagini Coil).
6. Impostare il focus automatico iniziale su "Gioca Ora" all'apertura del dettaglio gioco.
7. Rimuovere i pulsanti superflui "Motore" e "Cerca Info" dalla scheda di dettaglio, abilitando l'auto-scraping trasparente in background in caso di informazioni mancanti.
8. Ripristinare focus e posizione di scroll sulla card del gioco precedente quando si preme Back dal dettaglio.

## Scope & Non-Goals
- **In Scope**:
  - `TvFocusModifier.kt`: eliminazione scala invasiva, introduzione focus style stabile senza layout shift.
  - `HomeScreen.kt` & `HomeViewModel.kt`: layout compatto, debounce hero updates, griglia console, focus restoration con `lastFocusedRomId`.
  - `TvShelfRow.kt` & `TvGameCard.kt`: dimensioni ottimizzate e supporto focus restoration.
  - `SearchScreen.kt` & `SearchViewModel.kt`: popolamento iniziale catalogo, focus e trigger input per telecomando.
  - `DetailsScreen.kt` & `DetailsViewModel.kt`: initial focus su "Gioca Ora", rimozione bottoni superflui, auto-scrape asincrono in background se metadati assenti.
- **Non-Goals (YAGNI)**:
  - Non riscrivere la pipeline del player di gioco o WebRTC streaming.
  - Non alterare le API del backend server o il protocollo WebSocket di controllo.
  - Non introdurre librerie esterne non necessarie (rimanere su Jetpack Compose TV / stdlib).

## Task Checklist
- [ ] **Fase 1: Focus Modifier Stabile & Anti-Jank (Punto 2)**
  - [ ] Rimuovere lo scaling dinamico (`focusedScale = 1.0f`) o ridurlo a valore impercettibile senza layout shift.
  - [ ] Sostituire l'ombra dinamica `shadow()` (pesante su GPU Mali/Adreno TV) con bordo neon ad alto contrasto e glow leggero.
  - [ ] Fissare i pesi dei font nelle tab per evitare spostamenti di larghezza tra `Medium` e `Bold`.
- [ ] **Fase 2: Fluidità & Prestazioni D-pad (Punto 5)**
  - [ ] Introdurre debounce (200-250ms) nel `HomeViewModel.onCardFocused` per non scatenare ricomposizioni e caricamento immagini ad ogni micro-passo del D-pad.
  - [ ] Aggiungere `key` e `contentType` stabili agli item di `LazyColumn` e `LazyRow`.
  - [ ] Disattivare logger verboso Coil e tuning crossfade per TV.
- [ ] **Fase 3: Layout Home & Griglia Console (Punti 1 e 4)**
  - [ ] Ridurre l'altezza dell'Hero Billboard (da 380dp a 240-260dp) per liberare spazio visivo verticale.
  - [ ] Ridimensionare le card di gioco (da 145x205 dp a 120x170 dp) per mostrare 2-3 scaffali contemporaneamente.
  - [ ] In `HomeSection.SYSTEMS`, sostituire la `TvShelfRow` a riga singola con una griglia 2D (`LazyVerticalGrid` a 5-6 colonne o righe strutturate per TV) per sfogliare comodamente decine o centinaia di ROM.
- [ ] **Fase 4: Correzione Ricerca per Telecomando TV (Punto 3)**
  - [ ] All'apertura della ricerca a query vuota, mostrare il catalogo completo o i giochi recenti/popolari invece di uno schermo nero.
  - [ ] Configurare il campo di ricerca con supporto corretto a D-pad/Enter e attivazione tastiera Android TV (`SoftwareKeyboardController` o tastierino rapido).
- [ ] **Fase 5: Dettaglio Gioco - Focus "Gioca Ora" & Auto-Scraping (Punti 6 e 7)**
  - [ ] Aggiungere `FocusRequester` sul pulsante "Gioca Ora" in `DetailsScreen` con attivazione automatica in `LaunchedEffect(Unit)`.
  - [ ] Rimuovere i bottoni "Motore" e "Cerca Info" dalla barra delle azioni principali.
  - [ ] Nel `DetailsViewModel`, eseguire in background il controllo metadati: se la ROM non ha descrizione/anno/info, avviare automaticamente lo scraping asincrono senza richiedere click manuale.
- [ ] **Fase 6: Focus Restoration su Back (Punto 8)**
  - [ ] Salvare in `HomeViewModel` l'id dell'ultima ROM aperta o a fuoco (`lastFocusedRomId`).
  - [ ] Al ritorno su `HomeScreen`, richiedere il focus tramite `FocusRequester` o scrollare l'indice salvato in modo che la schermata non salti in cima alle tab ("Tutti").

## Strategia di Verifica
1. **Compilazione e Test Unitari**:
   - Eseguire `./gradlew test` per verificare che non ci siano regressioni nei test esistenti.
   - Verificare che il codice compili pulito con `./gradlew assembleDebug`.
2. **Verifica Empirica su Dispositivo TV**:
   - Installare l'APK aggiornato su Android TV (`adb -s 192.168.1.50:5555 install -r app/build/outputs/apk/debug/app-debug.apk`).
   - Navigazione D-Pad: testare lo scorrimento veloce orizzontale e verificare l'assenza di scatti (grazie al debounce).
   - Apertura dettaglio gioco: verificare che il focus vada subito su "Gioca Ora".
   - Pressione tasto BACK dal telecomando: verificare che la schermata Home torni esattamente sul gioco precedente senza saltare in cima.
   - Scheda per Console: verificare la presenza della griglia 2D.
   - Ricerca: verificare la presenza dei giochi all'apertura e la possibilità di effettuare ricerche.
