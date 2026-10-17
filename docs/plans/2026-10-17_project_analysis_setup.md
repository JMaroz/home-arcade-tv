# Piano di Lavoro: Analisi del Progetto e Configurazione File Essenziali

**Data**: 2026-10-17  
**Task**: Analisi architetturale, verifica build e creazione della struttura documentale/operativa essenziale (`docs/`, `HANDOFF.md`, CI/CD, configurazioni standard).

---

## 1. Obiettivo
Analizzare a fondo lo stato corrente del progetto Android TV (`HomeArcade-TV`), verificare la corretta compilazione e coerenza con i requisiti, e creare la struttura di file di governance e documentazione necessaria (`docs/plans/`, `docs/HANDOFF.md`, `docs/adr/`, `.github/workflows/ci.yml`).

## 2. Scope & Non-Goals
- **In Scope**:
  - Analisi approfondita di architettura, dipendenze Gradle, componenti UI (Jetpack Compose for TV), WebView bridging e gestione stato.
  - Verifica compilazione con `./gradlew assembleDebug`.
  - Creazione di `docs/HANDOFF.md` aggiornato e sintetico (30-60 righe, no append).
  - Creazione di ADR iniziale `docs/adr/001-tv-webview-architecture.md` che formalizza la scelta Compose TV + WebView fullscreen + JS Bridge.
  - Creazione di una pipeline CI GitHub Actions (`.github/workflows/ci.yml`) per build e validazione automatica del progetto.
- **Non-Goals (YAGNI / Ponytail)**:
  - Nessuna aggiunta superflua di librerie di terze parti o dipendenze non richieste.
  - Nessuna riscrittura di componenti già funzionanti senza esplicito bisogno.
  - Nessun test fittizio o scaffold vuoto che non sia integrato nella build.

## 3. Task Checklist
- [x] Controllo stato git (`git status`) e verifica pulizia workspace
- [x] Ispezione e analisi file sorgente (`app/src/main/...`), manifest, risorse e gradle config
- [x] Verifica empirica di compilazione (`./gradlew assembleDebug`)
- [x] Creazione piano strutturato (`docs/plans/2026-10-17_project_analysis_setup.md`)
- [x] Creazione ADR iniziale (`docs/adr/001-tv-webview-hybrid-architecture.md`)
- [x] Creazione workflow CI (`.github/workflows/ci.yml`)
- [x] Creazione snapshot handoff conforme alle linee guida (`docs/HANDOFF.md`)
- [x] Verifica finale git status e riscontro per l'utente

## 4. Strategia di Verifica
- Esecuzione `./gradlew assembleDebug` con esito BUILD SUCCESSFUL.
- Validazione sintassi YAML di `.github/workflows/ci.yml`.
- Verifica della presenza e completezza dei file generati in `docs/` e conformità alle regole Ponytail.
